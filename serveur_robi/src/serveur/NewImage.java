package serveur;

import graphicLayer.GElement;
import graphicLayer.GImage;
import stree.parser.SNode;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

class NewImage extends Thread implements Command {

		@Override
		public Reference run(Environment environment, Reference receiver, SNode method) {
			try{
				@SuppressWarnings("unchecked")
				GElement e = ((Class<GImage>) receiver.receiver).getDeclaredConstructor(Image.class).newInstance(ImageIO.read(new File("..\\" + method.get(2).contents())));
				Reference ref = new Reference(e);
				ref.addCommand("translate", new Translate());
				return ref;
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException
					| IOException e) {
				e.printStackTrace();
			}
			return receiver;
		}
	}
