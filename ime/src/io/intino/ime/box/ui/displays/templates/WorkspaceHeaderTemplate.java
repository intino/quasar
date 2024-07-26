package io.intino.ime.box.ui.displays.templates;

import io.intino.ime.box.ImeBox;

public class WorkspaceHeaderTemplate extends AbstractWorkspaceHeaderTemplate<ImeBox> {
	private String _title;
	private String _description = null;

	public WorkspaceHeaderTemplate(ImeBox box) {
		super(box);
	}

	public void title(String title) {
		this._title = title;
	}

	public void description(String value) {
		this._description = value;
	}

	@Override
	public void init() {
		super.init();
		cancel.onExecute(e -> hideTitleEditor());
		save.onExecute(e -> saveTitle());
		titleField.onEnterPress(e -> saveTitle());
	}

	@Override
	public void refresh() {
		super.refresh();
		titleLink.title(_title);
		titleLink.onExecute(e -> showTitleEditor());
		description.value(_description);
	}

	private void showTitleEditor() {
		titleField.value(_title);
		titleLink.visible(false);
		titleEditor.visible(true);
	}

	private void hideTitleEditor() {
		titleLink.visible(true);
		titleEditor.visible(false);
	}

	private void saveTitle() {
		_title = titleField.value();
		hideTitleEditor();
	}
}