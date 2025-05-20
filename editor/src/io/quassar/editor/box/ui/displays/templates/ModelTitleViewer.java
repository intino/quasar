package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.util.ModelHelper;
import io.quassar.editor.box.util.PermissionsHelper;
import io.quassar.editor.model.Model;

public class ModelTitleViewer extends AbstractModelTitleViewer<EditorBox> {
	private Model model;

	public ModelTitleViewer(EditorBox box) {
		super(box);
	}

	public void model(Model model) {
		this.model = model;
	}

	@Override
	public void init() {
		super.init();
		titleDialog.onSave(e -> refresh());
		simpleTitleBlock.onInit(e -> initSimpleTitleBlock());
		simpleTitleBlock.onShow(e -> refreshSimpleTitleBlock());
		qualifiedTitleBlock.onInit(e -> initQualifiedTitleBlock());
		qualifiedTitleBlock.onShow(e -> refreshQualifiedTitleBlock());
		projectBlock.onOpen(e -> refreshProjectBlock());
		moduleBlock.onOpen(e -> refreshModuleBlock());
	}

	@Override
	public void refresh() {
		super.refresh();
		qualifiedTitleBlock.hide();
		simpleTitleBlock.hide();
		if (model.isTitleQualified()) qualifiedTitleBlock.show();
		else simpleTitleBlock.show();
	}

	private void openDialog() {
		titleDialog.model(model);
		titleDialog.open();
	}

	private void initSimpleTitleBlock() {
		title.onExecute(e -> openDialog());
	}

	private void refreshSimpleTitleBlock() {
		title.title(ModelHelper.label(model, language(), box()));
		title.readonly(!PermissionsHelper.canEditTitle(model, box()));
	}

	private void initQualifiedTitleBlock() {
		editTitleTrigger.onExecute(e -> openDialog());
	}

	private void refreshQualifiedTitleBlock() {
		project.title(model.project());
		module.title(model.module());
		editTitleTrigger.readonly(!PermissionsHelper.canEditTitle(model, box()));
	}

	private void refreshProjectBlock() {
		projectBlock.projectViewer.model(model);
		projectBlock.projectViewer.onSelect(e -> projectBlock.close());
		projectBlock.projectViewer.project(model.project());
		projectBlock.projectViewer.refresh();
	}

	private void refreshModuleBlock() {
		moduleBlock.moduleBlockTitle.value(translate("MODELS"));
		moduleBlock.moduleViewer.onSelect(e -> moduleBlock.close());
		moduleBlock.moduleViewer.model(model);
		moduleBlock.moduleViewer.module(model.module());
		moduleBlock.moduleViewer.showTitle(false);
		moduleBlock.moduleViewer.refresh();
	}

}