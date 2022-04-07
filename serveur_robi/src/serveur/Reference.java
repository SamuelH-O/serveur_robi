package serveur;

import stree.parser.SNode;

import java.util.HashMap;
import java.util.Map;

class Reference {
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

	Reference run(Environment environment, SNode expr) {
		Command cmd = getCommandByName(expr.get(1).contents());
		return cmd.run(environment,this, expr);
	}

	Object getReceiver() {
		return receiver;
	}
}
