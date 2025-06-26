package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.models.File;
import io.quassar.editor.box.models.ModelContainer;
import io.quassar.editor.box.models.Workspace;
import io.quassar.editor.box.ui.types.LanguageTab;
import io.quassar.editor.box.ui.types.ModelView;
import io.quassar.editor.box.util.PermissionsHelper;
import io.quassar.editor.model.FilePosition;
import io.quassar.editor.model.GavCoordinates;
import io.quassar.editor.model.Model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

import static io.quassar.editor.box.models.File.ResourcesDirectory;

public class ModelTemplate extends AbstractModelTemplate<EditorBox> {
	private Model model;
	private String release;
	private ModelView selectedView;
	private LanguageTab selectedTab;
	private ModelContainer modelContainer;
	private io.quassar.editor.box.models.File selectedFile;
	private FilePosition selectedPosition;
	private boolean showHelp = false;
	private boolean isLanguageChangeBlockVisible = false;

	public ModelTemplate(EditorBox box) {
		super(box);
	}

	public void languageChanged(GavCoordinates language) {
		if (model == null) return;
		if (!model.language().equals(language)) return;
		if (isLanguageChangeBlockVisible) return;
		languageChangedBlock.show();
		isLanguageChangeBlockVisible = true;
	}

	public void openStarting(String model) {
		open(model, null, null, null, null, null, true);
	}

	public void openTemplate(String model) {
		open(model, Model.DraftRelease, null, null, null, null, false);
	}

	public void open(String model, String release) {
		open(model, release, null, null, null, null, false);
	}

	public void open(String model, String release, String tab, String view, String file, String position) {
		open(model, release, tab, view, file, position, false);
	}

	public void open(String model, String release, String tab, String view, String file, String position, boolean showHelp) {
		if (samePath(model, release, tab, view, file, position, showHelp)) return;
		this.model = box().modelManager().get(model);
		this.release = release != null ? release : Model.DraftRelease;
		this.selectedTab = tab != null ? LanguageTab.from(tab) : LanguageTab.Help;
		this.selectedView = view != null ? ModelView.from(view) : ModelView.Model;
		this.modelContainer = this.model != null ? box().modelManager().modelContainer(this.model, this.release) : null;
		this.selectedFile = file != null && modelContainer != null ? modelContainer.file(file) : null;
		this.selectedPosition = position != null ? FilePosition.from(position) : null;
		this.showHelp = showHelp;
		refresh();
	}

	private boolean samePath(String model, String release, String tab, String view, String file, String position, boolean showHelp) {
		if (this.model == null || !this.model.id().equals(model)) return false;
		if ((this.release == null && release != null) || (release != null && !this.release.equals(release))) return false;
		if ((this.selectedTab == null && tab != null) || (tab != null && !this.selectedTab.name().equalsIgnoreCase(tab))) return false;
		if ((this.selectedView == null && view != null) || (view != null && !this.selectedView.name().equalsIgnoreCase(view))) return false;
		if ((this.selectedFile == null && file != null) || (file != null && !this.selectedFile.name().equals(file))) return false;
		if ((this.selectedPosition == null && position != null) || (position != null && !this.selectedPosition.toString().equals(position))) return false;
		if (this.showHelp != showHelp) return false;
		return true;
	}

	@Override
	public void init() {
		super.init();
		reload.onExecute(e -> notifier.redirect());
	}

	@Override
	public void refresh() {
		super.refresh();
		notFoundBlock.visible(!PermissionsHelper.hasPermissions(model, session(), box()));
		refreshContent();
	}

	private void refreshContent() {
		contentBlock.visible(PermissionsHelper.hasPermissions(model, session(), box()));
		if (!contentBlock.isVisible()) return;
		licenseExpiredBanner.language(model.language());
		licenseExpiredBanner.hint(translate("Please request a license to enable modeling"));
		licenseExpiredBanner.refresh();
		modelEditor.model(model, release);
		modelEditor.view(view());
		modelEditor.tab(selectedTab);
		modelEditor.file(file(), selectedPosition);
		modelEditor.showHelp(showHelp);
		modelEditor.refresh();
	}

	private ModelView view() {
		return selectedView;
	}

	private File file() {
		if (selectedFile != null && modelContainer.exists(selectedFile)) return selectedFile;
		if (modelContainer == null) return null;
		Set<File> files = selectedView == null || selectedView == ModelView.Model ?
				modelContainer.modelFiles().stream().sorted(Comparator.comparing(File::name)).collect(Collectors.toSet()) :
				modelContainer.resourceFiles().stream().filter(f -> !f.uri().equals(ResourcesDirectory)).collect(Collectors.toSet());
		if (files.isEmpty()) return null;
		File result = files.stream().filter(f -> f.name().equals(Workspace.MainFile)).findFirst().orElse(null);
		return result != null ? result : new ArrayList<>(files).stream().min(Comparator.comparing(File::name)).orElse(null);
	}

}