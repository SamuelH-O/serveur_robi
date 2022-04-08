package serveur;

import graphicLayer.GElement;
import graphicLayer.GSpace;
import stree.parser.SNode;
import tools.Tools;

class SetColor implements Command {

	@Override
	public Reference run(Environment environment, Reference ref, SNode method) {
		if (Tools.getColorByName(method.get(2).contents()) == null) {
			return null;
		}
		if (method.get(0).contents().equals("space")) {
			((GSpace) ref.getReceiver()).setColor(Tools.getColorByName(method.get(2).contents()));
		} else {
			((GElement) ref.getReceiver()).setColor(Tools.getColorByName(method.get(2).contents()));
		}
		return ref;
	}
}
