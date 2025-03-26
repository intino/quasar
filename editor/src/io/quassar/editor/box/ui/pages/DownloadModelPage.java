package io.quassar.editor.box.ui.pages;

import io.intino.alexandria.Resource;
import io.intino.alexandria.logger.Logger;
import io.quassar.editor.box.util.DisplayHelper;
import io.quassar.editor.box.util.ZipHelper;
import io.quassar.editor.model.Model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DownloadModelPage extends AbstractDownloadModelPage {
	public String language;
	public String model;
	public String release;

	public io.intino.alexandria.Resource execute() {
		Model quassarModel = box.modelManager().get(language, model);

		if (quassarModel == null) {
			Logger.error("Model %s not found".formatted(model));
			return DisplayHelper.emptyFile();
		}

		try {
			File workspace = box.archetype().languages().workspace(language, model);
			File result = new File(box.archetype().tmp().root(), "%s.zip".formatted(model));
			ZipHelper.zipFolder(workspace.toPath(), result.toPath());
			return new Resource(result.getName(), new FileInputStream(result));
		} catch (Exception e) {
			Logger.error(e);
			return DisplayHelper.emptyFile();
		}
	}

}