package serveur;

import graphicLayer.*;
import stree.parser.SNode;
import stree.parser.SParser;
import tools.Tools;
import javax.imageio.ImageIO;
import java.awt.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Serveur {
	boolean commandRunning = false;
	boolean shouldStop = false;
	Environment environment = new Environment();

	public Serveur() {
		GSpace space = new GSpace("Serveur Robi", new Dimension(200, 100));
		space.open();

		Reference spaceRef = new Reference(space);
		Reference rectClassRef = new Reference(GRect.class);
		Reference ovalClassRef = new Reference(GOval.class);
		Reference imageClassRef = new Reference(GImage.class);
		Reference stringClassRef = new Reference(GString.class);

		spaceRef.addCommand("setColor", new SetColor());
		spaceRef.addCommand("sleep", new Sleep());
		spaceRef.addCommand("setDim", new SetDim());

		spaceRef.addCommand("add", new AddElement());
		spaceRef.addCommand("del", new DelElement());

		rectClassRef.addCommand("new", new NewElement());
		ovalClassRef.addCommand("new", new NewElement());
		imageClassRef.addCommand("new", new NewImage());
		stringClassRef.addCommand("new", new NewString());

		environment.addReference("space", spaceRef);
		environment.addReference("Rect", rectClassRef);
		environment.addReference("Oval", ovalClassRef);
		environment.addReference("Image", imageClassRef);
		environment.addReference("Label", stringClassRef);
	}

	private interface Command {
		// le receiver est l'objet qui va executer method
		// method est la s-expression resultat de la compilation
		// du code source a executer
		// exemple de code source : "(space setColor black)"
		Reference run(Reference receiver, SNode method);
	}

	private class Environment {
		HashMap<String, Reference> variables;
		Environment() {
			variables = new HashMap<>();
		}

		void addReference(String name, Reference ref) {
			variables.putIfAbsent(name, ref);
		}

		Reference getReferenceByName(String name) {
			return variables.get(name);
		}
	}

	@SuppressWarnings("InnerClassMayBeStatic")
	private class Reference {
		Object receiver;
		Map<String, Command> primitives;
		Reference(Object receiver) {
			this.receiver = receiver;
			primitives = new HashMap<>();
		}

		Command getCommandByName(String selector) {
			return primitives.get(selector);
		}

		void addCommand(String selector, Command primitive) {
			primitives.put(selector, primitive);
		}

		Reference run(SNode expr) {
			Command cmd = getCommandByName(expr.get(1).contents());
			return cmd.run(this, expr);
		}

		Object getReceiver() {
			return receiver;
		}
	}

	private class Interpreter {
		String compute(Environment environment, SNode method) {
			if(method.get(1).contents().equals("add")) {
				Reference newRef = environment.getReferenceByName(method.get(3).get(0).contents());
				newRef = newRef.run(method.get(3));
				environment.addReference(method.get(2).contents(), newRef);
			}
			// quel est le receiver
			Reference receiver = environment.getReferenceByName(method.get(0).contents());
			if(receiver == null) {
				return method.get(0).contents() + " n'est pas connu";
			} else {
				// demande au receiver d'exécuter la s-expression compilée
				receiver.run(method);
				return null;
			}
		}
	}

	private class Translate implements Command {
		@Override
		public Reference run(Reference receiver, SNode method) {
			((GElement) receiver.receiver).translate(new Point(Integer.parseInt(method.get(2).contents()),Integer.parseInt(method.get(3).contents())));
			return receiver;
		}
	}

	private class SetColor implements Command {
		@Override
		public Reference run(Reference receiver, SNode method) {
			if (method.get(0).contents().equals("space")) {
				((GSpace) receiver.receiver).setColor(Tools.getColorByName(method.get(2).contents()));
			} else {
				((GElement) receiver.receiver).setColor(Tools.getColorByName(method.get(2).contents()));
			}
			return receiver;
		}
	}

	private class Sleep implements Command {
		@Override
		public Reference run(Reference receiver, SNode method) {
			Tools.sleep(Integer.parseInt(method.get(2).contents()));
			return receiver;
		}
	}

	private class NewElement implements Command {
		public Reference run(Reference receiver, SNode method) {
			try {
				@SuppressWarnings("unchecked")
				GElement e = ((Class<GElement>) receiver.receiver).getDeclaredConstructor().newInstance();
				Reference ref = new Reference(e);
				ref.addCommand("setColor", new SetColor());
				ref.addCommand("translate", new Translate());
				ref.addCommand("setDim", new SetDim());
				return ref;
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	private class NewImage implements Command {
		@Override
		public Reference run(Reference receiver, SNode method) {
			try{
				@SuppressWarnings("unchecked")
				GElement e = ((Class<GImage>) receiver.receiver).getDeclaredConstructor(Image.class).newInstance(ImageIO.read(new File("..\\" + method.get(2).contents())));
				Reference ref = new Reference(e);
				ref.addCommand("translate", new Translate());
				return ref;
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException
					| IOException e) {
				e.printStackTrace();
			}
			return receiver;
		}
	}

	private class NewString implements Command {
		@Override
		public Reference run(Reference receiver, SNode method) {
			try{
				@SuppressWarnings("unchecked")
				GElement e = ((Class<GString>) receiver.receiver).getDeclaredConstructor(String.class).newInstance(method.get(2).contents());
				Reference ref = new Reference(e);
				ref.addCommand("setColor", new SetColor());
				ref.addCommand("translate", new Translate());
				ref.addCommand("setDim", new SetDim());
				return ref;
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
			return receiver;
		}
	}

	private class SetDim implements Command {
		@Override
		public Reference run(Reference receiver, SNode method) {
			((GBounded) receiver.receiver).setDimension(new Dimension(Integer.parseInt(method.get(2).contents()), Integer.parseInt(method.get(3).contents())));
			return receiver;
		}
	}

	private class AddElement implements Command {
		@Override
		public Reference run(Reference receiver, SNode method) {
			((GSpace) receiver.receiver).addElement((GElement) environment.getReferenceByName(method.get(2).contents()).getReceiver());
			return receiver;
		}
	}

	private class DelElement implements Command {
		@Override
		public Reference run(Reference receiver, SNode method) {
			((GSpace) receiver.receiver).removeElement((GElement) environment.getReferenceByName(method.get(2).contents()).getReceiver());
			return receiver;
		}
	}

	private void mainLoop() {
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(8000);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (serverSocket != null) {
			//noinspection InfiniteLoopStatement
			while (true) {
				Socket clientSocket;
				try {
					clientSocket = serverSocket.accept();
					if (clientSocket != null) {
						PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
						BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

						final Message handshakeGreet = new Message("handshake", "Connected ?");
						final Message handshakeResponse = new Message("handshake", "Connected !");

						if (reader.readLine().equals(Message.toJson(handshakeGreet))) {
							writer.println(Message.toJson(handshakeResponse));
							System.out.println("Handshake successfully executed");

							Thread msgReaderThread = new MsgReader(writer, reader);
							msgReaderThread.start();

						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private class CommandHandler extends Thread {
		PrintWriter writer;
		Message command;

		CommandHandler(PrintWriter writer, Message command) {
			this.writer = writer;
			this.command = command;
		}

		@Override
		public void run() {
			if (!commandRunning) {
				if (command.getType().equals("command")) {
					commandRunning = true;
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
						if (!shouldStop) {
							Interpreter i = new Interpreter();
							s = i.compute(environment, sNode);
							if (s != null) {
								traceMsgBuilder.append("Erreur : ").append(s);
							}
						} else {
							writer.println(Message.toJson(new Message("commandDone", "La command à été arrêtée")));
							shouldStop = false;
							return;
						}
					}
					Message traceMsg = new Message("trace", traceMsgBuilder.toString());
					System.out.print("msg trace envoyé : " + traceMsg.getMess());
					writer.println(Message.toJson(traceMsg));

					long endTimeCommand = System.currentTimeMillis();
					writer.println(Message.toJson(new Message("commandDone", "La commande à pris " + (endTimeCommand - startTimeCommand) + " ms")));
					commandRunning = false;
				} else {
					writer.println(Message.toJson(new Message("commandDone", "Erreur type de message pas reconnu")));
				}
			} else {
				writer.println(Message.toJson(new Message("trace", "Erreur : une commande est déjà en cours d'exécution")));
			}
		}
	}

	private class MsgReader extends Thread {
		BufferedReader reader;
		PrintWriter writer;

		public MsgReader(PrintWriter writer, BufferedReader reader) {
			this.reader = reader;
			this.writer = writer;
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
							shouldStop = true;
						} else {
							System.out.println(command.getMess());
							CommandHandler cmdHandlerThread = new CommandHandler(writer, command);
							cmdHandlerThread.start();
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

	public static void main(String[] args) {
		Serveur serv = new Serveur();
		serv.mainLoop();
	}
}
