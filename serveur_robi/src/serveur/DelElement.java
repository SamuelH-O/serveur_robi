package serveur;

import graphicLayer.GElement;
import graphicLayer.GSpace;
import stree.parser.SNode;

class DelElement extends Thread implements Command {

	@Override
	public Reference run(Environment environment, Reference receiver, SNode method) {
		if (environment.getReferenceByName(method.get(2).contents()).getReceiver() == null) {
			return null;
		}
		((GSpace) receiver.receiver).removeElement((GElement) environment.getReferenceByName(method.get(2).contents()).getReceiver());
		return receiver;
	}
}
