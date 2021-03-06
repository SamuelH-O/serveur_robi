package serveur;

import graphicLayer.GBounded;
import stree.parser.SNode;

import java.awt.*;

class SetDim implements Command {

	@Override
	public Reference run(Environment environment, Reference receiver, SNode method) {
		try {
			((GBounded) receiver.receiver).setDimension(new Dimension(Integer.parseInt(method.get(2).contents()), Integer.parseInt(method.get(3).contents())));
		} catch (NumberFormatException e) {
			return null;
		}
		return receiver;
	}
}