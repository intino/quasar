package io.intino.languageeditor.box.ui.displays.templates;

import io.intino.languageeditor.box.LanguageEditorBox;

public class WorkspaceHeaderTemplate extends AbstractWorkspaceHeaderTemplate<LanguageEditorBox> {
	private String _title;
	private String _description = null;

	public WorkspaceHeaderTemplate(LanguageEditorBox box) {
		super(box);
	}

	public void title(String title) {
		this._title = title;
	}

	public void description(String value) {
		this._description = value;
	}

	@Override
	public void refresh() {
		super.refresh();
		title.value(_title);
		description.value(_description);
	}
}