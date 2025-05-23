package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.LanguageCommands;
import io.quassar.editor.box.util.DisplayHelper;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageProperty;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class LanguagePropertiesTemplate extends AbstractLanguagePropertiesTemplate<EditorBox> {
	private Language language;
	private String release;
	private Set<String> tagSet;

	public LanguagePropertiesTemplate(EditorBox box) {
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
		titleField.onChange(e -> save(LanguageProperty.Title, titleField.value()));
		descriptionField.onChange(e -> save(LanguageProperty.Description, descriptionField.value()));
		citationField.onChange(e -> save(LanguageProperty.Citation, citationField.value()));
		citationLinkField.onChange(e -> save(LanguageProperty.CitationLink, citationLinkField.value()));
		licenseField.onChange(e -> save(LanguageProperty.License, licenseField.value()));
		addTagDialog.onOpen(e -> refreshTagDialog());
		addTag.onExecute(e -> addTag());
		tagField.onEnterPress(e -> addTag());
	}

	@Override
	public void refresh() {
		super.refresh();
		tagSet = new HashSet<>(language.tags());
		titleField.value(language.title());
		descriptionField.value(language.description());
		licenseField.value(language.license());
		citationField.value(language.citation());
		citationLinkField.value(language.citationLink());
		refreshTags();
	}

	private void refreshTagDialog() {
		tagField.value(null);
	}

	private void refreshTags() {
		tags.clear();
		tagSet.stream().sorted(Comparator.naturalOrder()).forEach(o -> fill(o, tags.add()));
	}

	private void fill(String tag, TagEditor display) {
		display.tag(tag);
		display.onRemove(o -> removeTag(tag));
		display.refresh();
	}

	private void removeTag(String tag) {
		tagSet.remove(tag);
		save(LanguageProperty.Tags, new ArrayList<>(tagSet));
		refreshTags();
	}

	private void addTag() {
		if (!DisplayHelper.check(tagField, this::translate)) return;
		addTagDialog.close();
		tagSet.add(tagField.value());
		save(LanguageProperty.Tags, new ArrayList<>(tagSet));
		refreshTags();
	}

	private void save(LanguageProperty property, Object value) {
		box().commands(LanguageCommands.class).save(language, property, value, username());
	}

}