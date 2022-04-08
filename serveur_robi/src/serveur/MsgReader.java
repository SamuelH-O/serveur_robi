package serveur;

import tools.Tools;

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
			MsgToCommandCall msgToCommandCallThread = null;
			try {
				System.out.println("Before while");
				while ((line = reader.readLine()) != null) {
					System.out.println("After while");
					if (line.endsWith("}")) {
						commandStringBuilder.append(line);
						Message command = Message.fromJson(commandStringBuilder.toString());
						if (command.getType().equals("stopCommand")) {
							assert msgToCommandCallThread != null;
							if (msgToCommandCallThread.isAlive()) {
								msgToCommandCallThread.interrupt();
								msgToCommandCallThread.stopAtNextCommand();
							}
							Tools.sleep(500);
							writer.println(Message.toJson(new Message("commandDone", "La command à été arrêtée")));
						} else {
							msgToCommandCallThread = new MsgToCommandCall(environment, writer, command);
							msgToCommandCallThread.start();
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
