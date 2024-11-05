package io.intino.ls.document;

import io.intino.alexandria.logger.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public class GitDocumentManager extends FileDocumentManager {
	private final GitRepository repository;

	public GitDocumentManager(File root, String branch, URL gitUrl, String username, String token) throws IOException, GitAPIException, URISyntaxException {
		super(root);
		this.repository = new GitRepository(root, gitUrl.toString(), credentialsProviderOf(username, token),  branch);
	}

	private CredentialsProvider credentialsProviderOf(String username, String token) {
		return null; // TODO OR
	}

	@Override
	public void commit(String user) {
		try {
			repository.commit(user);
		} catch (GitAPIException e) {
			Logger.error(e);
		}
	}

	@Override
	public void push() {
		repository.push();
	}
}