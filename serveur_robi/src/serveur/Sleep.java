package serveur;

import stree.parser.SNode;
import tools.Tools;

class Sleep extends Thread implements Command {

	@Override
	public Reference run(Environment environment, Reference receiver, SNode method) {
		Tools.sleep(Integer.parseInt(method.get(2).contents()));
		return receiver;
	}
}