package io.quassar.editor;

import io.intino.alexandria.Resource;
import io.intino.alexandria.exceptions.InternalServerError;
import io.intino.alexandria.exceptions.NotFound;
import io.intino.alexandria.logger.Logger;
import io.intino.builderservice.schemas.Message;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.builder.BuildResult;
import io.quassar.editor.box.builder.ModelBuilder;
import io.quassar.editor.box.util.ArchetypeHelper;
import io.quassar.editor.box.util.LanguageHelper;
import io.quassar.editor.box.util.TarHelper;
import io.quassar.editor.box.util.VersionNumberComparator;
import io.quassar.editor.model.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

public class ResetLanguages {

	private static final List<String> Languages = new ArrayList<>() {{
		add("monentia.anaga"); add("monet.orfeo"); add("monet.woven");
	}};

	public static void main(String[] args) throws IOException {
		EditorBox box = new EditorBox(args());
		box.start();
		resetLanguages(box);
		resetModels(box);
		rebuildArtifacts(box);
		box.stop();
		System.out.println("END!!!!!");
	}

	private static void resetLanguages(EditorBox box) {
		Languages.forEach(l -> resetLanguage(l, box));
	}

	private static void resetLanguage(String languageKey, EditorBox box) {
		Language language = box.languageManager().get(languageKey);
		List<LanguageRelease> releases = language.releases().stream().sorted((o1, o2) -> VersionNumberComparator.getInstance().compare(o1.version(), o2.version())).toList();
		IntStream.range(0, releases.size()-1).forEach(index -> box.languageManager().removeRelease(language, releases.get(index)));
		File release = box.archetype().languages().release(languageKey, language.lastRelease().version());
		release.renameTo(new File(release.getAbsolutePath().replace(language.lastRelease().version(), "1.0.0")));
		box.languageManager().renameRelease(language, language.lastRelease(), "1.0.0");
		resetMetamodel(language, box);
	}

	private static void resetModels(EditorBox box) {
		Languages.forEach(l -> {
			Language language = box.languageManager().get(l);
			List<Model> models = box.modelManager().models(language);
			models.forEach(m -> resetModel(m, language, box));
		});
	}

	private static void resetMetamodel(Language language, EditorBox box) {
		resetReleases(box.modelManager().get(language.metamodel()), language, box);
	}

	private static void resetModel(Model model, Language language, EditorBox box) {
		model.language(new GavCoordinates(language.collection(), language.name(), "1.0.0"));
		resetReleases(model, language, box);
	}

	private static void resetReleases(Model model, Language language, EditorBox box) {
		List<ModelRelease> releases = model.releases().stream().sorted((o1, o2) -> VersionNumberComparator.getInstance().compare(o1.version(), o2.version())).toList();
		if (releases.isEmpty() || releases.size() == 1) return;
		IntStream.range(0, releases.size()-1).forEach(index -> box.modelManager().removeRelease(model, releases.get(index).version()));
		File release = box.archetype().models().release(ArchetypeHelper.relativeModelPath(model.id()), model.id(), model.lastRelease().version());
		release.renameTo(new File(release.getAbsolutePath().replace(model.lastRelease().version(), "1.0.0")));
		box.modelManager().renameRelease(model, model.lastRelease(), "1.0.0");
	}

	private static void rebuildArtifacts(EditorBox box) {
		Languages.forEach(l -> rebuildArtifacts(l, box));
	}

	private static void rebuildArtifacts(String languageKey, EditorBox box) {
		Language language = box.languageManager().get(languageKey);
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
			BuildResult result = new ModelBuilder(model, Model.DraftRelease, new GavCoordinates(language.collection(), language.name(), release.version()), box).build(author);
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
