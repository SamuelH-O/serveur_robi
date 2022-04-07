package serveur;

import stree.parser.SNode;
import stree.parser.SParser;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

class MsgToCommandCall extends Thread {
	PrintWriter writer;
	Message command;
	Environment environment;

	MsgToCommandCall(Environment environment, PrintWriter writer, Message command) {
		this.writer = writer;
		this.command = command;
		this.environment = environment;
	}

	@Override
	public void run() {
		if (command.getType().equals("command")) {
			long startTimeCommand = System.currentTimeMillis();

			// création du parser
			SParser<SNode> parser = new SParser<>();

			// compilation
			List<SNode> compiled = null;
			try {
				compiled = parser.parse(command.getMess());
			} catch (IOException e) {
				e.printStackTrace();
			}

			// execution des s-expressions compilées
			assert compiled != null;
			StringBuilder traceMsgBuilder = new StringBuilder();
			traceMsgBuilder.append(command.getMess()).append(" ");
			String s;
			for (SNode sNode : compiled) {
				Interpreter i = new Interpreter();
				s = i.compute(environment, sNode);
				if (s != null) {
					traceMsgBuilder.append("Erreur : ").append(s);
				}
			}
			Message traceMsg = new Message("trace", traceMsgBuilder.toString());
			System.out.print("msg trace envoyé : " + traceMsg.getMess());
			writer.println(Message.toJson(traceMsg));

			long endTimeCommand = System.currentTimeMillis();
			writer.println(Message.toJson(new Message("commandDone", "La commande à pris " + (endTimeCommand - startTimeCommand) + " ms")));
		} else {
			writer.println(Message.toJson(new Message("commandDone", "Erreur type de message pas reconnu")));
		}
	}
}
