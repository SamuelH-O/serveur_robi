package serveur;

import graphicLayer.GElement;
import graphicLayer.GSpace;
import stree.parser.SNode;
import tools.Tools;

class SetColor implements Command {

	@Override
	public Reference run(Environment environment, Reference receiver, SNode method) {
		if (method.get(0).contents().equals("space")) {
			((GSpace) receiver.receiver).setColor(Tools.getColorByName(method.get(2).contents()));
		} else {
			((GElement) receiver.receiver).setColor(Tools.getColorByName(method.get(2).contents()));
		}
		return receiver;
	}
}
