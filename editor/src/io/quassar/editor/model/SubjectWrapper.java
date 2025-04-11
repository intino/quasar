package io.quassar.editor.model;

import systems.intino.datamarts.subjectindex.model.Subject;
import systems.intino.datamarts.subjectindex.model.Tokens;

import java.util.ArrayList;
import java.util.List;

public class SubjectWrapper {
	protected transient final Subject subject;

	public SubjectWrapper(Subject subject) {
		this.subject = subject;
	}

	protected List<String> getList(String name) {
		List<String> result = new ArrayList<>();
		subject.tokens().get(name).forEach(result::add);
		return result;
	}

	protected String get(String name) {
		Tokens.Values values = subject.tokens().get(name);
		return values.iterator().hasNext() ? values.first() : null;
	}

	protected void set(String name, String value) {
		if (value == null) return;
		subject.update().set(name, value).commit();
	}

	protected void put(String name, String value) {
		subject.update().put(name, value).commit();
	}

	protected void putList(String name, List<String> values) {
		Subject.Transaction feed = subject.update();
		feed.del(name);
		values.forEach(v -> feed.put(name, v));
		feed.commit();
	}

}
