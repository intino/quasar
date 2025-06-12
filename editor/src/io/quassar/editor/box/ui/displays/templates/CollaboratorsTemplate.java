package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.model.Model;

import java.util.*;
import java.util.function.Consumer;

public class CollaboratorsTemplate extends AbstractCollaboratorsTemplate<EditorBox> {
	private Model model;
	private Consumer<List<String>> changeListener;
	private Set<String> collaboratorSet = new HashSet<>();

	public CollaboratorsTemplate(EditorBox box) {
		super(box);
	}

	public void model(Model model) {
		this.model = model;
		this.collaboratorSet = new HashSet<>(model.collaborators());
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
		peopleField.value(null);
		ownerField.value(model.owner());
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