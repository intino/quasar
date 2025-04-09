package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.events.AddCollectionItemEvent;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.ui.datasources.ModelsDatasource;
import io.quassar.editor.box.ui.displays.items.ModelItem;
import io.quassar.editor.box.ui.types.LanguageTab;
import io.quassar.editor.box.util.DisplayHelper;
import io.quassar.editor.box.util.ModelHelper;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageRelease;
import io.quassar.editor.model.Model;

import java.util.Collections;

public class ModelsTemplate extends AbstractModelsTemplate<EditorBox> {
	private Language language;
	private LanguageRelease release;
	private LanguageTab tab;

	public ModelsTemplate(EditorBox box) {
		super(box);
	}

	public void language(Language language) {
		this.language = language;
	}

	public void release(LanguageRelease release) {
		this.release = release;
	}

	public void tab(LanguageTab tab) {
		this.tab = tab;
	}

	@Override
	public void init() {
		super.init();
		addModelDialog.onCreate(this::open);
		addModelTrigger.onExecute(e -> openAddModelDialog());
		modelList.onAddItem(this::refresh);
	}

	@Override
	public void refresh() {
		super.refresh();
		addModelTrigger.visible(tab == LanguageTab.Models);
		ModelsDatasource source = new ModelsDatasource(box(), session(), language, release, tab);
		modelList.source(source);
		modelList.reload();
		searchBox.visible(source.itemCount(null, Collections.emptyList()) > DisplayHelper.MinItemsCount);
	}

	private void refresh(AddCollectionItemEvent event) {
		refresh(event.item(), event.component());
	}

	private void refresh(Model model, ModelItem display) {
		display.label.title(ModelHelper.label(model, language(), box()));
		display.label.address(path -> PathHelper.modelPath(path, model));
		display.description.value(model.description() != null && !model.description().equals(translate("(no description)")) ? model.description() : null);
		display.owner.visible(tab == LanguageTab.Examples);
		if (display.owner.isVisible()) display.owner.value(model.owner());
		display.createDate.value(model.createDate());
	}

	private void openAddModelDialog() {
		addModelDialog.language(language);
		addModelDialog.open();
	}

	private void open(Model model) {
		notifier.dispatch(PathHelper.modelPath(model));
	}

}