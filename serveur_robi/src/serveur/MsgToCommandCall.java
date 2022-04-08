package serveur;

import stree.parser.SNode;
import stree.parser.SParser;
import stree.parser.SPrinter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

class MsgToCommandCall extends Thread {
	PrintWriter writer;
	Message command;
	Environment environment;
	boolean exit = false;

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
			String s;
			SPrinter printer = new SPrinter();
			for (SNode sNode : compiled) {
				Interpreter i = new Interpreter();
				s = i.compute(environment, sNode);
				sNode.accept(printer);
				traceMsgBuilder.append(printer.result().toString()).append("\n");
				printer = new SPrinter();
				if (s != null) {
					traceMsgBuilder.append(s);
				}
				if (exit) {
					Message traceMsg = new Message("trace", traceMsgBuilder.toString());
					writer.println(Message.toJson(traceMsg));
					return;
				}
			}
			Message traceMsg = new Message("trace", traceMsgBuilder.toString());
			System.out.println("msg trace envoyé : " + Message.toJson(traceMsg));
			writer.println(Message.toJson(traceMsg));

			long endTimeCommand = System.currentTimeMillis();
			Message commandDone = new Message("commandDone", "La commande à pris " + (endTimeCommand - startTimeCommand) + " ms" + "\n");
			writer.println(Message.toJson(commandDone));
			System.out.println("msg commandDone envoyé : " + Message.toJson(commandDone));
		} else {
			writer.println(Message.toJson(new Message("commandDone", "Erreur type de message pas reconnu")));
		}
	}

	public void stopAtNextCommand() {
		this.exit = true;
	}
}
