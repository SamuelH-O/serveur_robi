package serveur;

import stree.parser.SNode;

class Interpreter {

	String compute(Environment environment, SNode method) {

		if(method.get(1).contents().equals("add")) {
			Reference newRef = environment.getReferenceByName(method.get(3).get(0).contents());
			newRef = newRef.run(environment, method.get(3));
			environment.addReference(method.get(2).contents(), newRef);
		}

		// quel est le receiver
		Reference receiver = environment.getReferenceByName(method.get(0).contents());
		if(receiver == null) {
			return method.get(0).contents() + " n'est pas connu";
		} else {
			// demande au receiver d'exécuter la s-expression compilée
			receiver.run(environment, method);
			return null;
		}
	}
}
