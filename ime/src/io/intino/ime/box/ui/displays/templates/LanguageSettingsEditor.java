package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.Resource;
import io.intino.alexandria.exceptions.Conflict;
import io.intino.alexandria.exceptions.InternalServerError;
import io.intino.alexandria.ui.displays.UserMessage;
import io.intino.alexandria.ui.displays.events.actionable.ToggleEvent;
import io.intino.builderservice.schemas.BuilderInfo;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.DisplayHelper;
import io.intino.ime.box.util.LanguageHelper;
import io.intino.ime.model.Language;
import io.intino.ime.model.Operation;
import io.intino.ime.model.Release;

import java.util.*;

import static java.util.stream.Collectors.toMap;

public class LanguageSettingsEditor extends AbstractLanguageSettingsEditor<ImeBox> {
	private Language language;
	private Resource logo;
	private Map<String, Operation> operationMap;
	private Set<String> tagSet;
	private BuilderInfo info;
	private String error;

	public LanguageSettingsEditor(ImeBox box) {
		super(box);
	}

	public void language(Language language) {
		this.language = language;
		this.operationMap = language.operations().stream().collect(toMap(o -> UUID.randomUUID().toString(), o -> o, (a,b) -> b, LinkedHashMap::new));
		this.tagSet = new HashSet<>(language.tags());
	}

	public boolean check() {
		if (!DisplayHelper.check(descriptionField, this::translate)) return false;
		if (builderField == null) return true;
		String builder = builderField.value();
		if (info == null && builder != null && !builder.isEmpty()) {
			notifyUser(translate("Docker image name is not valid"), UserMessage.Type.Error);
			return false;
		}
		return true;
	}

	public Resource logo() {
		return logo;
	}

	public String description() {
		return descriptionField.value();
	}

	public boolean isPrivate() {
		return accessTypeField.state() == ToggleEvent.State.On;
	}

	public String builder() {
		return builderField != null ? builderField.value() : language.builder();
	}

	public List<Operation> operations() {
		return new ArrayList<>(operationMap.values());
	}

	public List<String> tags() {
		return new ArrayList<>(tagSet);
	}

	@Override
	public void init() {
		super.init();
		propertiesBlock.onInit(e -> initProperties());
		propertiesBlock.onShow(e -> refreshProperties());
		builderBlock.onInit(e -> initBuilder());
		builderBlock.onShow(e -> refreshBuilder());
		addTagDialog.onOpen(e -> refreshTagDialog());
		builderDetailsDialog.onOpen(e -> refreshBuilderDetailsDialog());
	}

	@Override
	public void refresh() {
		super.refresh();
		tabSelector.select("propertiesOption");
	}

	private void initProperties() {
		logoField.onChange(e -> logo = e.value());
		addTag.onExecute(e -> addTag());
		tagField.onEnterPress(e -> addTag());
	}

	private void refreshProperties() {
		logoField.value(LanguageHelper.logo(language, box()));
		nameField.value(language.name());
		descriptionField.value(language.description());
		accessTypeField.state(language.isPrivate() ? ToggleEvent.State.On : ToggleEvent.State.Off);
		refreshTags();
	}

	private void refreshTags() {
		tags.clear();
		tagSet.stream().sorted(Comparator.naturalOrder()).forEach(o -> fill(o, tags.add()));
	}

	private void initBuilder() {
		builderField.onChange(e -> updateInfo());
		checkBuilder.onExecute(e -> checkBuilder());
	}

	private void updateInfo() {
		error = builderField.value() == null || builderField.value().isEmpty() ? null : error;
		info = builderField.value() == null || builderField.value().isEmpty() ? null : info;
		refreshBuilderInfo(info);
		updateOperations(info);
	}

	private void updateOperations(BuilderInfo info) {
		if (info == null) return;
		List<String> operations = info.operations();
		operationMap = operationMap.entrySet().stream().filter(o -> !operations.contains(o.getKey())).collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, LinkedHashMap::new));
		operations.forEach(o -> operationMap.putIfAbsent(o, new Operation(o)));
		refreshOperations();
	}

	private void refreshBuilder() {
		builderField.value(language.builder());
		loadBuilderInfo(language.builder());
		refreshBuilderInfo(info);
		refreshOperations();
	}

	private Release lastRelease() {
		return box().languageManager().lastRelease(language);
	}

	private void fill(Map.Entry<String, Operation> operationEntry, OperationEditor display) {
		display.operation(operationEntry.getValue());
		display.onChange(o -> operationMap.put(operationEntry.getKey(), o));
		display.refresh();
	}

	private void fill(String tag, TagEditor display) {
		display.tag(tag);
		display.onRemove(o -> removeTag(tag));
		display.refresh();
	}

	private void removeTag(String tag) {
		tagSet.remove(tag);
		refreshTags();
	}

	private void refreshOperations() {
		operations.clear();
		operationMap.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(o -> fill(o, operations.add()));
	}

	private void refreshBuilderInfo(BuilderInfo info) {
		String name = builderField.value();
		validBuilderIcon.visible(isValidBuilder());
		noBuilderInfoIcon.visible(hasNoBuilderInformation());
		invalidBuilderIcon.visible(isInvalidBuilder());
		checkBuilder.readonly(name == null || name.isEmpty());
		showBuilderInfo.readonly(name == null || name.isEmpty() || info == null);
	}

	private void addTag() {
		if (!DisplayHelper.check(tagField, this::translate)) return;
		addTagDialog.close();
		tagSet.add(tagField.value());
		refreshTags();
	}

	private void refreshTagDialog() {
		tagField.value(null);
	}

	private void refreshBuilderDetailsDialog() {
		builderPropertiesBlock.visible(isValidBuilder());
		errorMessage.visible(isInvalidBuilder());
		errorMessage.value(error);
		if (!builderPropertiesBlock.isVisible()) return;
		builderProperties.clear();
		info.properties().forEach((key, value) -> fill(key, value, builderProperties.add()));
	}

	private void fill(String property, String value, BuilderPropertyView display) {
		display.property(property, value);
		display.refresh();
	}

	private void loadBuilderInfo(String builder) {
		try {
			this.error = null;
			this.info = box().builderService().getBuilderInfo(builder);
		} catch (InternalServerError | Conflict e) {
			this.error = builderField.value() != null && !builderField.value().isEmpty() ? e.getMessage() : null;
			this.info = null;
		}
	}

	private void checkBuilder() {
		refreshBuilderInfo(null);
		loadBuilderInfo(builderField.value());
		refreshBuilderInfo(info);
		refreshOperations();
		builderDetailsDialog.open();
	}

	private boolean isValidBuilder() {
		return info != null;
	}

	private boolean hasNoBuilderInformation() {
		return info == null && error == null;
	}

	private boolean isInvalidBuilder() {
		return info == null && error != null;
	}

}