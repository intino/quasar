package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.exceptions.*;
import io.intino.alexandria.ui.displays.events.AddItemEvent;
import io.intino.alexandria.ui.utils.DelayerUtil;
import io.intino.ime.box.*;
import io.intino.ime.box.schemas.*;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.PathHelper;
import io.intino.ime.box.ui.datasources.WorkspacesDatasource;
import io.intino.ime.box.ui.displays.items.MagazineItem;
import io.intino.ime.box.ui.displays.rows.WorkspaceTableRow;
import io.intino.ime.box.ui.displays.templates.AbstractWorkspacesMagazine;
import io.intino.ime.box.workspaces.Workspace;

import java.util.function.Consumer;

public class WorkspacesMagazine extends AbstractWorkspacesMagazine<ImeBox> {
	private WorkspacesDatasource source;
	private Consumer<Workspace> openWorkspaceListener;

	public WorkspacesMagazine(ImeBox box) {
		super(box);
	}

	public void source(WorkspacesDatasource source) {
		this.source = source;
	}

	public void onOpenWorkspace(Consumer<Workspace> listener) {
		this.openWorkspaceListener = listener;
	}

	public void filter(String condition) {
		workspacesMagazineCatalog.filter(condition);
	}

	@Override
	public void init() {
		super.init();
		workspacesMagazineCatalog.onAddItem(this::refresh);
	}

	@Override
	public void refresh() {
		super.refresh();
		workspacesMagazineCatalog.source(source);
	}

	private void refresh(AddItemEvent event) {
		Workspace workspace = event.item();
		MagazineItem item = event.component();
		item.title.title(workspace.title());
		item.title.onExecute(e -> {
			openWorkspaceListener.accept(workspace);
			DelayerUtil.execute(this, v -> notifier.redirect(PathHelper.workspacePath(session(), workspace)), 600);
		});
		item.owner.value(workspace.owner().fullName());
		item.lastModifiedDate.value(workspace.lastModifyDate());
		item.executionsCount.value(workspace.executionsCount());
		item.language.value(workspace.dsl());
	}
}