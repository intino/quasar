package io.intino.ime.box.orchestator;

import io.intino.alexandria.logger.Logger;
import io.intino.ime.box.scaffolds.ScaffoldFactory;
import io.intino.ls.document.DocumentManager;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ProjectCreator {

	private final String name;
	private final String lang;
	private final String codePackage;
	private final List<CodeBucket> codeBuckets;

	public ProjectCreator(String name, String lang, String codePackage, List<CodeBucket> codeBuckets) {
		this.name = name;
		this.lang = lang;
		this.codePackage = codePackage;
		this.codeBuckets = new ArrayList<>(codeBuckets);
		this.codeBuckets.add(new CodeBucket("graph", null, "io.intino.tara.builder:1.3.0"));
	}

	public void create(DocumentManager manager) {
		try {
			manager.upsertDocument(new URI("quassar"), new QuassarWriter(variables(), entries()).serialize());
			codeBuckets.forEach(b -> buildScaffold(manager, b));
		} catch (URISyntaxException e) {
			Logger.error(e);
		}
	}

	private void buildScaffold(DocumentManager manager, CodeBucket bucket) {
		ScaffoldFactory.scaffoldOf(bucket.scaffold, manager, bucket.path).build();
	}

	private List<ArchetypeEntry> entries() {
		return codeBuckets.stream()
				.map(b -> new ArchetypeEntry(b.path, b.scaffold != null ? b.scaffold.name() : null, List.of(b.builder)))
				.sorted(Comparator.comparing(ArchetypeEntry::path))
				.toList();
	}

	private Map<String, String> variables() {
		return Map.of("lang", lang, "project", name, "package", codePackage);
	}

	public static class CodeBucket {

		private final String path;
		private final ScaffoldFactory.Scaffold scaffold;
		private final String builder;

		public CodeBucket(String path, ScaffoldFactory.Scaffold scaffold, String builder) {
			this.path = path;
			this.scaffold = scaffold;
			this.builder = builder;
		}

		public String path() {
			return path;
		}

		public ScaffoldFactory.Scaffold scaffold() {
			return scaffold;
		}

		public String builder() {
			return builder;
		}
	}
}
