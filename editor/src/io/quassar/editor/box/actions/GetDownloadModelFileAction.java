package io.quassar.editor.box.actions;

import io.intino.alexandria.Resource;
import io.intino.alexandria.exceptions.Forbidden;
import io.intino.alexandria.logger.Logger;
import io.quassar.editor.box.util.DisplayHelper;
import io.quassar.editor.model.Model;
import io.quassar.editor.model.ModelRelease;

import java.io.InputStream;

public class GetDownloadModelFileAction extends QuassarAction {
	public String commit;
	public String file;

	public io.intino.alexandria.Resource execute() throws Forbidden {
		Model model = box.modelManager().find(commit);
		ModelRelease release = box.modelManager().findRelease(commit);

		if (model == null || release == null) {
			Logger.error("Model commit %s not found".formatted(model));
			return DisplayHelper.emptyFile();
		}

		try {
			InputStream content = box.modelManager().content(model, release.version(), file);
			return new Resource(file, content);
		} catch (Exception e) {
			Logger.error(e);
			return DisplayHelper.emptyFile();
		}
	}

}