package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.ui.displays.IntinoFileBrowser;

public class BrowserTemplate extends AbstractBrowserTemplate<EditorBox> {
	private IntinoFileBrowser browser;

	public BrowserTemplate(EditorBox box) {
		super(box);
	}

	public IntinoFileBrowser fileBrowser() {
		return browser;
	}

	@Override
	public void init() {
		super.init();
		fileBrowser.display(new IntinoFileBrowser(box()));
		browser = fileBrowser.display();
	}

}