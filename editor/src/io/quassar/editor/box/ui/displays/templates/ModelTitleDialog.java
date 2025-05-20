package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.UserMessage;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.ModelCommands;
import io.quassar.editor.box.util.DisplayHelper;
import io.quassar.editor.model.Model;

import java.util.List;
import java.util.function.Consumer;

public class ModelTitleDialog extends AbstractModelTitleDialog<EditorBox> {
	private Model model;
	private Consumer<String> saveListener;
	private AddType selectedAddType;
	private String selectedProject;
	private String selectedModule;

	private enum AddType { Project, Module }

	public ModelTitleDialog(EditorBox box) {
		super(box);
	}

	public void model(Model model) {
		this.model = model;
	}

	public void onSave(Consumer<String> listener) {
		this.saveListener = listener;
	}

	public void open() {
		dialog.open();
	}

	@Override
	public void init() {
		super.init();
		dialog.onOpen(e -> refreshDialog());
		addDialog.onOpen(e -> refreshAddDialog());
		simpleTitleBlock.onShow(e -> refreshSimpleTitleBlock());
		qualifiedTitleBlock.onInit(e -> initQualifiedTitleBlock());
		qualifiedTitleBlock.onShow(e -> refreshQualifiedTitleBlock());
		save.onExecute(e -> saveTitle());
		addField.onEnterPress(e -> add());
		add.onExecute(e -> add());
	}

	private void refreshDialog() {
		typeSelector.select(model.isTitleQualified() ? "qualifiedOption" : "simpleOption");
	}

	private void refreshSimpleTitleBlock() {
		titleField.value(model.title());
		titleField.focus();
	}

	private void initQualifiedTitleBlock() {
		projectSelector.onSelect(e -> updateProject());
		moduleSelector.onSelect(e -> updateModule());
		addProjectLink.onExecute(e -> {
			selectedAddType = AddType.Project;
			addDialog.open();
		});
		addModuleLink.onExecute(e -> {
			selectedAddType = AddType.Module;
			addDialog.open();
		});
	}

	private void refreshQualifiedTitleBlock() {
		qualifiedTitleField.value(qualifiedTitle());
		refreshProjects();
		updateProject();
	}

	private void refreshProjects() {
		projectSelector.clear();
		projectSelector.addAll(projects());
		if (selectedProject != null) projectSelector.select(selectedProject);
		else if (!model.project().isEmpty()) projectSelector.select(model.project());
	}

	private void updateProject() {
		selectedProject = selectedProject();
		refreshModules();
	}

	private void refreshModules() {
		if (selectedProject == null) return;
		moduleSelector.clear();
		moduleSelector.addAll(modules());
		if (selectedModule != null) moduleSelector.select(selectedModule);
		else if (!model.module().isEmpty()) moduleSelector.select(model.module());
	}

	private void updateModule() {
		selectedModule = selectedModule();
	}

	private List<String> projects() {
		return box().modelManager().projects(username());
	}

	private List<String> modules() {
		return box().modelManager().modules(selectedProject(), username());
	}

	private String selectedProject() {
		List<String> selection = projectSelector.selection();
		return !selection.isEmpty() ? selection.getFirst() : null;
	}

	private String selectedModule() {
		List<String> selection = moduleSelector.selection();
		return !selection.isEmpty() ? selection.getFirst() : null;
	}

	private void saveTitle() {
		if (simpleTitleBlock.isVisible()) saveSimpleTitle();
		else saveQualifiedTitle();
	}

	private void saveSimpleTitle() {
		String value = titleField.value();
		box().commands(ModelCommands.class).saveSimpleTitle(model, value, username());
		dialog.close();
		saveListener.accept(value);
	}

	private void saveQualifiedTitle() {
		String project = selectedProject != null ? selectedProject : model.project();
		String module = selectedModule != null ? selectedModule : model.module();
		if (project.isEmpty()) {
			notifyUser(translate("Project is required"), UserMessage.Type.Warning);
			return;
		}
		if (module.isEmpty()) {
			notifyUser(translate("Module is required"), UserMessage.Type.Warning);
			return;
		}
		box().commands(ModelCommands.class).saveQualifiedTitle(model, project, module, username());
		dialog.close();
		saveListener.accept(Model.qualifiedTitleFor(project, module));
	}

	private void refreshAddDialog() {
		addDialog.title(translate("Add %s".formatted(selectedAddType.name().toLowerCase())));
		addField.value(null);
		addField.focus();
	}

	private void add() {
		if (selectedAddType == null) return;
		if (!DisplayHelper.check(addField, this::translate)) return;
		if (selectedAddType == AddType.Project) this.selectedProject = addField.value();
		else this.selectedModule = addField.value();
		addDialog.close();
		refreshQualifiedTitleBlock();
	}

	private String qualifiedTitle() {
		String project = selectedProject != null ? selectedProject : model.project();
		String module = selectedModule != null ? selectedModule : model.module();
		return Model.qualifiedTitleFor(project, module);
	}

}