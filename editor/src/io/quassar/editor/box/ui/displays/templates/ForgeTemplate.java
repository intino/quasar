package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.util.DisplayHelper;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;

public class ForgeTemplate extends AbstractForgeTemplate<EditorBox> {
	private Model model;
	private String release;

	public ForgeTemplate(EditorBox box) {
		super(box);
	}

	public void open(String model, String release) {
		this.model = box().modelManager().get(model);
		this.release = release;
		refresh();
	}

	@Override
	public void init() {
		super.init();
		createLanguageBlock.onShow(e -> refreshCreateLanguageBlock());
	}

	@Override
	public void refresh() {
		super.refresh();
		notFoundBlock.visible(model == null);
		contentBlock.visible(model != null);
		if (model == null) return;
		Language language = box().languageManager().getWithMetamodel(model);
		if (language == null) createLanguageBlock.show();
	}

	private void refreshCreateLanguageBlock() {
		createLanguageStamp.model(model);
		createLanguageStamp.refresh();
	}

}