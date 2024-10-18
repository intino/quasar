package io.intino.ls.document;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import io.intino.alexandria.logger.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RebaseResult.Status;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.transport.ssh.jsch.JschConfigSessionFactory;
import org.eclipse.jgit.transport.ssh.jsch.OpenSshConfig;
import org.eclipse.jgit.util.FS;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.StreamSupport;

import static com.jcraft.jsch.OpenSSHConfig.parseFile;

public class GitRepository {
	public static final String REMOTE = "origin";
	private final CredentialsProvider credentialsProvider;
	private final String branch;
	private final Git git;
	private static final CustomJschConfigSessionFactory sshFactory;
	private static final TransportConfigCallback configCallback;

	static {
		sshFactory = new CustomJschConfigSessionFactory();
		SshSessionFactory.setInstance(sshFactory);
		configCallback = transport -> {
			if (transport instanceof SshTransport t) t.setSshSessionFactory(sshFactory);
		};
	}

	public GitRepository(File directory, String remoteUri, CredentialsProvider credentialsProvider, String branch) throws GitAPIException, URISyntaxException {
		this.credentialsProvider = credentialsProvider;
		this.branch = branch;
		this.git = new File(directory, ".git").exists() ?
				init(directory, remoteUri, branch) :
				clone(directory, remoteUri, branch);
	}

	private Git clone(File directory, String remoteUri, String branch) throws GitAPIException {
		Logger.debug("Cloning repository " + remoteUri + " : " + branch);
		return Git.cloneRepository()
				.setCredentialsProvider(credentialsProvider)
				.setTransportConfigCallback(configCallback)
				.setDirectory(directory)
				.setURI(remoteUri)
				.setRemote(REMOTE)
				.setNoTags()
				.setBranch(branch).call();
	}

	private Git init(File directory, String remoteUri, String branch) throws GitAPIException, URISyntaxException {
		if (remoteUri == null) return null;
		Git git = Git.init().setDirectory(directory).setInitialBranch(branch).call();
		git.remoteSetUrl().setRemoteName(REMOTE).setRemoteUri(new URIish(remoteUri)).call();
		git.fetch()
				.setCredentialsProvider(credentialsProvider)
				.setRemote(REMOTE)
				.call();
		return git;
	}

	public PullResult checkoutAndPull() {
		checkout(branch);
		return pull();
	}

	public RevCommit commit(String user) throws GitAPIException {
		Logger.debug("Commiting...");
		return git.commit()
				.setMessage("Commit done by " + user) // Mensaje del commit
				.setCredentialsProvider(credentialsProvider)
				.setAuthor(new PersonIdent("Tu Nombre", "tu-email@ejemplo.com")) // Autor del commit
				.call();
	}

	public enum PullResult {
		UP_TO_DATE, FAST_FORWARD
	}

	public PullResult pull() {
		try {
			Logger.debug("Pulling repository...");
			var result = git.pull().setCredentialsProvider(credentialsProvider).setRebase(true).setRemote(REMOTE).call();
			if (result.isSuccessful())
				if (result.getRebaseResult().getStatus().equals(Status.FAST_FORWARD)) return PullResult.FAST_FORWARD;
			Logger.debug("Repository pulled successfully");
			return PullResult.UP_TO_DATE;
		} catch (GitAPIException e) {
			Logger.error(e);
			return PullResult.UP_TO_DATE;
		}
	}

	public List<PushResult> push() {
		try {
			Logger.debug("Pushing changes to remote repository...");
			var result = git.push().setCredentialsProvider(credentialsProvider).setRemote(REMOTE).call();
			return StreamSupport.stream(result.spliterator(), false).toList();
		} catch (GitAPIException e) {
			Logger.error(e);
			return List.of();
		}
	}

	public GitRepository checkout(String branch) {
		try {
			git.fetch().setCredentialsProvider(credentialsProvider).setTransportConfigCallback(configCallback).call();
			Logger.debug("Checking out to branch " + branch);
			List<Ref> call = git.branchList().call();
			git.checkout().setCreateBranch(call.stream().noneMatch(b -> b.getName().endsWith("/" + branch))).setName(branch).call();
			Logger.debug("Repository checked out to " + branch + " successfully");
		} catch (GitAPIException e) {
			Logger.error(e);
		}
		return this;
	}

	public static class CustomJschConfigSessionFactory extends JschConfigSessionFactory {
		@Override
		protected void configure(OpenSshConfig.Host host, Session session) {
			session.setConfig("StrictHostKeyChecking", "false");
			session.setConfig("StrictHostKeyChecking", "no");
		}

		@Override
		protected JSch createDefaultJSch(FS fs) throws JSchException {
			JSch defaultJSch = super.createDefaultJSch(fs);
			defaultJSch.removeAllIdentity();
			File home = fs.userHome();
			try {
				defaultJSch.addIdentity(new File(home, ".ssh/id_rsa").getAbsolutePath());
				defaultJSch.setKnownHosts(new File(home, ".ssh/known_hosts").getAbsolutePath());
				File config = new File(home, ".ssh/config");
				if (config.exists()) defaultJSch.setConfigRepository(parseFile(config.getAbsolutePath()));
			} catch (IOException e) {
				Logger.error(e);
			}
			return defaultJSch;
		}
	}
}