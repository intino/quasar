package io.intino.ime.box;

import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.ui.services.auth.Space;
import io.intino.amidas.accessor.alexandria.core.AmidasOauthAccessor;
import io.intino.builderservice.QuassarBuilderServiceAccessor;
import io.intino.ime.box.commands.Commands;
import io.intino.ime.box.commands.CommandsFactory;
import io.intino.ime.box.languages.LanguageLoader;
import io.intino.ime.box.languages.LanguageManager;
import io.intino.ime.box.languages.LanguageServerManager;
import io.intino.ime.box.languages.LanguageServerWebSocketHandler;
import io.intino.ime.box.models.ModelManager;
import io.intino.ime.box.users.TokenProvider;
import io.intino.ime.box.util.ModelSequence;
import io.intino.ime.model.Model;
import io.intino.languagearchetype.Archetype;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.lsp4j.services.LanguageServer;

import java.io.IOException;
import java.net.URISyntaxException;

public class ImeBox extends AbstractBox {
	private Archetype archetype;
	private LanguageLoader languageLoader;
	private LanguageManager languageManager;
	private ModelManager modelManager;
	private CommandsFactory commandsFactory;
	private AmidasOauthAccessor amidasOauthAccessor;
	private LanguageServerManager serverManager;
	private QuassarBuilderServiceAccessor builderServiceAccessor;
	private TokenProvider tokenProvider;

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
		serverManager = new LanguageServerManager(languageLoader, model -> modelManager.workspace(model), user -> tokenProvider.of(user).gitHubToken());
		modelManager = new ModelManager(archetype, serverManager);
		builderServiceAccessor = new QuassarBuilderServiceAccessor(url(configuration.builderServiceUrl()));
		tokenProvider = new TokenProvider(archetype.configuration().userTokens());
		ModelSequence.init(archetype.configuration().modelSequence());
	}

	@Override
	protected void beforeSetupImeElementsUi(io.intino.alexandria.ui.UISpark sparkInstance) {
		sparkInstance.service().webSocket("/dsl/tara", new LanguageServerWebSocketHandler(this::serverFor));
	}

	public QuassarBuilderServiceAccessor builderService() {
		return builderServiceAccessor;
	}

	public TokenProvider tokenProvider() {
		return tokenProvider;
	}

	public LanguageServerManager serverManager() {
		return serverManager;
	}

	private LanguageServer serverFor(Session session) {
		try {
			Model model = modelManager.model(session.getUpgradeRequest().getParameterMap().get("model").getFirst());
			return serverManager.get(model);
		} catch (IOException | GitAPIException | URISyntaxException e) {
			Logger.error(e);
			return null;
		}
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

	public ModelManager modelManager() {
		return modelManager;
	}

	public <F extends Commands> F commands(Class<F> clazz) {
		return commandsFactory.command(clazz);
	}

	protected io.intino.alexandria.ui.services.AuthService authService(java.net.URL authServiceUrl) {
		if (authServiceUrl == null) return null;
		if (amidasOauthAccessor == null)
			amidasOauthAccessor = new AmidasOauthAccessor(new Space(url(configuration().url())).name("quasar-ime"), authServiceUrl);
		return amidasOauthAccessor;
	}

	public AmidasOauthAccessor authService() {
		return amidasOauthAccessor;
	}

}