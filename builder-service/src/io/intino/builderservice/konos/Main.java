package io.intino.builderservice.konos;

public class Main {
	public static void main(String[] args) {
		BuilderServiceBox box = new BuilderServiceBox(args);
		box.start();
		Runtime.getRuntime().addShutdownHook(new Thread(box::stop));
	}
}