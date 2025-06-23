package io.quassar.editor.model;

import systems.intino.datamarts.subjectstore.model.Subject;

import java.time.Instant;
import java.time.ZoneId;

public class License extends SubjectWrapper {

	public License(Subject subject) {
		super(subject);
	}

	public String code() {
		return get("code");
	}

	public void code(String value) {
		set("code", value);
	}

	public String user() {
		return get("user");
	}

	public void user(String value) {
		set("user", value);
	}

	public Collection collection() {
		return new Collection(subject.parent());
	}

	public enum Status { Created, Assigned, Revoked }
	public Status status() {
		return Status.valueOf(get("status"));
	}

	public void status(Status value) {
		set("status", value.name());
	}

	public int duration() {
		return Integer.parseInt(get("duration"));
	}

	public void duration(int value) {
		set("duration", String.valueOf(value));
	}

	public Instant createDate() {
		return Instant.parse(get("create-date"));
	}

	public void createDate(Instant createDate) {
		set("create-date", createDate.toString());
	}

	public Instant assignDate() {
		String value = get("assign-date");
		return value != null ? Instant.parse(value) : null;
	}

	public void assignDate(Instant value) {
		set("assign-date", value != null ? value.toString() : null);
	}

	public Instant expireDate() {
		if (assignDate() == null) return null;
		if (duration() == -1) return null;
		return assignDate().atZone(ZoneId.systemDefault()).plusMonths(duration()).toInstant();
	}

	public boolean isExpired() {
		if (status() == Status.Revoked) return true;
		Instant expireDate = expireDate();
		return expireDate == null || expireDate.isBefore(Instant.now());
	}

}
