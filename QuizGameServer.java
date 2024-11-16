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
	private static String IP = "localhost";
	private static int PORT = 1234;

	private static List<String[]> questions = Arrays.asList(
			new String[] { "What is 2 + 5?", "7" },
			new String[] { "What is 2 * 5?", "10" }, 
			new String[] { "What is 5 - 1?", "4" });

	public static void loadConfig() {
		File configFile = new File("C:/Users/hanse/Desktop/config.txt");

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

							in.readLine();
							String headerLine = in.readLine();
							String headerAnswer = headerLine.substring(7).trim();
							in.readLine();
							String bodyAnswer = in.readLine();
							
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
