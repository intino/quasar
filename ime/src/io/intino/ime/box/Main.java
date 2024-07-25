package io.intino.ime.box;

public class Main {
	public static void main(String[] args) {
		ImeBox box = new ImeBox(args);
		box.start();
		Runtime.getRuntime().addShutdownHook(new Thread(box::stop));
	}
}