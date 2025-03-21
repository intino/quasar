package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.ui.types.LanguageTab;
import io.quassar.editor.box.ui.types.LanguageView;
import io.quassar.editor.box.ui.types.LanguagesTab;
import io.quassar.editor.box.ui.types.LanguagesView;
import io.quassar.editor.box.util.PathHelper;

public class LanguagesHeaderTemplate extends AbstractLanguagesHeaderTemplate<EditorBox> {
	private LanguagesTab tab;
	private LanguagesView view;

	public LanguagesHeaderTemplate(EditorBox box) {
		super(box);
	}

	public void tab(LanguagesTab tab) {
		this.tab = tab;
	}

	public void view(LanguagesView view) {
		this.view = view;
	}

	@Override
	public void refresh() {
		super.refresh();
		languagesToolbar.visible(tab == LanguagesTab.Languages);
		publicLanguages.readonly(view == null || view == LanguagesView.PublicLanguages);
		publicLanguages.address(a -> PathHelper.languagesPath(a, LanguagesTab.Languages, LanguagesView.PublicLanguages));
		myLanguages.readonly(view == LanguagesView.OwnerLanguages);
		myLanguages.visible(user() != null);
		myLanguages.address(a -> PathHelper.languagesPath(a, LanguagesTab.Languages, LanguagesView.OwnerLanguages));
		homeOperation.readonly(tab == null || tab == LanguagesTab.Home);
		homeOperation.address(a -> PathHelper.languagesPath(a, LanguagesTab.Home));
		languagesOperation.readonly(tab == LanguagesTab.Languages);
		languagesOperation.address(a -> PathHelper.languagesPath(a, LanguagesTab.Languages));
	}

}