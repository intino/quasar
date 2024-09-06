package io.intino.ime.box.ui.displays.templates;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.ModelCommands;
import io.intino.ime.box.ui.DisplayHelper;
import io.intino.ime.box.util.ModelHelper;
import io.intino.ime.model.Model;

import java.util.function.Consumer;

public class CloneModelEditor extends AbstractCloneModelEditor<ImeBox> {
	private Model model;
	private Mode mode = Mode.Large;
	private Consumer<Model> cloneListener;

	public CloneModelEditor(ImeBox box) {
		super(box);
	}

	public void model(Model model) {
		this.model = model;
	}

	public enum Mode { Small, Large }
	public void mode(Mode mode) {
		this.mode = mode;
	}

	public void onClone(Consumer<Model> listener) {
		this.cloneListener = listener;
	}

	@Override
	public void init() {
		super.init();
		initDialog();
	}

	@Override
	public void refresh() {
		super.refresh();
		largeIcon.visible(mode == Mode.Large);
		smallIcon.visible(mode == Mode.Small);
	}

	private void initDialog() {
		modelDialog.onOpen(e -> refreshDialog());
		cloneModel.onExecute(e -> cloneModel());
		modelNameField.onChange(e -> DisplayHelper.checkModelName(modelNameField, this::translate, box()));
	}

	private void refreshDialog() {
		modelDialog.title(String.format(translate("Clone %s"), model.title()));
		modelNameField.value(ModelHelper.proposeName());
		modelTitleField.value(String.format(translate("%s Copy"), model.title()));
	}

	private void cloneModel() {
		if (!DisplayHelper.checkModelName(modelNameField, this::translate, box())) return;
		if (!DisplayHelper.check(modelTitleField, this::translate)) return;
		modelDialog.close();
		Model newModel = box().commands(ModelCommands.class).clone(model, modelNameField.value(), modelTitleField.value(), username());
		cloneListener.accept(newModel);
	}

}