package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.exceptions.*;
import io.intino.alexandria.ui.server.UIFile;
import io.quassar.editor.box.*;
import io.quassar.editor.box.schemas.*;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.ui.displays.templates.AbstractDownloadModelDialog;
import io.quassar.editor.box.util.DisplayHelper;
import io.quassar.editor.box.util.ModelHelper;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.model.Model;

import java.io.File;

public class DownloadModelDialog extends AbstractDownloadModelDialog<EditorBox> {
	private Model model;
	private String release;

	public DownloadModelDialog(EditorBox box) {
		super(box);
	}

	public void model(Model model) {
		this.model = model;
	}

	public void release(String release) {
		this.release = release;
	}

	public void open() {
		if (model.isPublic()) dialog.open();
		else downloadSilently.launch();
	}

	@Override
	public void init() {
		super.init();
		download.onExecute(e -> download());
		downloadSilently.onExecute(e -> download());
		dialog.onOpen(e -> refreshDialog());
	}

	private void refreshDialog() {
		dialog.title(translate("Download %s").formatted(ModelHelper.label(model, language(), box())));
		modelUrl.value(PathHelper.downloadModelUrl(model, release, session()));
		copyModelUrl.text(PathHelper.downloadModelUrl(model, release, session()));
	}

	private UIFile download() {
		File release = box().modelManager().release(model, this.release);
		return DisplayHelper.uiFile(ModelHelper.label(model, language(), box()) + "-" + release.getName(), release);
	}
}