package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.UserMessage;
import io.intino.alexandria.ui.displays.events.AddCollectionItemEvent;
import io.intino.alexandria.ui.displays.events.SelectionEvent;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.ModelCommands;
import io.intino.ime.box.scaffolds.ScaffoldFactory;
import io.intino.ime.box.ui.DisplayHelper;
import io.intino.ime.box.ui.datasources.LanguagesDatasource;
import io.intino.ime.box.ui.displays.rows.LanguageTableRow;
import io.intino.ime.box.util.LanguageHelper;
import io.intino.ime.box.util.ModelHelper;
import io.intino.ime.model.Language;
import io.intino.ime.model.LanguageLevel;
import io.intino.ime.model.Model;
import io.intino.ime.model.Release;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ModelDialog extends AbstractModelDialog<ImeBox> {
	private Language language;
	private Release release;
	private Consumer<Model> createListener;

	public ModelDialog(ImeBox box) {
		super(box);
	}

	public void language(Language language) {
		this.language = language;
	}

	public void release(Release release) {
		this.release = release;
	}

	public void onCreate(Consumer<Model> listener) {
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
		titleField.onEnterPress(e -> create());
		languageSelector.onSelect(this::updateSelectedLanguage);
		languageTable.onAddItem(this::refresh);
		programmingLanguageSelector.onSelect(e -> refreshScaffolds());
	}

	private void refreshDialog() {
		refreshLanguage();
		refreshTitle();
		refreshProgrammingLanguage();
		refreshScaffolds();
	}

	private void refreshLanguage() {
		refreshLanguageTitle();
		refreshLanguageSelector();
	}

	private void refreshLanguageTitle() {
		languageTitle.visible(release != null);
		if (!languageTitle.isVisible()) return;
		languageTitle.value(release.id());
	}

	private void refreshLanguageSelector() {
		languageSelector.visible(language == null);
		if (!languageSelector.isVisible()) return;
		languageSelector.valueProvider(l -> ((Language)l).name());
		languageSelector.source(new LanguagesDatasource(box(), session(), LanguageLevel.L1));
		languageSelector.focus();
	}

	private void refreshTitle() {
		titleBlock.visible(user() != null);
		if (!titleBlock.isVisible()) return;
		titleField.value(null);
		if (language != null) titleField.focus();
	}

	private void refreshProgrammingLanguage() {
		programmingLanguageBlock.visible(language != null && (language.programmingLanguages().isEmpty() || language.programmingLanguages().size() > 1));
		if (!programmingLanguageBlock.isVisible()) return;
		programmingLanguageSelector.clear();
		programmingLanguageSelector.addAll(language.programmingLanguages());
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

	private void updateSelectedLanguage(SelectionEvent event) {
		this.language = !event.selection().isEmpty() ? (Language) event.selection().getFirst() : null;
		this.release = language != null ? lastRelease(language) : null;
		refreshProgrammingLanguage();
	}

	private Release lastRelease(Language language) {
		return box().languageManager().lastRelease(language);
	}

	private void create() {
		if (!check()) return;
		dialog.close();
		String name = ModelHelper.proposeName();
		String title = user() == null ? translate("(no name)") : titleField.value();
		Release release = this.release != null ? this.release : lastRelease(language);
		Model model = box().commands(ModelCommands.class).create(name, title, release, programmingLanguage(), scaffold(), DisplayHelper.user(session()), username());
		createListener.accept(model);
	}

	private boolean check() {
		if (language == null && !DisplayHelper.check(languageSelector)) {
			notifyUser("Language field is required", UserMessage.Type.Warning);
			return false;
		}
		if (user() != null && !DisplayHelper.check(titleField, this::translate)) return false;
		if (programmingLanguage() == null) {
			notifyUser("Programming language field is required", UserMessage.Type.Warning);
			return false;
		}
		if (scaffold() == null) {
			notifyUser(translate("Select build system"), UserMessage.Type.Warning);
			return false;
		}
		return true;
	}

	private ScaffoldFactory.Language programmingLanguage() {
		if (language == null || language.programmingLanguages().isEmpty()) return null;
		if (language.programmingLanguages().size() == 1) return ScaffoldFactory.Language.valueOf(language.programmingLanguages().getFirst());
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

	private void refresh(AddCollectionItemEvent event) {
		Language language = event.item();
		LanguageTableRow row = event.component();
		row.lteLogoItem.logo.value(LanguageHelper.logo(language, box()));
		row.lteNameItem.name.value(language.name());
		row.lteDescriptionItem.description.value(language.description());
		row.lteOwnerItem.owner.value(language.owner());
	}

}