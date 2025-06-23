package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.events.AddCollectionItemEvent;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.models.ModelContainer;
import io.quassar.editor.box.ui.datasources.ModelsDatasource;
import io.quassar.editor.box.ui.displays.items.ModelItem;
import io.quassar.editor.box.ui.types.LanguageTab;
import io.quassar.editor.box.util.ModelHelper;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageRelease;
import io.quassar.editor.model.Model;

import java.util.function.Consumer;
import java.util.function.Function;

public class ModelsTemplate extends AbstractModelsTemplate<EditorBox> {
	private Language language;
	private LanguageRelease release;
	private LanguageTab tab;
	private Consumer<Model> beforeOpenModelListener;
	private Function<Boolean, Model> createModelListener;
	private Mode mode = Mode.Normal;
	private ModelsDialog dialog;
	private Model selectedModel;

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

	public void bindTo(ModelsDialog dialog) {
		this.dialog = dialog;
		dialog.onAddModel(mode != Mode.Embedded ? e -> notifyCreateModel() : null);
		dialog.bindTo(modelList);
	}

	public enum Mode { Normal, Embedded, Forge }
	public void mode(Mode mode) {
		this.mode = mode;
	}

	public void onBeforeOpenModel(Consumer<Model> listener) {
		this.beforeOpenModelListener = listener;
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
		modelList.onAddItem(this::refresh);
		backExamples.onExecute(e -> openBody());
	}

	@Override
	public void refresh() {
		super.refresh();
		LanguageRelease release = this.release != null ? language.release(this.release.version()) : null;
		ModelsDatasource source = new ModelsDatasource(box(), session(), language, release, tab);
		refreshList(source);
		refreshDialog(source);
	}

	private void refreshList(ModelsDatasource source) {
		modelList.source(source);
		modelList.reload();
	}

	private void refreshDialog(ModelsDatasource source) {
		if (dialog == null) return;
		dialog.language(language);
		dialog.countItemsProvider(l -> source.itemCount());
		dialog.refresh();
	}

	private void refresh(AddCollectionItemEvent event) {
		refresh(event.item(), event.component());
	}

	private void refresh(Model model, ModelItem display) {
		refreshLabel(model, display);
		refreshSiteLabel(model, display);
		refreshEmbeddedLabel(model, display);
		display.description.value(model.description() != null && !model.description().equals(translate("(no description)")) ? model.description() : null);
		display.language.visible(true);//(tab == LanguageTab.Examples);
		if (display.language.isVisible()) display.language.value(model.language().artifactId() + " " + model.language().version());
		display.createDate.value(model.createDate());
		display.updateDate.value(model.updateDate());
		display.owner.visible(model.owner() != null && !model.owner().equals(username()));
		if (display.owner.isVisible()) display.owner.value(model.owner());
	}

	private void refreshLabel(Model model, ModelItem display) {
		display.label.visible(mode == Mode.Normal);
		if (!display.label.isVisible()) return;
		display.label.title(ModelHelper.label(model, language(), box()));
		display.label.address(path -> PathHelper.modelPath(path, model));
	}

	private void refreshSiteLabel(Model model, ModelItem display) {
		display.siteLabel.visible(mode == Mode.Forge);
		if (!display.siteLabel.isVisible()) return;
		display.siteLabel.onBeforeOpen(e -> notifyBeforeOpen(model));
		display.siteLabel.title(ModelHelper.label(model, language(), box()));
		display.siteLabel.site(PathHelper.modelUrlFromForge(model, session()));
	}

	private void refreshEmbeddedLabel(Model model, ModelItem display) {
		display.embeddedLabel.visible(mode == Mode.Embedded);
		if (!display.embeddedLabel.isVisible()) return;
		display.embeddedLabel.title(ModelHelper.label(model, language(), box()));
		display.embeddedLabel.onExecute(e -> openModel(model));
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

	private void openBody() {
		embeddedModelBlock.hide();
		body.show();
	}

	private void openModel(Model model) {
		ModelContainer modelContainer = box().modelManager().modelContainer(model, Model.DraftRelease);
		if (modelContainer == null) return;
		notifyBeforeOpen(model);
		body.hide();
		embeddedModelBlock.show();
		selectedModel = model;
		modelTitle.value(ModelHelper.label(model, language(), box()));
		embeddedModelPreview.model(model, Model.DraftRelease);
		embeddedModelPreview.reset();
	}

	private void notifyBeforeOpen(Model model) {
		if (beforeOpenModelListener == null) return;
		beforeOpenModelListener.accept(model);
	}

}