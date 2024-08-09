package io.intino.ime.box.ui.pages;

import io.intino.alexandria.ui.services.push.User;
import io.intino.ime.box.ui.PathHelper;
import io.intino.ime.box.ui.displays.templates.WorkspaceTemplate;
import io.intino.ime.model.Workspace;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class WorkspacePage extends AbstractWorkspacePage {
	public String user;
	public String workspace;
	public String file;

	@Override
	public boolean hasPermissions() {
		if (PathHelper.PublicUser.equals(user)) return true;
		Workspace workspace = box.workspaceManager().workspace(this.workspace);
		if (workspace == null) return false;
		if (workspace.isPublic()) return true;
		User loggedUser = session.user();
		return loggedUser != null && loggedUser.username().equals(user);
	}

	@Override
	public String redirectUrl() {
		Workspace workspace = box.workspaceManager().workspace(this.workspace);
		return session.browser().baseUrl() + (workspace != null ? "/permissions" : "/not-found") + "?workspace=" + this.workspace + "&callback=" + URLEncoder.encode(session.browser().requestUrl(), StandardCharsets.UTF_8);
	}

	public io.intino.alexandria.ui.Soul prepareSoul(io.intino.alexandria.ui.services.push.UIClient client) {
		return new io.intino.alexandria.ui.Soul(session) {
			@Override
			public void personify() {
				WorkspaceTemplate component = new WorkspaceTemplate(box);
				component.user(WorkspacePage.this.user);
				component.workspace(workspace);
				component.file(file);
				register(component);
				component.init();
				component.refresh();
			}
		};
	}
}