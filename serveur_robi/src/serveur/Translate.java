package serveur;

import graphicLayer.GElement;
import stree.parser.SNode;

import java.awt.*;

class Translate implements Command {

	@Override
	public Reference run(Environment environment, Reference receiver, SNode method) {
		((GElement) receiver.receiver).translate(new Point(Integer.parseInt(method.get(2).contents()),Integer.parseInt(method.get(3).contents())));
		return receiver;
	}
}
