package io.intino.languageeditor.box;

import io.intino.alexandria.logger.Logger;
import io.intino.languagearchetype.Archetype;
import io.intino.languageeditor.box.commands.Commands;
import io.intino.languageeditor.box.commands.CommandsFactory;
import io.intino.languageeditor.box.lsp.LanguageServerWebSocketHandler;
import io.intino.languageeditor.box.workspaces.WorkspaceManager;
import io.intino.ls.DocumentManager;
import io.intino.ls.IntinoLanguageServer;
import tara.dsl.Proteo;

import java.io.File;
import java.io.IOException;

public class LanguageEditorBox extends AbstractBox {
	private Archetype archetype;
	private WorkspaceManager workspaceManager;
	private CommandsFactory commandsFactory;

	public LanguageEditorBox(String[] args) {
		this(new LanguageEditorConfiguration(args));
		this.archetype = new Archetype(configuration.home());
	}

	public LanguageEditorBox(LanguageEditorConfiguration configuration) {
		super(configuration);
		this.archetype = new Archetype(configuration.home());
	}

	@Override
	public io.intino.alexandria.core.Box put(Object o) {
		super.put(o);
		return this;
	}

	public void beforeStart() {
		commandsFactory = new CommandsFactory(this);
		workspaceManager = new WorkspaceManager(archetype);
	}

	@Override
	protected void beforeSetupEditorElementsUi(io.intino.alexandria.ui.UISpark sparkInstance) {
		try {
			LanguageServerWebSocketHandler handler = new LanguageServerWebSocketHandler();
			handler.init(new IntinoLanguageServer(new Proteo(), new DocumentManager(new File("./temp"))));
			sparkInstance.service().webSocket("/dsl/tara", handler);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public void afterStart() {
	}

	public void beforeStop() {
	}

	public void afterStop() {
	}

	public Archetype archetype() {
		return archetype;
	}

	public WorkspaceManager workspaceManager() {
		return workspaceManager;
	}

	public <F extends Commands> F commands(Class<F> clazz) {
		return commandsFactory.command(clazz);
	}

	protected io.intino.alexandria.ui.services.AuthService authService(java.net.URL authServiceUrl) {
		//TODO add your authService
		return null;
	}
}