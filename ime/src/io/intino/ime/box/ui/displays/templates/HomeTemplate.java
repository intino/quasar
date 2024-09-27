package io.intino.ime.box.ui.displays.templates;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.DisplayHelper;
import io.intino.ime.box.ui.ViewMode;

public class HomeTemplate extends AbstractHomeTemplate<ImeBox> {

	public HomeTemplate(ImeBox box) {
		super(box);
	}

	@Override
	public void init() {
		super.init();
		DisplayHelper.initViewMode(session());
		languagesBlock.onShow(e -> languagesBlock.refresh());
		modelsBlock.onShow(e -> modelsTemplate.refresh());
	}

	@Override
	public void refresh() {
		super.refresh();
		ViewMode viewMode = DisplayHelper.viewMode(session());
		languagesBlock.hide();
		modelsBlock.hide();
		if (viewMode == ViewMode.Languages) languagesBlock.show();
		if (viewMode == ViewMode.Models) modelsBlock.show();
	}

}