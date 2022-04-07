package serveur;

import graphicLayer.GElement;
import graphicLayer.GString;
import stree.parser.SNode;

import java.lang.reflect.InvocationTargetException;

class NewString extends Thread implements Command {

	@Override
	public Reference run(Environment environment, Reference receiver, SNode method) {
		try{
			@SuppressWarnings("unchecked")
			GElement e = ((Class<GString>) receiver.receiver).getDeclaredConstructor(String.class).newInstance(method.get(2).contents());
			Reference ref = new Reference(e);
			ref.addCommand("setColor", new SetColor());
			ref.addCommand("translate", new Translate());
			ref.addCommand("setDim", new SetDim());
			return ref;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		return receiver;
	}
}
