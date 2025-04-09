package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.Resource;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.ui.displays.UserMessage;
import io.intino.alexandria.ui.displays.events.ChangeEvent;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.LanguageCommands;
import io.quassar.editor.box.util.DisplayHelper;
import io.quassar.editor.box.util.PermissionsHelper;
import io.quassar.editor.model.Language;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Consumer;

public class LanguageEditor extends AbstractLanguageEditor<EditorBox> {
	private Language language;
	private Set<String> tagSet;
	private Consumer<Language> removeListener;
	private boolean logoExists = false;

	public LanguageEditor(EditorBox box) {
		super(box);
	}

	public void language(Language language) {
		this.language = language;
	}

	public String title() {
		return titleField.value();
	}

	public String description() {
		return descriptionField.value();
	}

	public List<String> tags() {
		return new ArrayList<>(tagSet);
	}

	public File logo() {
		if (!logoExists) return null;
		File tmpFile = new File(box().archetype().tmp().root(), language.name() + "-logo.png");
		return tmpFile.exists() ? tmpFile : box().languageManager().loadLogo(language);
	}

	public void onRemove(Consumer<Language> listener) {
		this.removeListener = listener;
	}

	public boolean check() {
		if (!DisplayHelper.check(titleField, this::translate)) return false;
		return DisplayHelper.check(descriptionField, this::translate);
	}

	@Override
	public void init() {
		super.init();
		addTagDialog.onOpen(e -> refreshTagDialog());
		logoField.onChange(this::updateLogo);
		addTag.onExecute(e -> addTag());
		tagField.onEnterPress(e -> addTag());
		removeLanguage.onExecute(e -> removeLanguage());
	}

	@Override
	public void refresh() {
		super.refresh();
		tagSet = new HashSet<>(language.tags());
		File logo = box().languageManager().loadLogo(language);
		logoExists = logo.exists();
		logoField.value(logo.exists() ? logo : null);
		titleField.value(language.title());
		descriptionField.value(language.description());
		refreshTags();
		removeLanguage.readonly(!PermissionsHelper.canRemove(language, session(), box()));
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
		refreshTags();
	}

	private void addTag() {
		if (!DisplayHelper.check(tagField, this::translate)) return;
		addTagDialog.close();
		tagSet.add(tagField.value());
		refreshTags();
	}

	private void updateLogo(ChangeEvent event) {
		try {
			File tmpFile = new File(box().archetype().tmp().root(), language.name() + "-logo.png");
			if (tmpFile.exists()) tmpFile.delete();
			Resource value = event.value();
			if (value != null) Files.write(tmpFile.toPath(), value.bytes());
			logoExists = value != null;
			logoField.value(value != null ? tmpFile : null);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private void removeLanguage() {
		notifyUser(translate("Removing language..."), UserMessage.Type.Loading);
		box().commands(LanguageCommands.class).remove(language, username());
		notifyUser(translate("Language removed successfully..."), UserMessage.Type.Success);
		removeListener.accept(language);
	}

}