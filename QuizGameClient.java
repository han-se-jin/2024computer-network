import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class QuizGameClient {
	private static String IP = "localhost";
	private static int PORT = 1234;

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

		System.out.println("Connecting to IP: " + IP + ", Port: " + PORT);

		try {
			Socket socket = new Socket(IP, PORT);
			System.out.println("Connected.");

			try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
					BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))) {

				String line;
				while ((line = in.readLine()) != null) {
					if (!line.startsWith("HTTP/1.1")) {
						continue;
					}

					String header = in.readLine();

					in.readLine();

					String body = in.readLine();
					
					if(line.contains("400 Bad Request")) {
						System.out.println("Error: Invalid answer format.");
						continue;
					}

					if (header.contains("Question-Number")) {
						System.out.println("Question: " + body);
						System.out.print("Enter your answer: ");
						String answer = userInput.readLine();
						out.write("HTTP/1.1\r\n" +
						          "ANSWER: " + answer + "\r\n" +
								  "\r\n" +
						          answer + "\r\n");
						out.flush();
					} else if (header.contains("Result:")) {
						System.out.println(body);
					} else if (header.contains("Total-Score")) {
						System.out.println("Total Score: " + body);
						break;
					}
				}
			}
		} catch (IOException e) {
			System.out.println("Disconnected." + e.getMessage());
		}
	}
}
