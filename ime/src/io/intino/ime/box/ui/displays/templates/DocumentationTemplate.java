package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.ui.displays.components.Layer;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.PathHelper;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class DocumentationTemplate extends AbstractDocumentationTemplate<ImeBox> {

	public DocumentationTemplate(ImeBox box) {
		super(box);
	}

	@Override
	public void init() {
		super.init();
		openSearchLayerTrigger.onOpen(e -> openSearch(e.layer()));
		header.onOpenSearch(e -> openSearch());
	}

	@Override
	public void refresh() {
		super.refresh();
		header.view(HeaderTemplate.View.Documentation);
		header.refresh();
		documentationStamp.content(content());
		documentationStamp.refresh();
	}

	private static String content() {
		try {
			InputStream stream = DocumentationTemplate.class.getResourceAsStream("/documentation/main.html");
			if (stream == null) return "";
			return IOUtils.toString(stream, StandardCharsets.UTF_8);
		} catch (IOException e) {
			Logger.error(e);
			return "";
		}
	}

	private void openSearch(Layer<?, ?> layer) {
		HomeTemplate template = new HomeTemplate(box());
		template.id(UUID.randomUUID().toString());
		layer.template(template);
		template.page(HomeTemplate.Page.Search);
		template.refresh();
	}

	private void openSearch() {
		openSearchLayerTrigger.address(path -> PathHelper.searchPath());
		openSearchLayerTrigger.launch();
	}
}