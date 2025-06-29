package io.quassar.editor.box;

import io.intino.alexandria.exceptions.InternalServerError;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.ui.AlexandriaUiServer;
import io.intino.alexandria.ui.services.auth.Space;
import io.intino.amidas.accessor.alexandria.core.AmidasOauthAccessor;
import io.intino.builderservice.QuassarBuilderServiceAccessor;
import io.intino.builderservice.schemas.RegisterBuilder;
import io.quassar.archetype.Archetype;
import io.quassar.editor.box.commands.Commands;
import io.quassar.editor.box.commands.CommandsFactory;
import io.quassar.editor.box.languages.*;
import io.quassar.editor.box.languages.artifactories.LocalLanguageArtifactory;
import io.quassar.editor.box.models.ModelManager;
import io.quassar.editor.box.projects.ProjectManager;
import io.quassar.editor.box.ui.displays.templates.ModelHeaderTemplate;
import io.quassar.editor.box.users.UserManager;
import io.quassar.editor.model.Model;
import io.quassar.editor.model.Utilities;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.lsp4j.services.LanguageServer;
import systems.intino.datamarts.subjectstore.SubjectStore;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.*;

public class EditorBox extends AbstractBox {
	private final Archetype archetype;
	private SubjectStore subjectStore;
	private AmidasOauthAccessor authService;
	private LanguageLoader languageLoader;
	private CollectionManager collectionManager;
	private LanguageManager languageManager;
	private ModelManager modelManager;
	private UserManager userManager;
	private ProjectManager projectManager;
	private CommandsFactory commandsFactory;
	private LanguageServerManager serverManager;
	private QuassarBuilderServiceAccessor builderAccessor;
	private Utilities utilities;

	public EditorBox(String[] args) {
		this(new EditorConfiguration(args));
	}

	public EditorBox(EditorConfiguration configuration) {
		super(configuration);
		this.archetype = new Archetype(configuration.home());
	}

	@Override
	public io.intino.alexandria.core.Box put(Object o) {
		super.put(o);
		return this;
	}

	@Override
	protected void beforeSetupEditorElementsUi(AlexandriaUiServer server) {
		server.registerWs("/dsl/tara", new LanguageServerWebSocketHandler(EditorBox.this::serverFor));
	}

	public void beforeStart() {
		boolean exists = archetype.index().exists();
		if (!exists) new SubjectGenerator(this).generate();
		subjectStore = createSubjectStore();
		utilities = new Utilities(archetype.configuration().editor().utilities());
		commandsFactory = new CommandsFactory(this);
		languageLoader = new LanguageLoader(new LocalLanguageArtifactory(archetype, this::modelWithLanguage));
		collectionManager = new CollectionManager(archetype, subjectStore);
		languageManager = new LanguageManager(archetype, subjectStore, id -> modelManager.get(id));
		serverManager = new LanguageServerManager(languageLoader, this::workSpaceOf).onChangeWorkspace(this::notifyChangeWorkspace);
		modelManager = new ModelManager(archetype, subjectStore, l -> languageManager.get(l), serverManager);
		userManager = new UserManager(archetype, subjectStore, Integer.parseInt(configuration.newUserLicenseTime()));
		projectManager = new ProjectManager(archetype);
		builderAccessor = new QuassarBuilderServiceAccessor(url(configuration.builderServiceUrl()));
		setupServiceBuilder();
	}

	private SubjectStore createSubjectStore() {
		try {
			return new SubjectStore(archetype.index());
		} catch (IOException e) {
			return null;
		}
	}

	private Model modelWithLanguage(String id) {
		String metamodel = languageManager.get(id).metamodel();
		return metamodel != null ? modelManager.get(metamodel) : null;
	}

	private URI workSpaceOf(Model model, String version) {
		return modelManager.workspace(model, version).root().toURI();
	}

	public void afterStart() {
	}

	public void beforeStop() {
		try {
			subjectStore.seal();
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public void afterStop() {
	}

	public Utilities utilities() {
		return utilities;
	}

	public Archetype archetype() {
		return archetype;
	}

	public CollectionManager collectionManager() {
		return collectionManager;
	}

	public LanguageManager languageManager() {
		return languageManager;
	}

	public ModelManager modelManager() {
		return modelManager;
	}

	public UserManager userManager() {
		return userManager;
	}

	public ProjectManager projectManager() {
		return projectManager;
	}

	public QuassarBuilderServiceAccessor builderAccessor() {
		return builderAccessor;
	}

	public AmidasOauthAccessor authService() {
		return authService;
	}

	protected io.intino.alexandria.ui.services.AuthService authService(java.net.URL authServiceUrl) {
		if (authService == null) authService = authServiceUrl != null ? new AmidasOauthAccessor(new Space(url(configuration().url())).name("quasar-editor"), authServiceUrl) : null;
		return authService;
	}

	public <T extends Commands> T commands(Class<T> clazz) {
		return commandsFactory.commands(clazz);
	}

	public boolean isLocalRunning() {
		File[] files = archetype.configuration().root().listFiles();
		return files == null || Arrays.stream(files).filter(f -> !f.getName().startsWith(".")).allMatch(File::isDirectory);
	}

	private LanguageServer serverFor(Session session) {
		try {
			Map<String, List<String>> parameterMap = session.getUpgradeRequest().getParameterMap();
			Model model = modelManager.get(parameterMap.get("model").getFirst());
			return serverManager.get(model, parameterMap.get("model-release").getFirst());
		} catch (IOException | GitAPIException | URISyntaxException e) {
			Logger.error(e);
			return null;
		}
	}

	private void setupServiceBuilder() {
		try {
			builderAccessor.postBuilders(new RegisterBuilder().imageURL(configuration().quassarBuilder()));
		} catch (InternalServerError e) {
			Logger.error(e);
		}
	}

	private void notifyChangeWorkspace(Model model) {
		model.updateDate(Instant.now());
		souls().stream().filter(Objects::nonNull).map(s -> s.displays(ModelHeaderTemplate.class)).flatMap(Collection::stream).filter(t -> t.model().id().equals(model.id())).forEach(h -> h.checked(false));
	}

	public boolean isDebugMode() {
		return configuration.debugMode() != null && Boolean.parseBoolean(configuration.debugMode());
	}
}