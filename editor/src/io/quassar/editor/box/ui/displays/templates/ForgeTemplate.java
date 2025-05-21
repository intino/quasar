package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.ui.types.ForgeView;
import io.quassar.editor.box.ui.types.LanguageView;
import io.quassar.editor.box.util.DisplayHelper;
import io.quassar.editor.box.util.PermissionsHelper;
import io.quassar.editor.box.util.SessionHelper;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;

public class ForgeTemplate extends AbstractForgeTemplate<EditorBox> {
	private Model model;
	private String release;
	private ForgeView view;

	public ForgeTemplate(EditorBox box) {
		super(box);
	}

	public void open(String model, String release, String view) {
		this.model = box().modelManager().get(model);
		this.release = release;
		this.view = view != null ? ForgeView.from(view) : SessionHelper.forgeView(session());
		refresh();
	}

	@Override
	public void init() {
		super.init();
		createLanguageBlock.onInit(e -> initCreateLanguageBlock());
		createLanguageBlock.onShow(e -> refreshCreateLanguageBlock());
		languageBlock.onShow(e -> refreshLanguageBlock());
	}

	@Override
	public void refresh() {
		super.refresh();
		notFoundBlock.visible(!PermissionsHelper.hasPermissions(model, session()));
		contentBlock.visible(PermissionsHelper.hasPermissions(model, session()));
		if (model == null) return;
		Language language = box().languageManager().getWithMetamodel(model);
		hideBlocks();
		if (language == null) createLanguageBlock.show();
		else languageBlock.show();
	}

	private void hideBlocks() {
		if (createLanguageBlock.isVisible()) createLanguageBlock.hide();
		if (languageBlock.isVisible()) languageBlock.hide();
	}

	private void initCreateLanguageBlock() {
		createLanguageStamp.onCreate(language -> refresh());
	}

	private void refreshCreateLanguageBlock() {
		createLanguageStamp.model(model);
		createLanguageStamp.release(release);
		createLanguageStamp.refresh();
	}

	private void refreshLanguageBlock() {
		languageStamp.language(box().languageManager().getWithMetamodel(model));
		languageStamp.release(release);
		languageStamp.view(view);
		languageStamp.refresh();
	}

}