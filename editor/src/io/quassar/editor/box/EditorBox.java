package io.quassar.editor.box;

import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.ui.AlexandriaUiServer;
import io.quassar.archetype.Archetype;
import io.quassar.editor.box.commands.Commands;
import io.quassar.editor.box.commands.CommandsFactory;
import io.quassar.editor.box.languages.LanguageLoader;
import io.quassar.editor.box.languages.LanguageManager;
import io.quassar.editor.box.languages.LanguageServerManager;
import io.quassar.editor.box.languages.LanguageServerWebSocketHandler;
import io.quassar.editor.box.languages.artifactories.LocalLanguageArtifactory;
import io.quassar.editor.box.models.ModelManager;
import io.quassar.editor.box.projects.ProjectManager;
import io.quassar.editor.box.users.UserManager;
import io.quassar.editor.model.Model;
import io.quassar.editor.model.Utilities;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.lsp4j.services.LanguageServer;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class EditorBox extends AbstractBox {
	private final Archetype archetype;
	private LanguageLoader languageLoader;
	private LanguageManager languageManager;
	private ModelManager modelManager;
	private UserManager userManager;
	private ProjectManager projectManager;
	private CommandsFactory commandsFactory;
	private LanguageServerManager serverManager;
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
		utilities = new Utilities(archetype.configuration().editor().utilities());
		commandsFactory = new CommandsFactory(this);
		languageLoader = new LanguageLoader(new LocalLanguageArtifactory(archetype));
		languageManager = new LanguageManager(archetype);
		serverManager = new LanguageServerManager(languageLoader, (model, version) -> workSpaceOf(model, version));
		modelManager = new ModelManager(archetype, serverManager);
		userManager = new UserManager(archetype);
		projectManager = new ProjectManager(archetype);
	}

	private URI workSpaceOf(Model model, String version) {
		return modelManager.workspace(model, version);
	}

	public void afterStart() {
	}

	public void beforeStop() {
	}

	public void afterStop() {
	}

	public Utilities utilities() {
		return utilities;
	}

	public Archetype archetype() {
		return archetype;
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

	protected io.intino.alexandria.ui.services.AuthService authService(java.net.URL authServiceUrl) {
		//TODO add your authService
		return null;
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
			Model model = modelManager.get(parameterMap.get("dsl").getFirst(), parameterMap.get("model").getFirst());
			return serverManager.get(model, parameterMap.get("model-version").getFirst());
		} catch (IOException | GitAPIException | URISyntaxException e) {
			Logger.error(e);
			return null;
		}
	}

}