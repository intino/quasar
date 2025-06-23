package io.quassar.editor.model;

import io.quassar.editor.box.util.SubjectHelper;
import systems.intino.datamarts.subjectstore.model.Subject;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

public class Collection extends SubjectWrapper {

	public Collection(Subject subject) {
		super(subject);
	}

	public String id() {
		return subject.name();
	}

	public String name() {
		return getOrEmpty("name");
	}

	public void name(String value) {
		set("name", value);
	}

	public String owner() {
		return owner(subject);
	}

	public static String owner(Subject subject) {
		return subject.get("owner");
	}

	public void owner(String owner) {
		set("owner", owner);
	}

	public Instant createDate() {
		String value = get("create-date");
		return value != null ? Instant.parse(value) : null;
	}

	public void createDate(Instant date) {
		set("create-date", date.toString());
	}

	public Instant updateDate() {
		String value = get("update-date");
		return value != null ? Instant.parse(value) : null;
	}

	public void updateDate(Instant date) {
		set("update-date", date.toString());
	}

	public List<String> collaborators() {
		return getList("collaborator");
	}

	public void collaborators(List<String> values) {
		putList("collaborator", values);
	}

	public void add(String collaborator) {
		put("collaborator", collaborator);
	}

	public enum SubscriptionPlan { Professional, Enterprise }
	public SubscriptionPlan subscriptionPlan() {
		String name = get("subscription-plan");
		return name != null ? SubscriptionPlan.valueOf(name) : SubscriptionPlan.Professional;
	}

	public void subscriptionPlan(SubscriptionPlan value) {
		set("subscription-plan", value.name());
	}

	public List<License> licenses() {
		Stream<Subject> result = subject.children().collect().stream().filter(s -> s.is(SubjectHelper.LicenseType));
		return result.map(this::licenseOf).toList();
	}

	public List<License> licenses(String username) {
		Stream<Subject> result = subject.children().collect().stream().filter(s -> s.is(SubjectHelper.LicenseType));
		return result.map(this::licenseOf).filter(l -> username.equals(l.user())).toList();
	}

	public License license(String code) {
		return licenses().stream().filter(r -> r.code().equals(code)).findFirst().orElse(null);
	}

	public License anyLicense(String username) {
		return licenses().stream().filter(r -> username.equals(r.user())).findFirst().orElse(null);
	}

	public List<License> validLicenses() {
		return licenses().stream().filter(r -> !r.isExpired()).toList();
	}

	public License validLicense(String username) {
		return licenses().stream().filter(r -> username.equals(r.user()) && !r.isExpired()).findFirst().orElse(null);
	}

	private License licenseOf(Subject subject) {
		return new License(subject);
	}

}
