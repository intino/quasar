package io.intino.ime.box.models;

import io.intino.alexandria.logger.Logger;
import io.intino.ime.box.util.WorkspaceHelper;
import io.intino.ime.model.Model;
import io.intino.ls.IntinoLanguageServer;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.WorkspaceSymbol;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageServer;

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ModelContainerReader {
	private final Model model;
	private final LanguageServer server;

	public ModelContainerReader(Model model, LanguageServer server) {
		this.model = model;
		this.server = server;
	}

	public List<ModelContainer.File> files() {
		try {
			Either<List<? extends SymbolInformation>, List<? extends WorkspaceSymbol>> symbols = server.getWorkspaceService().symbol(new WorkspaceSymbolParams()).get();
			return WorkspaceHelper.filesOf(symbols.getRight());
		} catch (InterruptedException | ExecutionException e) {
			Logger.error(e);
			return Collections.emptyList();
		}
	}

	public String content(String uri) {
		try(InputStream content = ((IntinoLanguageServer) server).getWorkspaceService().content(URI.create(uri))) {
			return new String(content.readAllBytes(), Charset.defaultCharset());
		} catch (Exception e) {
			Logger.error(e);
			return "";
		}
	}

}
