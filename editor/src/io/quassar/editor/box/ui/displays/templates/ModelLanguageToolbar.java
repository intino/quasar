package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.models.File;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.ui.types.LanguageTab;
import io.quassar.editor.box.ui.types.ModelView;
import io.quassar.editor.box.util.LanguageHelper;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.model.FilePosition;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageRelease;
import io.quassar.editor.model.Model;

public class ModelLanguageToolbar extends AbstractModelLanguageToolbar<EditorBox> {
	private Model model;
	private String release;
	private ModelView view;
	private LanguageTab tab;
	private File file;
	private FilePosition filePosition;

	public ModelLanguageToolbar(EditorBox box) {
		super(box);
	}

	public void model(Model model) {
		this.model = model;
	}

	public void release(String release) {
		this.release = release;
	}

	public void view(ModelView view) {
		this.view = view;
	}

	public void tab(LanguageTab tab) {
		this.tab = tab;
	}

	public void file(File file) {
		this.file = file;
	}

	public void filePosition(FilePosition position) {
		this.filePosition = position;
	}

	@Override
	public void refresh() {
		super.refresh();
		Language language = box().languageManager().get(model.language());
		refreshAbout(language);
		refreshExamples(language);
		refreshHelp(language);
		refreshLogo(language);
	}

	private void refreshAbout(Language language) {
		aboutText.visible(tab == LanguageTab.About);
		aboutLink.visible(tab != LanguageTab.About);
		if (aboutText.isVisible()) aboutText.value(translate("about"));
		if (aboutLink.isVisible()) {
			aboutLink.address(a -> PathHelper.modelPath(a, model, release, LanguageTab.About, view, file, filePosition));
			aboutLink.title(translate("about"));
		}
	}

	private void refreshHelp(Language language) {
		helpText.visible(tab == LanguageTab.Help);
		helpLink.visible(tab != LanguageTab.Help);
		if (helpLink.isVisible()) helpLink.address(a -> PathHelper.modelPath(a, model, release, LanguageTab.Help, view, file, filePosition));
	}

	private void refreshExamples(Language language) {
		boolean hasExamples = LanguageHelper.hasExamples(language);
		examplesText.visible(hasExamples && tab == LanguageTab.Examples);
		examplesLink.visible(hasExamples && tab != LanguageTab.Examples);
		if (examplesLink.isVisible()) examplesLink.address(a -> PathHelper.modelPath(a, model, release, LanguageTab.Examples, view, file, filePosition));
	}

	private void refreshLogo(Language language) {
		languageLogo.value(LanguageHelper.logo(language, box()));
	}

}