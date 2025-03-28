package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.logger.Logger;
import io.quassar.editor.box.EditorBox;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class AboutTemplate extends AbstractAboutTemplate<EditorBox> {

	public AboutTemplate(EditorBox box) {
		super(box);
	}

	public void open() {
		refresh();
	}

	@Override
	public void refresh() {
		super.refresh();
		try {
			File readme = box().archetype().readme();
			if (!readme.exists()) return;
			aboutStamp.content(Files.readString(readme.toPath()));
			aboutStamp.refresh();
		} catch (IOException e) {
			Logger.error(e);
		}
	}
}