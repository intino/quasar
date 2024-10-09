package io.intino.ls.document;

import io.intino.alexandria.logger.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.lsp4j.TextDocumentItem;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GitDocumentManager extends FileDocumentManager {
	private final Object lock = new Object();
	private final CredentialsProvider credentialsProvider;

	public GitDocumentManager(File root, URL gitUrl, CredentialsProvider credentialsProvider) throws IOException {
		super(root);
		this.credentialsProvider = credentialsProvider;
	}


	@Override
	public void commit() {
	}



}