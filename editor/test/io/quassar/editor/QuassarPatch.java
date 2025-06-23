package io.quassar.editor;

import io.intino.alexandria.Resource;
import io.intino.alexandria.exceptions.InternalServerError;
import io.intino.alexandria.exceptions.NotFound;
import io.intino.alexandria.logger.Logger;
import io.intino.builderservice.schemas.Message;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.builder.BuildResult;
import io.quassar.editor.box.builder.ModelBuilder;
import io.quassar.editor.box.util.LanguageHelper;
import io.quassar.editor.box.util.TarHelper;
import io.quassar.editor.model.*;
import io.quassar.editor.model.Collection.SubscriptionPlan;
import systems.intino.datamarts.subjectstore.SubjectStore;
import systems.intino.datamarts.subjectstore.model.Subject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.*;

public class QuassarPatch {

	private static final Map<String, Set<String>> CollectionsPro = new HashMap<>() {{
		put("monentia", Set.of("picota", "anaga"));
		put("monet", Set.of("visora", "garaxia", "woven", "orfeo"));
		put("misc", Set.of("xxx", "yyy", "uuu"));
		put("ulpgc", Set.of("flogo"));
	}};

	private static final Map<String, Set<String>> Collections = new HashMap<>() {{
		put("monentia", Set.of("picota"));
		put("monet", Set.of("visora", "garaxia", "woven"));
		put("misc", Set.of("exampledsl"));
	}};

	private static final Map<String, String> CollectionOwnersPro = new HashMap<>() {{
		put("monentia", "josejuanhernandez@gmail.com");
		put("monet", "josejuanhernandez@gmail.com");
		put("misc", "josejuanhernandez@gmail.com");
		put("ulpgc", "josejuanhernandez@gmail.com");
	}};

	private static final Map<String, String> CollectionOwners = new HashMap<>() {{
		put("monentia", "mcaballero");
		put("monet", "mcaballero");
		put("misc", "mcaballero");
	}};

	private static final Map<String, List<String>> CollectionCollaboratorsPro = new HashMap<>() {{
		put("monentia", List.of("mcaballero@gmail.com", "octavioroncal11@gmail.com", "naitsirc98@gmail.com"));
		put("monet", List.of("mcaballero@gmail.com"));
		put("misc", List.of("mcaballero@gmail.com", "octavioroncal11@gmail.com"));
		put("ulpgc", List.of("mcaballero@gmail.com"));
	}};

	private static final Map<String, List<String>> CollectionCollaborators = new HashMap<>() {{
		put("monentia", List.of("oroncal"));
		put("monet", List.of("oroncal"));
		put("misc", List.of("oroncal"));
	}};

	public static void main(String[] args) throws IOException {
		File root = new File("/Users/mcaballero/Proyectos/quassar/temp");
		prepareIndex(root);
		renameDsls(root);
		rebuildArtifacts();
		System.out.println("END!!!!!");
	}

	private static void prepareIndex(File root) throws IOException {
		File indexFile = new File(root, "index.triples");
		SubjectStore store = new SubjectStore(indexFile);
		store.seal();
		store.open("metta", "dsl").update().set("collection", "quassar").del("group");
		updateDsls(store);
		updateModels(store);
		createCollections(store);
		store.seal();
		renameDslsInIndex(indexFile);
	}

	private static void updateDsls(SubjectStore store) {
		store.query("dsl").where("parent-name").equals("metta").stream().forEach(s -> s.update().set("parent-collection", "quassar").del("parent-group"));
		Collections.forEach((key, value) -> value.forEach(dsl -> store.open(dsl, "dsl").update().set("collection", key).del("group")));
	}

	private static void updateModels(SubjectStore store) {
		store.query("model").where("visibility").equals("Public").stream().forEach(s -> s.update().del("visibility"));
		store.query("model").where("dsl-name").equals("metta").stream().forEach(s -> s.update().set("dsl-collection", "quassar").del("dsl-group"));
		Collections.forEach((key, value) -> value.forEach(dsl -> {
			store.query("model").where("dsl-name").equals(dsl).stream().forEach(s -> s.update().set("dsl-collection", key).del("dsl-group"));
		}));
	}

	private static void createCollections(SubjectStore store) {
		Collections.keySet().forEach(c -> {
			Subject subject = store.create(c, "collection");
			Subject.Updating update = subject.update();
			update.set("name", c).
					set("owner", CollectionOwners.get(c)).
					set("subscription-plan", c.equals("monentia") ? SubscriptionPlan.Enterprise.name() : SubscriptionPlan.Professional.name()).
					set("create-date", Instant.now().toString()).
					set("update-date", Instant.now().toString());
			CollectionCollaborators.get(c).forEach(collab -> update.set("collaborator", collab));
		});
	}

	private static void renameDslsInIndex(File file) throws IOException {
		List<String> lines = Files.readAllLines(file.toPath());
		List<String> result = new ArrayList<>();
		lines.forEach(l -> {
			if (!l.matches("^.*\\.dsl.*")) {
				result.add(l);
				return;
			}
			for (Map.Entry<String, Set<String>> entry : Collections.entrySet()) {
				String key = entry.getKey();
				Set<String> value = entry.getValue();
				for (String dsl : value) {
					l = l.replace(dsl + ".dsl", key + "." + dsl + ".dsl");
				}
			}
			result.add(l);
		});
		Files.delete(file.toPath());
		Files.writeString(file.toPath(), String.join("\n", result));
	}

	private static void renameDsls(File root) {
		File dslsDir = new File(root, "dsls");
		Collections.forEach((key, value) -> value.forEach(dsl -> new File(dslsDir, dsl).renameTo(new File(dslsDir,  key + "." + dsl))));
	}

	private static void rebuildArtifacts() {
		EditorBox box = new EditorBox(args());
		box.start();
		Collections.forEach((key, value) -> value.forEach(dsl -> rebuildArtifacts(key, dsl, box)));
	}

	private static void rebuildArtifacts(String collection, String dsl, EditorBox box) {
		Language language = box.languageManager().get(Language.key(collection, dsl));
		language.releases().forEach(r -> rebuild(language, r, User.Quassar, box));
	}

	private static String[] args() {
		return new String[] {
			"port=8080",
			"url=http://localhost:8080",
			"home=./temp",
			"title=Quassar",
			"federation-url=",
			"google-client-id=910085173896-4b9kmbi0vapnffbas814ab2ruvg7cm36.apps.googleusercontent.com",
			"language-artifactory=https://artifactory.intino.io/artifactory/releases",
			"language-repository=/Users/mcaballero/.m2",
			"builder-service-url=http://localhost:9002",
			"new-user-license-time=60",
			"renew-license-time-duration=12",
			"collection-collaborators-count=4",
			"tara-builder=io.intino.tara.builder:1.3.0",
			"quassar-builder=quassar625/io.quassar.quassar-builder:1.0.0"
		};
	}

	private static BuildResult rebuild(Language language, LanguageRelease release, String author, EditorBox box) {
		File destination = null;
		try {
			System.out.println("Building " + language.name() + ":" + release.version());
			Model model = box.modelManager().get(language.metamodel());
			BuildResult result = new ModelBuilder(model, release.version(), new GavCoordinates(language.collection(), language.name(), release.version()), box).build(author);
			if (!result.success()) return result;
			Resource resource = result.zipArtifacts();
			destination = box.archetype().tmp().builds(UUID.randomUUID().toString());
			TarHelper.extract(resource.inputStream(), destination);
			box.languageManager().saveDsl(language, release.version(), LanguageHelper.dslOf(destination));
			box.languageManager().saveDslManifest(language, release.version(), LanguageHelper.dslManifestOf(destination));
			box.languageManager().saveGraph(language, release.version(), LanguageHelper.graphOf(destination));
			box.languageManager().saveParsers(language, release.version(), LanguageHelper.parsersOf(destination));
			return result;
		}
		catch (Exception | InternalServerError | NotFound e) {
			Logger.error(e);
			return BuildResult.failure(List.of(new Message().content(e.getMessage()).kind(Message.Kind.ERROR)));
		} finally {
			if (destination != null) destination.delete();
		}
	}

}
