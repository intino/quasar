package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.Resource;
import io.intino.alexandria.ui.displays.UserMessage;
import io.intino.alexandria.ui.displays.events.actionable.ToggleEvent;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.LanguageCommands;
import io.intino.ime.box.orchestator.ProjectCreator;
import io.intino.ime.box.scaffolds.Scaffold;
import io.intino.ime.box.scaffolds.ScaffoldFactory;
import io.intino.ime.box.ui.DisplayHelper;
import io.intino.ime.model.Language;
import io.intino.ime.model.Release;

import java.net.URL;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class LanguageDialog extends AbstractLanguageDialog<ImeBox> {
	private Language parent;
	private Release release;
	private Resource logo = null;
	private Consumer<Language> createListener;

	public LanguageDialog(ImeBox box) {
		super(box);
	}

	public void parent(Language value) {
		this.parent = value;
	}

	public void release(Release release) {
		this.release = release;
	}

	public void onCreate(Consumer<Language> listener) {
		this.createListener = listener;
	}

	public void open() {
		dialog.open();
	}

	@Override
	public void init() {
		super.init();
		dialog.onOpen(e -> refreshDialog());
		create.onExecute(e -> create());
		logoField.onChange(e -> logo = e.value());
		groupField.onEnterPress(e -> create());
		descriptionField.onEnterPress(e -> create());
		programmingLanguageSelector.onSelect(e -> refreshScaffolds());
	}

	private void refreshDialog() {
		nameField.value(null);
		groupField.value(null);
		descriptionField.value(null);
		logoField.value((URL)null);
		accessTypeField.state(ToggleEvent.State.On);
		refreshProgrammingLanguage();
		refreshScaffolds();
	}

	private void refreshProgrammingLanguage() {
		programmingLanguageBlock.visible(parent.programmingLanguages().isEmpty() || parent.programmingLanguages().size() > 1);
		if (!programmingLanguageBlock.isVisible()) return;
		programmingLanguageSelector.clear();
		programmingLanguageSelector.addAll(parent.programmingLanguages());
		programmingLanguageSelector.select(parent.programmingLanguages().getFirst());
	}

	private void refreshScaffolds() {
		ScaffoldFactory factory = new ScaffoldFactory();
		ScaffoldFactory.Language programmingLanguage = programmingLanguage();
		List<ScaffoldFactory.Scaffold> scaffolds = factory.scaffoldsOf(programmingLanguage);
		scaffoldBlock.visible(programmingLanguage != null && scaffolds.size() > 1);
		if (!scaffoldBlock.isVisible()) return;
		scaffoldSelector.clear();
		scaffoldSelector.addAll(scaffolds.stream().map(Enum::name).collect(Collectors.toList()));
		scaffoldSelector.select(scaffolds.getFirst().name());
	}

	private void create() {
		if (!check()) return;
		dialog.close();
		String name = nameField.value();
		String group = groupField.value();
		String description = descriptionField.value();
		Language language = box().commands(LanguageCommands.class).create(name, release, group, programmingLanguage(), scaffold(), description, logo, isPrivate(), username());
		createListener.accept(language);
	}

	private boolean check() {
		if (!DisplayHelper.checkLanguageName(nameField, this::translate, box())) return false;
		if (programmingLanguage() == null) {
			notifyUser(translate("Select programming language"), UserMessage.Type.Warning);
			return false;
		}
		if (scaffold() == null) {
			notifyUser(translate("Select build system"), UserMessage.Type.Warning);
			return false;
		}
		return DisplayHelper.check(descriptionField, this::translate);
	}

	private ScaffoldFactory.Language programmingLanguage() {
		if (parent.programmingLanguages().isEmpty()) return null;
		if (parent.programmingLanguages().size() == 1) return ScaffoldFactory.Language.valueOf(parent.programmingLanguages().getFirst());
		List<String> selection = programmingLanguageSelector.selection();
		return !selection.isEmpty() ? ScaffoldFactory.Language.valueOf(selection.getFirst()) : null;
	}

	private ScaffoldFactory.Scaffold scaffold() {
		ScaffoldFactory.Language language = programmingLanguage();
		if (language == null) return null;
		List<ScaffoldFactory.Scaffold> scaffolds = new ScaffoldFactory().scaffoldsOf(language);
		if (scaffolds.isEmpty()) return null;
		if (scaffolds.size() == 1) return scaffolds.getFirst();
		List<String> selection = scaffoldSelector.selection();
		return !selection.isEmpty() ? ScaffoldFactory.Scaffold.valueOf(selection.getFirst()) : null;
	}

	private boolean isPrivate() {
		return accessTypeField.state() == ToggleEvent.State.On;
	}

}