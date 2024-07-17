package io.intino.languageeditor.box.ui.pages;

import io.intino.languageeditor.box.ui.displays.templates.WorkspaceTemplate;

public class WorkspacePage extends AbstractWorkspacePage {
	public String name;

	public io.intino.alexandria.ui.Soul prepareSoul(io.intino.alexandria.ui.services.push.UIClient client) {
		return new io.intino.alexandria.ui.Soul(session) {
			@Override
			public void personify() {
				WorkspaceTemplate component = new WorkspaceTemplate(box);
				component.workspace(name);
				register(component);
				component.init();
				component.refresh();
			}
		};
	}
}