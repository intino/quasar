package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.components.BlockConditional;
import io.intino.alexandria.ui.displays.components.Layer;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.DisplayHelper;
import io.intino.ime.box.ui.PathHelper;

import java.util.Arrays;
import java.util.UUID;

public class HomeTemplate extends AbstractHomeTemplate<ImeBox> {
	private Page current = null;
	private String searchCondition;

	public enum Page {
		Search, Main, Language, Model, Documentation, Dashboard;

		public static Page from(String key) {
			return Arrays.stream(values()).filter(v -> v.name().equalsIgnoreCase(key)).findFirst().orElse(null);
		}
	}

	public void openSearch() {
		openPage(Page.Search);
	}

	public void openMain() {
		openPage(Page.Main);
	}

	public void openLanguage(String language) {
		openPage(Page.Language);
		languageBlock.languageTemplate.language(language);
		languageBlock.languageTemplate.refresh();
	}

	public void openModel(String model, String file) {
		openPage(Page.Model);
		modelBlock.modelTemplate.model(model);
		modelBlock.modelTemplate.file(file);
		modelBlock.modelTemplate.refresh();
	}

	public void openDashboard() {
		openPage(Page.Dashboard);
		dashboardBlock.dashboardTemplate.refresh();
	}

	public void openDocumentation() {
		openPage(Page.Documentation);
		documentationBlock.documentationTemplate.refresh();
	}

	public HomeTemplate(ImeBox box) {
		super(box);
	}

	public void page(Page page) {
		openPage(page);
	}

	@Override
	public void init() {
		super.init();
		DisplayHelper.initViewMode(session());
		homeBlock.onInit(e -> initHome());
		homeBlock.onShow(e -> refreshHome());
		openSearchLayerTrigger.onOpen(e -> refreshLayer(e.layer()));
	}

	@Override
	public void refresh() {
		super.refresh();
		refreshCurrentPage();
	}

	private boolean openPage(Page page) {
		if (page == current) return false;
		hideBlocks();
		blockOf(page).show();
		current = page;
		return true;
	}

	private void initHome() {
		header.onOpenSearch(e -> openSearch(searchCondition));
		openSearchDialog.onExecute(e -> openSearchLayerTrigger.launch());
	}

	private void refreshHome() {
		header.view(HeaderTemplate.View.Home);
		header.refresh();
	}

	private void refreshLanguage() {
		languageTemplate.refresh();
	}

	private void refreshModel() {
		modelTemplate.refresh();
	}

	private void refreeshDocumentation() {
		documentationBlock.refresh();
	}

	private void refreshDashboard() {
		dashboardTemplate.refresh();
	}

	public void openSearch(String condition) {
		condition(condition);
		openSearchLayerTrigger.address(path -> PathHelper.searchPath(searchCondition));
		openSearchLayerTrigger.launch();
	}

	public void search(String condition) {
		if (!searchBlock.isVisible()) return;
		if (sameCondition(condition)) return;
		condition(condition);
		searchBlock.searchTemplate.condition(searchCondition);
		searchBlock.searchTemplate.refresh();
	}

	private boolean sameCondition(String condition) {
		return (searchCondition == null && condition == null) || (searchCondition != null && searchCondition.equals(condition));
	}

	public void condition(String searchCondition) {
		this.searchCondition = searchCondition;
	}

	private void refreshSearch() {
		searchBlock.searchTemplate.condition(searchCondition);
		searchBlock.searchTemplate.refresh();
	}

	private void refreshLayer(Layer<?, ?> layer) {
		HomeTemplate template = new HomeTemplate(box());
		template.id(UUID.randomUUID().toString());
		layer.template(template);
		layer.onClose(e -> homeBlock.show());
		template.page(Page.Search);
		template.condition(searchCondition);
		template.refresh();
	}

	private String optionOf(Page page) {
		if (page == Page.Search) return Page.Search.name();
		if (page == Page.Main) return Page.Main.name();
		if (page == Page.Language) return Page.Language.name();
		if (page == Page.Model) return Page.Model.name();
		if (page == Page.Documentation) return Page.Documentation.name();
		if (page == Page.Dashboard) return Page.Dashboard.name();
		return page.name().toLowerCase();
	}

	private void hideBlocks() {
		if (searchBlock.isVisible()) searchBlock.hide();
		if (homeBlock.isVisible()) homeBlock.hide();
		if (languageBlock.isVisible()) languageBlock.hide();
		if (modelBlock.isVisible()) modelBlock.hide();
		if (documentationBlock.isVisible()) documentationBlock.hide();
		if (dashboardBlock.isVisible()) dashboardBlock.hide();
	}

	private BlockConditional<?, ?> blockOf(Page page) {
		if (page == Page.Search) return searchBlock;
		if (page == Page.Main) return homeBlock;
		if (page == Page.Language) return languageBlock;
		if (page == Page.Model) return modelBlock;
		if (page == Page.Documentation) return documentationBlock;
		if (page == Page.Dashboard) return dashboardBlock;
		return null;
	}

	private void refreshCurrentPage() {
		if (current == Page.Search) refreshSearch();
		if (current == Page.Main) refreshHome();
		if (current == Page.Language) refreshLanguage();
		if (current == Page.Model) refreshModel();
		if (current == Page.Documentation) refreeshDocumentation();
		if (current == Page.Dashboard) refreshDashboard();
	}

}