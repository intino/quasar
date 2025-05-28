package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.events.AddCollectionItemEvent;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.ui.datasources.ModelsDatasource;
import io.quassar.editor.box.ui.displays.items.ModelItem;
import io.quassar.editor.box.ui.types.LanguageTab;
import io.quassar.editor.box.util.DisplayHelper;
import io.quassar.editor.box.util.ModelHelper;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.box.util.PermissionsHelper;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageRelease;
import io.quassar.editor.model.Model;

import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Function;

public class ModelsTemplate extends AbstractModelsTemplate<EditorBox> {
	private Language language;
	private LanguageRelease release;
	private LanguageTab tab;
	private Function<Boolean, Model> createModelListener;
	private Mode mode = Mode.Normal;

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

	public enum Mode { Normal, Forge }
	public void mode(Mode mode) {
		this.mode = mode;
	}

	public void onCreateModel(Function<Boolean, Model> listener) {
		this.createModelListener = listener;
	}

	@Override
	public void init() {
		super.init();
		addModelTrigger.onExecute(e -> notifyCreateModel());
		modelList.onAddItem(this::refresh);
	}

	@Override
	public void refresh() {
		super.refresh();
		addModelTrigger.visible(createModelListener != null && PermissionsHelper.canAddModel(language, session(), box()));
		ModelsDatasource source = new ModelsDatasource(box(), session(), language, release, tab);
		modelList.source(source);
		modelList.reload();
		searchBox.visible(source.itemCount(null, Collections.emptyList()) > DisplayHelper.MinItemsCount);
	}

	private void refresh(AddCollectionItemEvent event) {
		refresh(event.item(), event.component());
	}

	private void refresh(Model model, ModelItem display) {
		display.label.visible(mode == Mode.Normal);
		if (display.label.isVisible()) {
			display.label.title(ModelHelper.label(model, language(), box()));
			display.label.address(path -> PathHelper.modelPath(path, model));
		}
		display.siteLabel.visible(mode == Mode.Forge);
		if (display.siteLabel.isVisible()) {
			display.siteLabel.title(ModelHelper.label(model, language(), box()));
			display.siteLabel.site(PathHelper.modelUrl(model, session()));
		}
		display.description.value(model.description() != null && !model.description().equals(translate("(no description)")) ? model.description() : null);
		display.owner.visible(true);
		if (display.owner.isVisible()) display.owner.value(model.owner());
		display.language.visible(true);//(tab == LanguageTab.Examples);
		if (display.language.isVisible()) display.language.value(model.language().artifactId() + " " + model.language().version());
		display.createDate.value(model.createDate());
	}

	private void notifyCreateModel() {
		if (createModelListener == null) return;
		Model model = createModelListener.apply(true);
		if (mode == Mode.Normal) notifier.dispatch(PathHelper.startingModelPath(model));
		else {
			modelTrigger.site(PathHelper.modelUrl(model, session()));
			modelTrigger.launch();
		}
	}

}