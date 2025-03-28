package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.ui.types.LanguagesTab;
import io.quassar.editor.box.util.PathHelper;

public class LanguagesHeaderTemplate extends AbstractLanguagesHeaderTemplate<EditorBox> {
	private LanguagesTab tab;

	public LanguagesHeaderTemplate(EditorBox box) {
		super(box);
	}

	public void tab(LanguagesTab tab) {
		this.tab = tab;
	}

	@Override
	public void refresh() {
		super.refresh();
		publicLanguagesText.visible(false); //(tab == null || tab == LanguagesTab.PublicLanguages);
		publicLanguagesLink.visible(false); //(tab == LanguagesTab.OwnerLanguages);
		if (publicLanguagesLink.isVisible()) publicLanguagesLink.address(a -> PathHelper.languagesPath(a, LanguagesTab.PublicLanguages));
		myLanguagesText.visible(false); //(user() != null && tab == LanguagesTab.OwnerLanguages);
		myLanguagesLink.visible(false); //(user() != null && (tab == null || tab == LanguagesTab.PublicLanguages));
		if (myLanguagesLink.isVisible()) myLanguagesLink.address(a -> PathHelper.languagesPath(a, LanguagesTab.OwnerLanguages));
	}

}