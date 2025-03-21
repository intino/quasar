package io.quassar.editor.box;

public class Main {
	public static void main(String[] args) {
		EditorBox box = new EditorBox(args);
		box.start();
		Runtime.getRuntime().addShutdownHook(new Thread(box::stop));
	}
}