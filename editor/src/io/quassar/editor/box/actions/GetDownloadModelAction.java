package io.quassar.editor.box.actions;

import io.intino.alexandria.Resource;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.exceptions.*;

import java.io.File;
import java.io.FileInputStream;

import io.quassar.editor.box.util.ArchetypeHelper;
import io.quassar.editor.box.util.DisplayHelper;
import io.quassar.editor.box.util.ModelHelper;
import io.quassar.editor.box.util.ZipHelper;
import io.quassar.editor.model.Model;
import io.quassar.editor.model.ModelRelease;

public class GetDownloadModelAction extends QuassarAction {
	public String commit;

	public io.intino.alexandria.Resource execute() throws Forbidden {
		Model model = box.modelManager().find(commit);
		ModelRelease release = box.modelManager().findRelease(commit);

		if (model == null || release == null) {
			Logger.error("Model commit %s not found".formatted(commit));
			return DisplayHelper.emptyFile();
		}

		try {
			File result = box.modelManager().release(model, release.version());
			return new Resource(ModelHelper.label(model, "es", box) + ".zip", new FileInputStream(result));
		} catch (Exception e) {
			Logger.error(e);
			return DisplayHelper.emptyFile();
		}
	}

}