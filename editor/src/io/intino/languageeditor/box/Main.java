package io.intino.languageeditor.box;

public class Main {
	public static void main(String[] args) {
		LanguageEditorBox box = new LanguageEditorBox(args);
		box.start();
		Runtime.getRuntime().addShutdownHook(new Thread(box::stop));
	}
}