package serveur;

import stree.parser.SNode;

class Interpreter {

	String compute(Environment environment, SNode method) {

		if(method.get(1).contents().equals("add")) {
			if (environment.getReferenceByName(method.get(3).get(0).contents()) == null) {
				return "Erreur : " + method.get(3).get(0).contents() + " n'est pas connu ";
			}
			Reference newRef = environment.getReferenceByName(method.get(3).get(0).contents());
			newRef = newRef.run(environment, method.get(3));
			environment.addReference(method.get(2).contents(), newRef);
		}

		// quel est le receiver
		Reference ref = environment.getReferenceByName(method.get(0).contents());
		if(ref == null) {
			return "Erreur : " + method.get(0).contents() + " n'est pas connu ";
		} else if (ref.getCommandByName(method.get(1).contents()) == null) {
			return "Erreur : " + method.get(1).contents() + " n'est pas connu ";
		} else {
			// demande au receiver d'exécuter la s-expression compilée
			if (ref.run(environment, method) == null) {
				return "Erreur : 3éme partie de la commande n'est pas correct ";
			}
			return null;
		}
	}
}
