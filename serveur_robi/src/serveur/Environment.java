package serveur;

import java.util.HashMap;

class Environment {
	HashMap<String, Reference> variables;

	Environment() {
		variables = new HashMap<>();
	}

	void addReference(String name, Reference ref) {
		variables.putIfAbsent(name, ref);
	}

	Reference getReferenceByName(String name) {
		return variables.get(name);
	}
}