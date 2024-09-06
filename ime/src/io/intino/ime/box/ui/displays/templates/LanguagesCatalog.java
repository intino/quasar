package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.events.AddItemEvent;
import io.intino.alexandria.ui.utils.DelayerUtil;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.ModelCommands;
import io.intino.ime.box.ui.DisplayHelper;
import io.intino.ime.box.ui.PathHelper;
import io.intino.ime.box.ui.datasources.LanguagesDatasource;
import io.intino.ime.box.ui.displays.items.LanguageMagazineItem;
import io.intino.ime.box.util.ModelHelper;
import io.intino.ime.model.Language;
import io.intino.ime.model.Model;

import java.util.function.Consumer;

public class LanguagesCatalog extends AbstractLanguagesCatalog<ImeBox> {
	private LanguagesDatasource source;
	private Consumer<Model> openModelListener;
	private Language selectedLanguage;

	public LanguagesCatalog(ImeBox box) {
		super(box);
	}

	public void source(LanguagesDatasource source) {
		this.source = source;
	}

	public void onOpenModel(Consumer<Model> listener) {
		this.openModelListener = listener;
	}

	public void filter(String condition) {
		languagesMagazine.filter(condition);
	}

	@Override
	public void init() {
		super.init();
		languagesMagazine.onAddItem(this::refresh);
		addPrivateModelDialog.onOpen(e -> refreshAddPrivateModelDialog());
		createModel.onExecute(e -> createModel());
		nameField.onChange(e -> DisplayHelper.checkModelName(nameField, this::translate, box()));
	}

	@Override
	public void refresh() {
		super.refresh();
		languagesMagazine.source(source);
	}

	private void refresh(AddItemEvent event) {
		Language language = event.item();
		LanguageMagazineItem item = event.component();
		item.title.value(language.id());
		item.owner.value(language.owner());
		item.privatePill.visible(language.isPrivate());
		item.createDate.value(language.createDate());
		item.metaLanguage.value(language.info().metaLanguage());
		item.addModel.onExecute(e -> createModel(language));
		item.addModel.visible(user() == null);
		item.addPrivateModel.bindTo(addPrivateModelDialog);
		item.addPrivateModel.onOpen(e -> refreshAddPrivateModelDialog(language));
		item.addPrivateModel.visible(user() != null);
	}

	private void refreshAddPrivateModelDialog(Language language) {
		this.selectedLanguage = language;
		refreshAddPrivateModelDialog();
	}

	private void refreshAddPrivateModelDialog() {
		if (selectedLanguage == null) return;
		languageField.value(selectedLanguage.id());
		nameField.value(ModelHelper.proposeName());
		titleField.value(null);
	}

	private void createModel() {
		if (!DisplayHelper.checkModelName(nameField, this::translate, box())) return;
		if (!DisplayHelper.check(titleField, this::translate)) return;
		addPrivateModelDialog.close();
		createModel(selectedLanguage, nameField.value(), titleField.value());
	}

	private void createModel(Language language) {
		createModel(language, ModelHelper.proposeName(), translate("(no name)"));
	}

	private void createModel(Language language, String name, String title) {
		Model model = box().commands(ModelCommands.class).create(name, title, language.id(), DisplayHelper.user(session()), username());
		openModelListener.accept(model);
		DelayerUtil.execute(this, v -> notifier.redirect(PathHelper.modelUrl(session(), model)), 600);
	}

}