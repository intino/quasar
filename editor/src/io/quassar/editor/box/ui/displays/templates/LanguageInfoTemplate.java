package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.events.SelectionEvent;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.LanguageCommands;
import io.quassar.editor.box.util.ModelHelper;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageProperty;
import io.quassar.editor.model.Model;
import io.quassar.editor.model.Visibility;

import java.util.Arrays;
import java.util.List;

public class LanguageInfoTemplate extends AbstractLanguageInfoTemplate<EditorBox> {
	private Language language;
	private String release;

	public LanguageInfoTemplate(EditorBox box) {
		super(box);
	}

	public void language(Language language) {
		this.language = language;
	}

	public void release(String release) {
		this.release = release;
	}

	@Override
	public void init() {
		super.init();
		editorStamp.onChangeName(this::rename);
		editorStamp.onChangeLogo(e -> save(LanguageProperty.Logo, e));
		grantAccessField.onChange(e -> save(LanguageProperty.GrantAccess, grantAccessList()));
		licenseField.onChange(e -> save(LanguageProperty.License, licenseField.value()));
		visibilitySelector.onSelect(this::updateVisibility);
	}

	@Override
	public void refresh() {
		super.refresh();
		Model metamodel = box().modelManager().get(language.metamodel());
		editorStamp.language(language);
		editorStamp.refresh();
		metamodelLink.title(ModelHelper.label(metamodel, language(), box()));
		metamodelLink.site(PathHelper.modelUrl(metamodel, release, session()));
		visibilitySelector.selection(language.isPrivate() ? "privateVisibilityOption" : "publicVisibilityOption");
		publicVisibilityBlock.visible(language.isPublic());
		grantAccessField.value(String.join("; ", language.grantAccessList()));
		licenseField.value(language.license());
	}

	private List<String> grantAccessList() {
		return Arrays.stream(grantAccessField.value().split(";")).map(String::trim).filter(s -> !s.isEmpty()).toList();
	}

	private void rename(String newName) {
		box().commands(LanguageCommands.class).rename(language, newName, username());
	}

	private void save(LanguageProperty property, Object value) {
		box().commands(LanguageCommands.class).save(language, property, value, username());
	}

	private void updateVisibility(SelectionEvent event) {
		List<String> selection = event.selection();
		boolean isPrivate = selection.isEmpty() || selection.getFirst().equals("privateVisibilityOption");
		box().commands(LanguageCommands.class).save(language, LanguageProperty.Visibility, isPrivate ? Visibility.Private : Visibility.Public, username());
		refresh();
	}

}