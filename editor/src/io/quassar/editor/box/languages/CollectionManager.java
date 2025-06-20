package io.quassar.editor.box.languages;

import io.quassar.archetype.Archetype;
import io.quassar.editor.box.util.SubjectHelper;
import io.quassar.editor.model.*;
import systems.intino.datamarts.subjectstore.SubjectStore;
import systems.intino.datamarts.subjectstore.model.Subject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
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

	public List<License> anyLicenses(String owner) {
		return subjectStore.query().isType(SubjectHelper.LicenseType).where("user").equals(owner).collect().stream().map(this::getLicense).toList();
	}

	public License anyLicense(String collection, String owner) {
		Subject subject = subjectStore.open(SubjectHelper.collectionPath(collection));
		if (subject.isNull()) return null;
		return subject.children().where("user").equals(owner).stream().map(this::getLicense).findFirst().orElse(null);
	}

	public List<License> licenses(String owner) {
		return anyLicenses(owner).stream().filter(l -> !l.isExpired()).toList();
	}

	public List<Collection> collections(String owner) {
		if (owner == null) return Collections.emptyList();
		List<Subject> result = new ArrayList<>(subjectStore.query().isType(SubjectHelper.CollectionType).isRoot().where("owner").equals(owner).collect());
		result.addAll(subjectStore.query().isType(SubjectHelper.CollectionType).isRoot().where("collaborator").contains(owner).collect());
		return result.stream().map(this::get).toList();
	}

	public Collection create(String name, Collection.SubscriptionPlan plan, String owner) {
		Collection collection = new Collection(subjectStore.create(SubjectHelper.collectionPath(name)));
		collection.name(name);
		collection.owner(owner);
		collection.subscriptionPlan(plan);
		collection.createDate(Instant.now());
		collection.updateDate(Instant.now());
		return collection;
	}

	public License createLicense(Collection collection, String code, int duration) {
		License license = new License(subjectStore.create(SubjectHelper.pathOf(collection, code)));
		license.status(License.Status.Created);
		license.createDate(Instant.now());
		license.code(code);
		license.duration(duration);
		return license;
	}

	public void remove(Collection collection, String owner) {
		subjectStore.open(SubjectHelper.pathOf(collection)).drop();
	}

	public Collection get(String key) {
		return get(subjectStore.open(SubjectHelper.collectionPath(key)));
	}

	public License getLicense(String license) {
		Subject subject = subjectStore.query().isType(SubjectHelper.LicenseType).where("code").equals(license).collect().stream().findFirst().orElse(null);
		return subject != null ? getLicense(subject) : null;
	}

	public void assign(License license, String user) {
		license.status(License.Status.Assigned);
		license.assignDate(Instant.now());
		license.user(user);
	}

	private Collection get(Subject subject) {
		if (subject == null || subject.isNull()) return null;
		return new Collection(subject);
	}

	private License getLicense(Subject subject) {
		if (subject == null || subject.isNull()) return null;
		return new License(subject);
	}

}
