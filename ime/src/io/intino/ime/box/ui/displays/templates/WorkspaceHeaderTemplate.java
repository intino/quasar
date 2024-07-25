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
	public void refresh() {
		super.refresh();
		title.value(_title);
		description.value(_description);
	}
}