package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.UserMessage;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command.CommandResult;
import io.quassar.editor.box.ui.types.ForgeView;
import io.quassar.editor.box.util.LanguageHelper;
import io.quassar.editor.box.util.PathHelper;
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
		infoLink.onExecute(e -> showInfoBlock());
		infoDialog.onOpen(e -> refreshInfoDialog());
		infoDialog.onClose(e -> refreshProperties());
		helpBlock.onInit(e -> initHelpBlock());
		helpBlock.onShow(e -> refreshHelpBlock());
		kitBlock.onInit(e -> initKitBlock());
		kitBlock.onShow(e -> refreshKitBlock());
		executionBlock.onInit(e -> initExecutionBlock());
		executionBlock.onShow(e -> refreshExecutionBlock());
		viewSelector.select(0);
		versionSelector.onSelect(e -> updateVersion());
		refreshVersions.onExecute(e -> refreshVersions());
	}

	@Override
	public void refresh() {
		super.refresh();
		refreshProperties();
		refreshVersions();
		refreshFooter();
		refreshView();
	}

	private void showInfoBlock() {
		infoDialog.open();
	}

	private void refreshView() {
		Model model = box().modelManager().get(language.metamodel());
		viewSelector.address(path -> PathHelper.forgePath(path, model.id(), release));
		if (view != null) viewSelector.selection(view.name());
		hideViews();
		if (view == ForgeView.Help) helpBlock.show();
		else if (view == ForgeView.Kit) kitBlock.show();
		else if (view == ForgeView.Execution) executionBlock.show();
		else kitBlock.show();
	}

	private void hideViews() {
		if (helpBlock.isVisible()) helpBlock.hide();
		else if (kitBlock.isVisible()) kitBlock.hide();
		else if (executionBlock.isVisible()) executionBlock.hide();
	}

	private void refreshInfoDialog() {
		infoStamp.language(language);
		infoStamp.release(release);
		infoStamp.refresh();
	}

	private void initHelpBlock() {
		helpStamp.onCreateVersion(this::versionCreated);
	}

	private void refreshHelpBlock() {
		helpStamp.language(language);
		helpStamp.release(release);
		helpStamp.refresh();
	}

	private void initKitBlock() {
		kitStamp.onCreateVersion(this::versionCreated);
	}

	private void refreshKitBlock() {
		kitStamp.language(language);
		kitStamp.release(release);
		kitStamp.refresh();
	}

	private void initExecutionBlock() {
		executionStamp.onCreateVersion(this::versionCreated);
	}

	private void refreshExecutionBlock() {
		executionStamp.language(language);
		executionStamp.release(release);
		executionStamp.refresh();
	}

	private void refreshProperties() {
		logo.value(LanguageHelper.logo(language, box()));
		languageName.value(language.name().toLowerCase());
	}

	private void refreshVersions() {
		Model metamodel = box().modelManager().get(language.metamodel());
		List<String> releases = box().modelManager().releases(metamodel).reversed();
		versionSelector.address(path -> PathHelper.forgeReleasePath(path, metamodel.id(), viewSelector.selection().getFirst()));
		versionSelector.clear();
		versionSelector.addAll(releases);
		if (release != null) versionSelector.selection(release);
	}

	private void refreshFooter() {
		Language parentLanguage = box().languageManager().get(language.parent());
		parentLanguageBlock.visible(parentLanguage != null);
		if (parentLanguage == null) return;
		parentLanguageImage.value(LanguageHelper.logo(parentLanguage, box()));
		parentLanguageTitle.value("%s forge".formatted(parentLanguage.name()));
		parentLanguageLink.address(path -> PathHelper.languagePath(language));
	}

	private void updateVersion() {
		List<String> selection = versionSelector.selection();
		release = !selection.isEmpty() ? selection.getFirst() : null;
		refreshView();
	}

	private void versionCreated(CommandResult result) {
		if (!result.success()) notifyUser(translate("Could not create version"), UserMessage.Type.Error);
		refresh();
	}

}