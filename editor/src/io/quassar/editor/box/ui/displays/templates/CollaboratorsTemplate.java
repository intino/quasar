package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;

import java.util.*;
import java.util.function.Consumer;

public class CollaboratorsTemplate extends AbstractCollaboratorsTemplate<EditorBox> {
	private String owner;
	private Set<String> collaboratorSet = new HashSet<>();
	private Consumer<List<String>> changeListener;
	private boolean readonly = false;

	public CollaboratorsTemplate(EditorBox box) {
		super(box);
	}

	public void owner(String owner) {
		this.owner = owner;
	}

	public void collaborators(List<String> collaborators) {
		this.collaboratorSet = new HashSet<>(collaborators);
	}

	public void readonly(boolean value) {
		this.readonly = value;
	}

	public void onChange(Consumer<List<String>> listener) {
		this.changeListener = listener;
	}

	@Override
	public void init() {
		super.init();
		invite.onExecute(e -> invitePeople());
	}

	@Override
	public void refresh() {
		super.refresh();
		invite.readonly(readonly);
		peopleField.value(null);
		ownerField.value(owner);
		collaborators.clear();
		collaboratorSet.forEach(u -> fill(u, collaborators.add()));
	}

	private void fill(String user, CollaboratorItemTemplate display) {
		display.user(user);
		display.onRemove(this::removeUser);
		display.refresh();
	}

	private void removeUser(String user) {
		collaboratorSet.remove(user);
		notifyChange();
		refresh();
	}

	private void invitePeople() {
		List<String> people = Arrays.stream(peopleField.value().split(";?\\n")).map(String::trim).filter(s -> !s.isEmpty()).toList();
		collaboratorSet.addAll(people);
		peopleField.value(null);
		notifyChange();
		refresh();
	}

	private void notifyChange() {
		changeListener.accept(new ArrayList<>(collaboratorSet));
	}

}