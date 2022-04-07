package serveur;

import graphicLayer.*;
import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Serveur {
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

							Thread msgReaderThread = new MsgReader(environment, writer, reader);
							msgReaderThread.start();

						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) {
		Serveur serv = new Serveur();
		serv.mainLoop();
	}
}
