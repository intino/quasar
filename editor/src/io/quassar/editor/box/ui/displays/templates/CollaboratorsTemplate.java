package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.events.AddCollectionItemEvent;
import io.intino.alexandria.ui.displays.events.SelectionEvent;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.ui.datasources.CollaboratorsDatasource;
import io.quassar.editor.box.ui.displays.items.CollaboratorSelectorItem;
import io.quassar.editor.model.Model;
import io.quassar.editor.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CollaboratorsTemplate extends AbstractCollaboratorsTemplate<EditorBox> {
	private Model model;
	private Consumer<List<User>> changeListener;
	private List<User> collaboratorList = new ArrayList<>();

	public CollaboratorsTemplate(EditorBox box) {
		super(box);
	}

	public void model(Model model) {
		this.model = model;
		this.collaboratorList = model.collaborators().stream().map(c -> box().userManager().get(c)).filter(Objects::nonNull).collect(Collectors.toList());
	}

	public void onChange(Consumer<List<User>> listener) {
		this.changeListener = listener;
	}

	@Override
	public void init() {
		super.init();
		collaboratorSelectorList.onAddItem(this::refresh);
		collaboratorSelector.onSelect(this::addCollaborator);
	}

	@Override
	public void refresh() {
		super.refresh();
		noCollaboratorsBlock.visible(collaboratorList.isEmpty());
		collaboratorsBlock.visible(!collaboratorList.isEmpty());
		collaborators.clear();
		collaboratorList.forEach(u -> fill(u, collaborators.add()));
		refreshCollaboratorsSelector();
	}

	private void refreshCollaboratorsSelector() {
		collaboratorSelectorList.source(new CollaboratorsDatasource(box(), session(), collaboratorList));
		collaboratorSelectorList.reload();
	}

	private void addCollaborator(SelectionEvent event) {
		if (event.selection().isEmpty()) return;
		addCollaborator((User)event.first());
	}

	private void addCollaborator(User user) {
		collaboratorList.stream().filter(u -> u.name().equals(user.name())).findFirst().ifPresent(collaboratorList::remove);
		collaboratorList.add(user);
		changeListener.accept(collaboratorList);
		refresh();
	}

	private void fill(User user, CollaboratorItemTemplate display) {
		display.user(user);
		display.onRemove(this::removeUser);
		display.refresh();
	}

	private void removeUser(User user) {
		collaboratorList.stream().filter(u -> u.name().equals(user.name())).findFirst().ifPresent(collaboratorList::remove);
		changeListener.accept(collaboratorList);
		refresh();
	}

	private void refresh(AddCollectionItemEvent event) {
		User user = event.item();
		CollaboratorSelectorItem display = event.component();
		display.name.value(user.name());
	}

}