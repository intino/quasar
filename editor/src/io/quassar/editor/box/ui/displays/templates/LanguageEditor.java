package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.events.actionable.ToggleEvent;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.LanguageCommands;
import io.quassar.editor.box.util.DisplayHelper;
import io.quassar.editor.box.util.DisplayHelper.CheckResult;
import io.quassar.editor.model.Collection;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public class LanguageEditor extends AbstractLanguageEditor<EditorBox> {
	private Language language;
	private Model metamodel;
	private Consumer<Boolean> checkIdListener;
	private Consumer<String> changeIdListener;
	private Consumer<File> changeLogoListener;
	private String selectedCollection = null;

	public LanguageEditor(EditorBox box) {
		super(box);
	}

	public void language(Language language) {
		this.language = language;
	}

	public void metamodel(Model metamodel) {
		this.metamodel = metamodel;
	}

	public void onCheckId(Consumer<Boolean> listener) {
		this.checkIdListener = listener;
	}

	public void onChangeId(Consumer<String> listener) {
		this.changeIdListener = listener;
	}

	public void onChangeLogo(Consumer<File> listener) {
		this.changeLogoListener = listener;
	}

	public String languageCollection() {
		return collectionSelector.selection().getFirst();
	}

	public Collection collection() {
		List<String> selection = collectionSelector.selection();
		return !selection.isEmpty() ? box().collectionManager().get(selection.getFirst()) : null;
	}

	public String languageName() {
		return nameField.value();
	}

	public boolean isPrivate() {
		return privateField.state() == ToggleEvent.State.On;
	}

	public File logo() {
		return languageLogoDialog.logo();
	}

	public void focus() {
		nameField.focus();
	}

	public boolean check() {
		message.value(null);
		if (collectionSelector.selection().isEmpty()) { message.value(translate("Collection is required")); return false; }
		CheckResult result = DisplayHelper.checkLanguageName(nameField.value(), this::translate);
		if (!result.success()) { message.value(result.message()); return false; }
		result = DisplayHelper.checkLanguageInUse(collectionSelector.selection().getFirst(), nameField.value(), this::translate, box());
		if (!result.success()) { message.value(result.message()); return false; }
		return true;
	}

	@Override
	public void init() {
		super.init();
		createCollectionDialog.onCreate(this::updateCollection);
		nameField.onEnterPress(e -> changeId());
		nameField.onChange(e -> refreshState());
		change.onCancelAffirmed(e -> collectionSelector.selection(language.collection()));
		change.onExecute(e -> changeId());
		languageLogoDialog.onChangeLogo(this::updateLogo);
		collectionSelector.onSelect(e -> updateCollection());
		privateField.onToggle(e -> updateAccess());
	}

	@Override
	public void refresh() {
		super.refresh();
		refreshCollectionSelector();
		nameField.value(language != null ? language.name() : metamodelTitle());
		nameField.readonly(language != null);
		privateField.state(language == null || language.isPrivate() ? ToggleEvent.State.On : ToggleEvent.State.Off);
		languageLogoDialog.language(language);
		languageLogoDialog.languageIdProvider(e -> languageId());
		languageLogoDialog.languageNameProvider(e -> nameField.value());
		languageLogoDialog.readonly(nameField.value() == null || nameField.value().isEmpty());
		languageLogoDialog.refresh();
		refreshState();
		if (language == null && nameField.value() != null && !nameField.value().isEmpty()) languageLogoDialog.generateDefaultLogo();
	}

	private String metamodelTitle() {
		String result = metamodel.title().contains(".") ? metamodel.title().substring(metamodel.title().lastIndexOf(".")+1) : metamodel.title();
		return result.toLowerCase();
	}

	private void refreshState() {
		boolean sameName = language == null || language.name().equals(nameField.value());
		boolean validCollection = !collectionSelector.selection().isEmpty();
		boolean sameCollection = language == null || (validCollection && language.collection().equals(collectionSelector.selection().getFirst()));
		boolean validName = sameName && DisplayHelper.checkLanguageName(nameField.value(), this::translate).success() && (language != null || !collectionSelector.selection().isEmpty());
		boolean inUse = isInUse();
		refreshMessage(validCollection && (language == null || !sameCollection || !sameName));
		if (!sameName) message.value(translate("DSL can't change its name once created"));
		validNameIcon.visible(!inUse && validName);
		invalidNameIcon.visible(inUse || !validName);
		languageLogoDialog.readonly(nameField.value() == null || nameField.value().isEmpty());
		if (checkIdListener != null) checkIdListener.accept(!inUse);
	}

	private void changeId() {
		if (changeIdListener == null) return;
		changeIdListener.accept(languageId());
		refresh();
	}

	private void refreshMessage(boolean checkInUse) {
		message.value(null);
		if (collectionSelector.selection().isEmpty()) { message.value("Collection is required"); return; }
		if (nameField.value() == null || nameField.value().isEmpty()) return;
		CheckResult result = DisplayHelper.checkLanguageName(nameField.value(), this::translate);
		if (!result.success()) { message.value(result.message()); return; }
		result = checkInUse ? DisplayHelper.checkLanguageInUse(collectionSelector.selection().getFirst(), nameField.value(), this::translate, box()) : new CheckResult(true, null);
		if (!result.success()) { message.value(result.message()); }
	}

	private void updateLogo(File file) {
		if (changeLogoListener != null) changeLogoListener.accept(file);
	}

	private String languageId() {
		String collection = collectionSelector.selection().getFirst();
		if (collection == null) return null;
		return Language.key(collection, nameField.value());
	}

	private void updateCollection(Collection collection) {
		this.selectedCollection = collection.name();
		refreshCollectionSelector();
		refreshState();
	}

	private void updateCollection() {
		boolean inUse = isInUse();
		if (!sameLanguage() && !inUse && language != null) change.launch();
		this.selectedCollection = !collectionSelector.selection().isEmpty() ? collectionSelector.selection().getFirst() : null;
		refreshCollectionSelector();
		refreshState();
	}

	private boolean sameLanguage() {
		boolean sameName = language != null && language.name().equals(nameField.value());
		boolean validCollection = !collectionSelector.selection().isEmpty();
		boolean sameCollection = language != null && validCollection && language.collection().equals(collectionSelector.selection().getFirst());
		return sameCollection && sameName;
	}

	private boolean isInUse() {
		boolean sameName = language == null || language.name().equals(nameField.value());
		boolean validCollection = !collectionSelector.selection().isEmpty();
		boolean sameCollection = language == null || (validCollection && language.collection().equals(collectionSelector.selection().getFirst()));
		return validCollection && (language == null || !sameCollection || !sameName) && !DisplayHelper.checkLanguageInUse(collectionSelector.selection().getFirst(), nameField.value(), this::translate, box()).success();
	}

	private void refreshCollectionSelector() {
		List<String> options = box().collectionManager().collections(username()).stream().map(Collection::name).toList();
		collectionSelector.clear();
		collectionSelector.addAll(options);
		if (selectedCollection != null) collectionSelector.selection(selectedCollection);
		else if (language != null) collectionSelector.selection(language.collection());
		else if (!options.isEmpty()) collectionSelector.selection(options.getFirst());
	}

	private void updateAccess() {
		if (language == null) return;
		if (privateField.state() == ToggleEvent.State.On) box().commands(LanguageCommands.class).makePrivate(language, username());
		else box().commands(LanguageCommands.class).makePublic(language, username());
	}

}