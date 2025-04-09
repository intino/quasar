package io.quassar.editor.box.actions;

import io.intino.alexandria.Resource;
import io.intino.alexandria.exceptions.Forbidden;
import io.intino.alexandria.logger.Logger;
import io.quassar.editor.box.util.DisplayHelper;
import io.quassar.editor.model.Model;

import java.io.InputStream;

public class GetDownloadModelFileAction extends QuassarAction {
	public String model;
	public String release;
	public String file;

	public io.intino.alexandria.Resource execute() throws Forbidden {
		Model quassarModel = box.modelManager().get(model);
		if (quassarModel == null) {
			Logger.error("Model %s not found".formatted(model));
			return DisplayHelper.emptyFile();
		}

		if (!check(quassarModel)) throw new Forbidden("You dont have access to download this model release");

		try {
			InputStream content = box.modelManager().content(quassarModel, release, file);
			return new Resource(file, content);
		} catch (Exception e) {
			Logger.error(e);
			return DisplayHelper.emptyFile();
		}
	}

}