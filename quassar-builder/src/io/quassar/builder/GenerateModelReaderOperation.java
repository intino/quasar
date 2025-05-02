package io.quassar.builder;

import io.intino.builder.CompilerConfiguration;
import io.intino.itrules.formatters.StringFormatters;
import io.intino.magritte.builder.compiler.operations.LayerGenerationOperation;
import io.intino.tara.Language;
import io.intino.tara.builder.LanguageLoader;
import io.intino.tara.builder.core.CompilationUnit;
import io.intino.tara.builder.core.errorcollection.CompilationFailedException;
import io.intino.tara.builder.core.errorcollection.TaraException;
import io.intino.tara.builder.core.operation.model.ModelOperation;
import io.intino.tara.builder.utils.FileSystemUtils;
import io.intino.tara.model.Level;
import io.intino.tara.processors.model.Model;
import io.quassar.builder.modelreader.ModelReaderGenerator;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import static io.intino.builder.BuildConstants.DSL_VERSION;
import static io.intino.builder.BuildConstants.GENERATION_PACKAGE;
import static io.intino.builder.CompilerConfiguration.LANGUAGE_PACKAGE;
import static io.intino.tara.model.Level.M2;
import static java.io.File.separator;

public class GenerateModelReaderOperation extends ModelOperation {
	private final CompilerConfiguration configuration;

	public GenerateModelReaderOperation(CompilationUnit unit) {
		super(unit);
		this.configuration = unit.configuration();
	}

	@Override
	public void call(Model model) throws CompilationFailedException {
		if (model.mograms().isEmpty() || model.mograms().stream().allMatch(m -> m.level() == Level.M1)) return;
		new LayerGenerationOperation(unit).call(model);
		if (isM2(model) && hasM3()) generateMetaModel();
		new ModelReaderGenerator(unit.configuration()).generate();
		copyLanguageAsBuildItem();
	}

	private void copyLanguageAsBuildItem() {
		try {
			File file = new File(configuration.outDirectory().getParentFile(), "build/language");
			file.mkdirs();
			FileSystemUtils.copyDir(languageDirectory().getAbsolutePath(), file.getAbsolutePath());
		} catch (FileSystemException e) {
			throw new RuntimeException(e);
		}
	}

	private void generateMetaModel() {
		String version = metalanguageVersion(unit.language().metaLanguage(), configuration.localRepository());
		if (version == null) return;
		Language metalanguage = load(unit.language().metaLanguage(), version);
		if (metalanguage == null) return;
		File languageFile = LanguageLoader.getLanguagePath(metalanguage.languageName(), configuration.dsl().version(), configuration.localRepository().getAbsolutePath());
		String generationPackage = metalanguageGenerationPackage(metalanguage.languageName(), languageFile);
		if (generationPackage == null) return;
		this.unit.configuration().generationPackage(generationPackage);
		new LayerGenerationOperation(unit.setLanguage(metalanguage)).call((Model) unit.language().model());
	}

	private boolean hasM3() {
		return !unit.language().languageName().equalsIgnoreCase("Meta");
	}

	private static boolean isM2(Model model) {
		return model.mograms().stream().anyMatch(m -> m.level().equals(M2));
	}

	private File languageDirectory() {
		return new File(configuration.localRepository(), LANGUAGE_PACKAGE.replace(".", separator) + separator +
				StringFormatters.camelCase().format(configuration.dsl().outDsl()).toString().toLowerCase() + separator + (configuration.version() == null ? "1.0.0" : configuration.version()));
	}

	private Language load(String metalanguage, String version) {
		try {
			var localRepo = this.configuration.localRepository().getAbsolutePath();

			if (version == null) return null;
			return LanguageLoader.load(metalanguage, version, localRepo);
		} catch (TaraException e) {
			return null;
		}
	}

	private String metalanguageVersion(String name, File file) {
		if (file.isDirectory() || !file.exists()) return null;
		else {
			try (JarFile jarFile = new JarFile(file)) {
				Manifest manifest = jarFile.getManifest();
				final Attributes tara = manifest.getAttributes("tara");
				return tara == null ? name : tara.getValue(DSL_VERSION);
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}
		return null;
	}

	String metalanguageGenerationPackage(String name, File file) {
		if (file.isDirectory() || !file.exists()) return null;
		try (JarFile jarFile = new JarFile(file)) {
			Manifest manifest = jarFile.getManifest();
			final Attributes tara = manifest.getAttributes("tara");
			return tara == null ? name : tara.getValue(GENERATION_PACKAGE.replace(".", "-"));
		} catch (IOException e) {
			System.out.println(e.getMessage());
			return null;
		}
	}
}
