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
	private View view = View.Default;
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

	public enum View { List, Default }
	public void view(View view) {
		this.view = view;
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
		largeIcon.visible(mode == Mode.Large && view == View.Default);
		smallIcon.visible(mode == Mode.Small && view == View.Default);
		largeIconInList.visible(mode == Mode.Large && view == View.List);
		smallIconInList.visible(mode == Mode.Small && view == View.List);
	}

	private void initDialog() {
		modelDialog.onOpen(e -> refreshDialog());
		cloneModel.onExecute(e -> cloneModel());
		modelTitleField.onEnterPress(e -> cloneModel());
	}

	private void refreshDialog() {
		modelDialog.title(String.format(translate("Clone %s"), ModelHelper.label(model, language(), box())));
		modelTitleField.value(String.format(translate("%s Copy"), ModelHelper.label(model, language(), box())));
	}

	private void cloneModel() {
		if (!DisplayHelper.check(modelTitleField, this::translate)) return;
		modelDialog.close();
		Model newModel = box().commands(ModelCommands.class).clone(model, ModelHelper.proposeName(), modelTitleField.value(), username(), username());
		cloneListener.accept(newModel);
	}

}