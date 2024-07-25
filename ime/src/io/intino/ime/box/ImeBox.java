package io.intino.ime.box;

import io.intino.languagearchetype.Archetype;
import io.intino.ime.box.commands.Commands;
import io.intino.ime.box.commands.CommandsFactory;
import io.intino.ime.box.lsp.LanguageServerWebSocketHandler;
import io.intino.ime.box.workspaces.WorkspaceManager;

public class ImeBox extends AbstractBox {
	private Archetype archetype;
	private WorkspaceManager workspaceManager;
	private CommandsFactory commandsFactory;

	public ImeBox(String[] args) {
		this(new ImeConfiguration(args));
		this.archetype = new Archetype(configuration.home());
	}

	public ImeBox(ImeConfiguration configuration) {
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
	protected void beforeSetupImeElementsUi(io.intino.alexandria.ui.UISpark sparkInstance) {
		LanguageServerWebSocketHandler handler = new LanguageServerWebSocketHandler(new LanguageServerFactory(), this.workspaceManager());
		sparkInstance.service().webSocket("/dsl/tara", handler);
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