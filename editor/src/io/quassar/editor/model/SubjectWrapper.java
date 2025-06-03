package io.quassar.editor.model;

import io.quassar.editor.box.util.SubjectHelper;
import systems.intino.datamarts.subjectstore.model.Subject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubjectWrapper {
	protected transient final Subject subject;
	private final Map<String, String> cache = new HashMap<>();
	private final Map<String, List<String>> listCache = new HashMap<>();

	public SubjectWrapper(Subject subject) {
		this.subject = subject;
	}

	protected List<String> getList(String name) {
		if (listCache.containsKey(name)) return listCache.get(name);
		List<String> result = new ArrayList<>(SubjectHelper.terms(subject, name));
		listCache.put(name, result);
		return result;
	}

	protected String getOrEmpty(String name) {
		return get(name) != null ? get(name) : "";
	}

	protected String get(String name) {
		if (cache.containsKey(name)) return cache.get(name);
		List<String> terms = SubjectHelper.terms(subject, name);
		cache.put(name, !terms.isEmpty() ? terms.getFirst() : null);
		return cache.get(name);
	}

	protected void set(String name, String value) {
		cache.remove(name);
		if (value == null) return;
		subject.update().set(name, value);
	}

	protected void put(String name, String value) {
		cache.remove(name);
		subject.update().put(name, value);
	}

	protected void del(String name, String value) {
		cache.remove(name);
		subject.update().del(name, value);
	}

	protected void putList(String name, List<String> values) {
		listCache.remove(name);
		Subject.Updating updating = subject.update();
		updating.del(name);
		values.forEach(v -> updating.put(name, v));
	}

}
