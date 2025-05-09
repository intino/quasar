package io.quassar.editor;

import systems.intino.datamarts.subjectstore.SubjectStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SubjectStore_ {

	public static void main(String[] args) throws IOException {
		SubjectStore store = new SubjectStore("jdbc:sqlite:/tmp/index.iss");
		File outFile = new File("/tmp/index.dump.txt");
		if (outFile.exists()) outFile.delete();
		FileOutputStream stream = new FileOutputStream("/tmp/index.dump.txt");
		store.dumpIndex(stream);
	}

}
