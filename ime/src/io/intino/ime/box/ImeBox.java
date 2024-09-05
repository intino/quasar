package io.intino.ime.box;

import io.intino.alexandria.ui.services.auth.Space;
import io.intino.amidas.accessor.alexandria.core.AmidasOauthAccessor;
import io.intino.ime.box.commands.Commands;
import io.intino.ime.box.commands.CommandsFactory;
import io.intino.ime.box.dsls.LanguageLoader;
import io.intino.ime.box.dsls.LanguageManager;
import io.intino.ime.box.dsls.LanguageServerFactory;
import io.intino.ime.box.lsp.LanguageServerWebSocketHandler;
import io.intino.ime.box.util.WorkspaceSequence;
import io.intino.ime.box.workspaces.WorkspaceManager;
import io.intino.languagearchetype.Archetype;

public class ImeBox extends AbstractBox {
	private Archetype archetype;
	private LanguageLoader languageLoader;
	private LanguageManager languageManager;
	private WorkspaceManager workspaceManager;
	private CommandsFactory commandsFactory;
	private AmidasOauthAccessor amidasOauthAccessor;

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
		languageLoader = new LanguageLoader(archetype.repository().languages().root(), url(configuration.languageArtifactory()));
		languageManager = new LanguageManager(archetype);
		workspaceManager = new WorkspaceManager(archetype, languageManager);
		WorkspaceSequence.init(archetype.configuration().workspaceSequence());
	}

	@Override
	protected void beforeSetupImeElementsUi(io.intino.alexandria.ui.UISpark sparkInstance) {
		LanguageServerWebSocketHandler handler = new LanguageServerWebSocketHandler(new LanguageServerFactory(languageLoader), workspaceManager);
		sparkInstance.service().webSocket("/dsl/tara", handler);
	}

	public void afterStart() {
	}

	public void beforeStop() {
	}

	public void afterStop() {
	}

	public LanguageLoader languageProvider() {
		return this.languageLoader;
	}

	public LanguageManager languageManager() {
		return languageManager;
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
		if (authServiceUrl == null) return null;
		if (amidasOauthAccessor == null) amidasOauthAccessor = new AmidasOauthAccessor(new Space(url(configuration().url())).name("quasar-ime"), authServiceUrl);
		return amidasOauthAccessor;
	}

	public AmidasOauthAccessor authService() {
		return amidasOauthAccessor;
	}
}