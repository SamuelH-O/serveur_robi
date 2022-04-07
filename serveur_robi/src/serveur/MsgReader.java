package serveur;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

class MsgReader extends Thread {
		BufferedReader reader;
		PrintWriter writer;
		Environment environment;

		public MsgReader(Environment environment, PrintWriter writer, BufferedReader reader) {
			this.reader = reader;
			this.writer = writer;
			this.environment = environment;
		}

		@Override
		public void run() {
			String line;
			StringBuilder commandStringBuilder = new StringBuilder();
			try {
				while ((line = reader.readLine()) != null) {
					System.out.print(line);
					if (line.endsWith("}")) {
						commandStringBuilder.append(line);
						System.out.println("\n" + commandStringBuilder);
						Message command = Message.fromJson(commandStringBuilder.toString());
						if (command.getType().equals("stopCommand")) {
							// Is a MsgToCommandCall thread running
							writer.println(Message.toJson(new Message("commandDone", "La command à été arrêtée")));
						} else {
							System.out.println(command.getMess());
							MsgToCommandCall MsgToCommandCallThread = new MsgToCommandCall(environment, writer, command);
							MsgToCommandCallThread.start();
						}
						commandStringBuilder = new StringBuilder();
					} else {
						commandStringBuilder.append(line);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
