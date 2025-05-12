package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.events.SelectionEvent;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.LanguageCommands;
import io.quassar.editor.box.ui.types.ForgeView;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageProperty;
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
		generalBlock.onInit(e -> initGeneralBlock());
		generalBlock.onShow(e -> refreshGeneralBlock());
		visibilityBlock.onInit(e -> initVisibilityBlock());
		visibilityBlock.onShow(e -> refreshVisibilityBlock());
	}

	@Override
	public void refresh() {
		super.refresh();
		refreshView();
	}

	private void initGeneralBlock() {
		editorStamp.onChangeId(this::rename);
		editorStamp.onChangeLogo(e -> save(LanguageProperty.Logo, e));
	}

	private void refreshGeneralBlock() {
		editorStamp.language(language);
		editorStamp.refresh();
		refreshProperties();
	}

	private void initVisibilityBlock() {
		grantAccessField.onChange(e -> save(LanguageProperty.GrantAccess, grantAccessList()));
		licenseField.onChange(e -> save(LanguageProperty.License, licenseField.value()));
		visibilitySelector.onSelect(this::updateVisibility);
	}

	private void refreshVisibilityBlock() {
		visibilitySelector.selection(language.isPrivate() ? "privateVisibilityOption" : "publicVisibilityOption");
		visibilityBlock.publicVisibilityBlock.visible(language.isPublic());
		grantAccessField.value(String.join("\n", language.grantAccessList()));
		licenseField.value(language.license());
	}

	private void refreshView() {
		hideViews();
		viewSelector.select(0);
	}

	private void hideViews() {
		if (generalBlock.isVisible()) generalBlock.hide();
		else if (visibilityBlock.isVisible()) visibilityBlock.hide();
	}

	private void refreshProperties() {
		propertiesStamp.language(language);
		propertiesStamp.release(release);
		propertiesStamp.refresh();
	}

	private List<String> grantAccessList() {
		return Arrays.stream(grantAccessField.value().split(";?\\n")).map(String::trim).filter(s -> !s.isEmpty()).toList();
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
		refreshVisibilityBlock();
	}

}