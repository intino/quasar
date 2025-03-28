package io.quassar.editor.box.actions;

import io.intino.alexandria.Resource;
import io.intino.alexandria.logger.Logger;
import io.quassar.editor.box.EditorBox;
import io.intino.alexandria.exceptions.*;

import java.io.File;
import java.io.FileInputStream;

import io.quassar.editor.box.util.DisplayHelper;
import io.quassar.editor.box.util.ZipHelper;
import io.quassar.editor.model.Model;

public class GetDownloadModelAction implements io.intino.alexandria.rest.RequestErrorHandler {
	public EditorBox box;
	public io.intino.alexandria.http.server.AlexandriaHttpContext context;
	public String language;
	public String model;
	public String release;
	public String token;

	public io.intino.alexandria.Resource execute() throws Forbidden {
		Model quassarModel = box.modelManager().get(language, model);
		if (quassarModel == null) {
			Logger.error("Model %s not found".formatted(model));
			return DisplayHelper.emptyFile();
		}

		if (!check(quassarModel)) throw new Forbidden("You dont have access to download this model release");

		try {
			File workspace = box.archetype().languages().workspace(language, model);
			File result = new File(box.archetype().tmp().root(), "%s-%s.zip".formatted(model, release));
			ZipHelper.zipFolder(workspace.toPath(), result.toPath());
			return new Resource(result.getName(), new FileInputStream(result));
		} catch (Exception e) {
			Logger.error(e);
			return DisplayHelper.emptyFile();
		}
	}

	private boolean check(Model quassarModel) {
		if (quassarModel.isPublic()) return true;
		return quassarModel.tokens().values().stream().anyMatch(t -> t.equals(token));
	}

	public void onMalformedRequest(Throwable e) throws AlexandriaException {
		throw new BadRequest("Malformed request");
	}
}