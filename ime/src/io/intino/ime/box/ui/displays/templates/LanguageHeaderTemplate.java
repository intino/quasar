package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.Resource;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ImeBrowser;
import io.intino.ime.box.commands.LanguageCommands;
import io.intino.ime.box.languages.LanguageManager;
import io.intino.ime.box.ui.PathHelper;
import io.intino.ime.box.ui.ViewMode;
import io.intino.ime.box.ui.datasources.MemoryLanguagesDatasource;
import io.intino.ime.box.util.LanguageHelper;
import io.intino.ime.box.util.ModelHelper;
import io.intino.ime.model.*;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public class LanguageHeaderTemplate extends AbstractLanguageHeaderTemplate<ImeBox> {
	private Language language;
	private Consumer<Language> openLanguageListener;
	private Consumer<Language> openModelListener;
	private Consumer<Boolean> saveSettingsListener;
	private Consumer<ViewMode> changeViewListener;

	public LanguageHeaderTemplate(ImeBox box) {
		super(box);
	}

	public void language(Language value) {
		this.language = value;
	}

	public void onOpenSearch(Consumer<Boolean> listener) {
		quassarHeader.onOpenSearch(listener);
	}

	public void onChangeView(Consumer<ViewMode> listener) {
		this.changeViewListener = listener;
	}

	public void onOpenLanguage(Consumer<Language> listener) {
		this.openLanguageListener = listener;
	}

	public void onOpenModel(Consumer<Language> listener) {
		this.openModelListener = listener;
	}

	public void onSaveSettings(Consumer<Boolean> listener) {
		this.saveSettingsListener = listener;
	}

	@Override
	public void init() {
		super.init();
		openModel.onExecute(e -> openModelOf(language));
		settingsDialog.onOpen(e -> refreshSettingsDialog());
		saveSettings.onExecute(e -> saveSettings());
		quassarHeader.onChangeView(this::openModel);
		languageChildrenCatalog.onOpenLanguage(this::notifyOpen);
	}

	@Override
	public void refresh() {
		super.refresh();
		quassarHeader.canEditViewMode(LanguageHelper.level(language, box()) != LanguageLevel.L3);
		quassarHeader.refresh();
		Release lastRelease = box().languageManager().lastRelease(language);
		refreshLanguageLinks();
		title.value(LanguageHelper.label(language, this::translate));
		openModel.visible(ModelHelper.canOpenModel(language, lastRelease, user()));
		viewSettings.visible(LanguageHelper.canEdit(language, lastRelease, user()));
	}

	private void refreshLanguageLinks() {
		LanguageLevel level = LanguageHelper.level(language, box());
		Language l3Language = LanguageHelper.l3Parent(language, box());
		Language l2Language = LanguageHelper.l2Parent(language, box());
		ImeBrowser browser = new ImeBrowser(box());
		List<Browser.Model> l2Children = level == LanguageLevel.L3 ? browser.language(language.name()).children() : Collections.emptyList();
		List<Browser.Model> l1Children = level == LanguageLevel.L2 ? browser.language(language.name()).children() : Collections.emptyList();
		boolean l3Readonly = l3Language == null;
		boolean l2Readonly = level == LanguageLevel.L3 || l2Language == null;
		boolean l1Readonly = level == LanguageLevel.L2 || level == LanguageLevel.L3;
		l3Link.visible(level != LanguageLevel.L3);
		l3Link.readonly(l3Readonly);
		l3Link.path(PathHelper.languagePath(l3Language));
		l3.visible(level == LanguageLevel.L3);
		this.l2Children.visible(level == LanguageLevel.L3 && !l2Children.isEmpty());
		this.l2Children.onOpen(e -> refreshBrowserLanguageChildrenView(l2Children));
		l2Link.visible(level == LanguageLevel.L1 && l2Language != null);
		l2Link.readonly(l2Readonly);
		l2Link.path(PathHelper.languagePath(l2Language));
		l2.visible(level == LanguageLevel.L2 || (level == LanguageLevel.L3 && l2Children.isEmpty()));
		refreshStyles(l1Readonly, l2Readonly, l3Readonly);
		l1Link.visible(level != LanguageLevel.L1 && l1Children.isEmpty());
		l1Link.readonly(l1Readonly);
		this.l1Children.visible(level != LanguageLevel.L1 && !l1Children.isEmpty());
		this.l1Children.onOpen(e -> refreshBrowserLanguageChildrenView(l1Children));
		l1.visible(level == LanguageLevel.L1);
	}

	private void refreshBrowserLanguageChildrenView(List<Browser.Model> children) {
		LanguageManager languageManager = box().languageManager();
		List<Browser.Language> languageList = children.stream().map(Browser.Model::releasedLanguage).filter(Objects::nonNull).distinct().filter(this::isVisible).toList();
		refreshModelChildrenView(languageList.stream().map(l -> languageManager.get(l.name())).toList());
	}

	private boolean isVisible(Browser.Language browserLanguage) {
		Language language = box().languageManager().get(browserLanguage.name());
		return language.isPublic() || language.owner().equals(username());
	}

	private void refreshModelChildrenView(List<Language> children) {
		languageChildrenCatalog.title(String.format(translate("Languages using %s"), LanguageHelper.label(language, this::translate)));
		languageChildrenCatalog.source(new MemoryLanguagesDatasource(box(), session(), children));
		languageChildrenCatalog.refresh();
	}

	private void refreshStyles(boolean l1Readonly, boolean l2Readonly, boolean l3Readonly) {
		if (l3Readonly) l3Link.formats(Set.of("linkDisabled", "m3l3Style"));
		if (l2Readonly) l2Link.formats(Set.of("linkDisabled", "m2l2Style"));
		if (l1Readonly) l1Link.formats(Set.of("linkDisabled", "m1l1Style"));
	}

	private void refreshSettingsDialog() {
		settingsDialog.title(String.format(translate("%s properties"), language.name()));
		settingsEditor.language(language);
		settingsEditor.refresh();
	}

	private void saveSettings() {
		if (!settingsEditor.check()) return;
		settingsDialog.close();
		Resource logo = settingsEditor.logo();
		String description = settingsEditor.description();
		boolean isPrivate = settingsEditor.isPrivate();
		String dockerImageUrl = settingsEditor.dockerImageUrl();
		List<Operation> operations = settingsEditor.operations();
		box().commands(LanguageCommands.class).save(language, description, isPrivate, dockerImageUrl, logo, operations, username());
		saveSettingsListener.accept(true);
		refresh();
	}

	private void openModelOf(Language language) {
		openModelListener.accept(language);
	}

	private void openModel(ViewMode viewMode) {
		openModelOf(language);
		if (changeViewListener != null) changeViewListener.accept(viewMode);
	}

	private void notifyOpen(Language l) {
		openLanguageListener.accept(l);
		l2Children.closePopover();
		l1Children.closePopover();
	}

}