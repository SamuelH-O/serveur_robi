package serveur;

import graphicLayer.GElement;
import graphicLayer.GSpace;
import stree.parser.SNode;

class AddElement implements Command {

	@Override
	public Reference run(Environment environment, Reference receiver, SNode method) {
		((GSpace) receiver.receiver).addElement((GElement) environment.getReferenceByName(method.get(2).contents()).getReceiver());
		return receiver;
	}
}
