import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

public class QuizGameServer {
	// default values of IP and PORT
	private static String IP = "localhost";
	private static int PORT = 1234;

	// questions list
	private static List<String[]> questions = Arrays.asList(
			new String[] { "What is 2 + 5?", "7" },
			new String[] { "What is 2 * 5?", "10" }, 
			new String[] { "What is 5 - 1?", "4" });

	// load file config.txt
	public static void loadConfig() {
		File configFile = new File("C:/Users/hanse/Desktop/config.txt");

		// if the file missing default values are used
		if (!configFile.exists()) {
			System.out.println("Error, using default values");
			return;
		}

		try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
			IP = reader.readLine().trim();
			PORT = Integer.parseInt(reader.readLine().trim());
		} catch (IOException e) {
			System.out.println("Error, using default values");
		}
	}
	
	// connect with client
	public static void main(String[] args) {
		loadConfig();

		try (ServerSocket serverSocket = new ServerSocket(PORT)) {
			System.out.println("Start Server...");
			System.out.println("Waiting for clients");

			while (true) {
				Socket socket = null;

				try {
					socket = serverSocket.accept();
					System.out.println("Connected.");

					int score = 0;

					try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
						BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

						// send questions to client
						for (int i = 0; i < questions.size(); i++) {
							String[] question = questions.get(i);
							String Questions = String.format(
									"HTTP/1.1 200 OK\r\n" + 
							        "Question-Number: %d\r\n" + 
								    "\r\n" + 
							        "%s\r\n", 
							        i + 1, question[0]);
							out.write(Questions);
							out.flush();

							// receive client's answer
							in.readLine();
							String headerLine = in.readLine();
							String headerAnswer = headerLine.substring(7).trim();
							in.readLine();
							String bodyAnswer = in.readLine();
							
							// if client's answer is not numeric, give error message
							if(bodyAnswer == null || bodyAnswer.isEmpty() || bodyAnswer.chars().anyMatch(c -> c < 48 || c > 57)) {
								System.out.println("Received answer: " + bodyAnswer);
								System.out.println("Error: Invalid answer format.");
								out.write("HTTP/1.1 400 Bad Request\r\n\r\n");
								out.flush();
								
								out.write(Questions);
								out.flush();
								continue;
							}
							
							System.out.println("Received answer: " + bodyAnswer);
							
							// if client's answer is correct, plus the score
							String answer = bodyAnswer;
							if (question[1].equalsIgnoreCase(answer)) {
								System.out.println("Correct!");
								out.write("HTTP/1.1 200 OK\r\n" + 
								          "Result: Correct!\r\n" +
										  "\r\n" +
								          "Correct!\r\n");
								out.flush();
								score++;
							} else {
								// if client's answer is incorrect, give the correct answer
								System.out.println("Incorrect.");
								out.write(String.format(
										"HTTP/1.1 200 OK\r\n" + 
								        "Result: Incorrect.\r\n" +
										"\r\n" +
										"Incorrect. The correct answer is: %s\r\n",
										question[1]));
								out.flush();
							}
						}

						// if all questions is done, give total score
						out.write(String.format("HTTP/1.1 200 OK\r\n" + 
						                        "Total-Score: %d\r\n" + "\r\n" + 
								                "%d\r\n", 
								                score, score));
						out.flush();
					}

				} catch (IOException e) {
					System.out.println(e.getMessage());
				} finally {
					try {
						// if it is finished, close socket connection
						socket.close();
						System.out.println("Client connection closed");
					} catch (IOException e) {
						System.out.println(e.getMessage());
					}
				}

			}
		} catch (IOException e) {
			System.out.println("Disconnected.");
		}
	}
}
