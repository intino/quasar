package io.quassar.editor.box.languages;

import io.quassar.archetype.Archetype;
import io.quassar.editor.box.util.SubjectHelper;
import io.quassar.editor.model.Collection;
import io.quassar.editor.model.Language;
import systems.intino.datamarts.subjectstore.SubjectStore;
import systems.intino.datamarts.subjectstore.model.Subject;

import java.time.Instant;
import java.util.List;

public class CollectionManager {
	private final Archetype archetype;
	private final SubjectStore subjectStore;

	public CollectionManager(Archetype archetype, SubjectStore store) {
		this.archetype = archetype;
		this.subjectStore = store;
	}

	public boolean exists(String collection) {
		return !subjectStore.query().isType(SubjectHelper.CollectionType).isRoot().where("name").equals(collection).isEmpty();
	}

	public List<Collection> collections(String owner) {
		return subjectStore.query().isType(SubjectHelper.CollectionType).isRoot().where("owner").equals(owner).collect().stream().map(this::get).toList();
	}

	public Collection create(String name, String owner) {
		Collection collection = new Collection(subjectStore.create(SubjectHelper.collectionPath(name)));
		collection.name(name);
		collection.owner(owner);
		collection.createDate(Instant.now());
		collection.updateDate(Instant.now());
		return collection;
	}

	public Collection get(String key) {
		return get(subjectStore.open(SubjectHelper.collectionPath(key)));
	}

	private Collection get(Subject subject) {
		if (subject == null || subject.isNull()) return null;
		return new Collection(subject);
	}

}
