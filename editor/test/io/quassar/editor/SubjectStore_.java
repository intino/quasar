package io.quassar.editor;

import systems.intino.datamarts.subjectstore.SubjectStore;
import systems.intino.datamarts.subjectstore.model.Subject;
import systems.intino.datamarts.subjectstore.model.Term;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class SubjectStore_ {
/*
	public static void main(String[] args) throws IOException {
		SubjectStore store = new SubjectStore("jdbc:sqlite:/tmp/index.iss");
		store.subjects().collect().forEach(s -> fix(s));
		File outFile = new File("/tmp/index.dump.txt");
		if (outFile.exists()) outFile.delete();
		FileOutputStream stream = new FileOutputStream("/tmp/index.dump.txt");
		store.dumpIndex(stream);
	}

	private static void fix(Subject subject) {
		List<Term> termList = subject.terms().stream().filter(t -> t.value() == null || t.value().isEmpty()).toList();
		if (termList.isEmpty()) return;
		Subject.Updating index = subject.index();
		termList.forEach(t -> index.del(t.tag()));
		index.terminate();
	}
*/
	public static void main(String[] args) throws IOException {
		SubjectStore store = new SubjectStore(new File("/tmp/index.triples"));
		Subject subject = store.open("yYAr2Dze.model");
		subject.update().set("name", "bbbb");
		store.seal();
		System.out.println(subject.get("name"));
	}

}
