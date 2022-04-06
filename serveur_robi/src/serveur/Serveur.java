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
						Thread t = new ClientHandler(clientSocket, writer, reader);
						t.start();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private class ClientHandler extends Thread {
		Socket socket;
		PrintWriter writer;
		BufferedReader reader;

		public ClientHandler(Socket socket, PrintWriter writer, BufferedReader reader) {
			this.socket = socket;
			this.writer = writer;
			this.reader = reader;
		}

		@Override
		public void run() {
			try {
				if (reader.readLine().equals("Connected ?")) {
					writer.print("Connected !");
					/*char[] commandCharArray = new char[254];
					StringBuilder commandStringBuilder = new StringBuilder();
					while (reader.read(commandCharArray) != -1) {
						commandStringBuilder.append(commandCharArray);
					}
					Message command = Message.fromJson(commandStringBuilder.toString());*/
					Message command = Message.fromJson(reader.readLine());
					if (!commandRunning) {
						commandRunning = true;
						long startTimeCommand = System.currentTimeMillis();
						// creation du parser
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
						long endTimeCommand = System.currentTimeMillis();
						Message traceMsg = new Message();
						traceMsgBuilder.append(" à pris ").append(endTimeCommand - startTimeCommand).append(" ms");
						traceMsg.setMess(traceMsgBuilder.toString());
						traceMsg.setType("t");
						System.out.print(traceMsg.getMess());
						writer.print(Message.toJson(traceMsg));
						commandRunning = false;
					} else {
						writer.println("Erreur : une commande est déjà en cours d'exécution");
					}
				}
				writer.close();
				reader.close();
				socket.close();
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
