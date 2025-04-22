package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.ui.types.ForgeView;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.box.util.SubjectHelper;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;

import java.util.List;

public class LanguageForgeTemplate extends AbstractLanguageForgeTemplate<EditorBox> {
	private Language language;
	private String release;
	private ForgeView view;

	public LanguageForgeTemplate(EditorBox box) {
		super(box);
	}

	public void language(Language language) {
		this.language = language;
	}

	public void release(String release) {
		this.release = release;
	}

	public void view(ForgeView view) {
		this.view = view;
	}

	@Override
	public void init() {
		super.init();
		viewSelector.select(0);
		propertiesBlock.onShow(e -> refreshPropertiesBlock());
		helpBlock.onInit(e -> initHelpBlock());
		helpBlock.onShow(e -> refreshHelpBlock());
		kitBlock.onInit(e -> initKitBlock());
		kitBlock.onShow(e -> refreshKitBlock());
		toolsBlock.onInit(e -> initToolsBlock());
		toolsBlock.onShow(e -> refreshToolsBlock());
		versionSelector.onSelect(e -> updateVersion());
	}

	@Override
	public void refresh() {
		super.refresh();
		infoStamp.language(language);
		infoStamp.release(release);
		infoStamp.refresh();
		refreshView();
		refreshVersions();
	}

	private void refreshView() {
		Model model = box().modelManager().get(language.metamodel());
		versionSelectorBlock.visible(view != null && view != ForgeView.Properties);
		viewSelector.address(path -> PathHelper.forgePath(path, model.id(), release));
		if (view != null) viewSelector.selection(view.name());
		hideViews();
		if (view == ForgeView.Help) helpBlock.show();
		else if (view == ForgeView.Kit) kitBlock.show();
		else if (view == ForgeView.Tools) toolsBlock.show();
		else propertiesBlock.show();
	}

	private void hideViews() {
		if (propertiesBlock.isVisible()) propertiesBlock.hide();
		else if (helpBlock.isVisible()) helpBlock.hide();
		else if (kitBlock.isVisible()) kitBlock.hide();
		else if (toolsBlock.isVisible()) toolsBlock.hide();
	}

	private void refreshPropertiesBlock() {
		propertiesStamp.language(language);
		propertiesStamp.release(release);
		propertiesStamp.refresh();
	}

	private void initHelpBlock() {
		helpStamp.onCreateVersion(e -> refresh());
	}

	private void refreshHelpBlock() {
		helpStamp.language(language);
		helpStamp.release(release);
		helpStamp.refresh();
	}

	private void initKitBlock() {
		kitStamp.onCreateVersion(e -> refresh());
	}

	private void refreshKitBlock() {
		kitStamp.language(language);
		kitStamp.release(release);
		kitStamp.refresh();
	}

	private void initToolsBlock() {
		toolsStamp.onCreateVersion(e -> refresh());
	}

	private void refreshToolsBlock() {
		toolsStamp.language(language);
		toolsStamp.release(release);
		toolsStamp.refresh();
	}

	private void refreshVersions() {
		Model metamodel = box().modelManager().get(language.metamodel());
		List<String> releases = box().modelManager().releases(metamodel);
		versionSelector.address(path -> PathHelper.forgeReleasePath(path, metamodel.id(), viewSelector.selection().getFirst()));
		versionSelector.clear();
		versionSelector.addAll(releases);
		if (release != null) versionSelector.selection(release);
	}

	private void updateVersion() {
		List<String> selection = versionSelector.selection();
		release = !selection.isEmpty() ? selection.getFirst() : null;
		refreshView();
	}

}