package serveur;

import stree.parser.SNode;

interface Command {
	// le receiver est l'objet qui va executer method
	// method est la s-expression resultat de la compilation
	// du code source a executer
	// exemple de code source : "(space setColor black)"
	Reference run(Environment environment, Reference receiver, SNode method);
}
