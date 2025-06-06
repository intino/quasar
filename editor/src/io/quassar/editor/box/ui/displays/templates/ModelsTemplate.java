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
import java.util.List;
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

	public void refresh(Model model) {
		int index = modelList.findItem(i -> i instanceof Model && ((Model)i).id().equals(model.id()));
		if (index == -1) return;
		modelList.refresh(index, model);
	}

	@Override
	public void init() {
		super.init();
		addModelTrigger.onExecute(e -> notifyCreateModel());
		modelList.onAddItem(this::refresh);
		mostRecentLink.onExecute(e -> updateSorting("most recent"));
		lastModifiedLink.onExecute(e -> updateSorting("last modified"));
	}

	public void reload() {
		ModelsDatasource source = new ModelsDatasource(box(), session(), language, language.release(this.release.version()), tab);
		modelList.source(source);
		modelList.reload();
	}

	@Override
	public void refresh() {
		super.refresh();
		refreshSorting("most recent");
		searchBox.condition(null);
		addModelTrigger.visible(createModelListener != null && PermissionsHelper.canAddModel(language, session(), box()));
		ModelsDatasource source = new ModelsDatasource(box(), session(), language, release, tab);
		modelList.source(source);
		modelList.reload();
		catalogOperations.visible(source.itemCount(null, Collections.emptyList()) > DisplayHelper.MinItemsCount);
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
			display.siteLabel.site(PathHelper.modelUrlFromForge(model, session()));
		}
		display.description.value(model.description() != null && !model.description().equals(translate("(no description)")) ? model.description() : null);
		display.language.visible(true);//(tab == LanguageTab.Examples);
		if (display.language.isVisible()) display.language.value(model.language().artifactId() + " " + model.language().version());
		display.createDate.value(model.createDate());
		display.updateDate.value(model.updateDate());
	}

	private void notifyCreateModel() {
		if (createModelListener == null) return;
		Model model = createModelListener.apply(true);
		if (mode == Mode.Normal) notifier.dispatch(PathHelper.startingModelPath(model));
		else {
			modelTrigger.site(PathHelper.modelUrlFromForge(model, session()));
			modelTrigger.launch();
		}
	}

	private void updateSorting(String sorting) {
		modelList.sortings(List.of(sorting));
		refreshSorting(sorting);
	}

	private void refreshSorting(String sorting) {
		mostRecentLink.visible(!sorting.equals("most recent"));
		mostRecentText.visible(sorting.equals("most recent"));
		lastModifiedLink.visible(!sorting.equals("last modified"));
		lastModifiedText.visible(sorting.equals("last modified"));
	}

}