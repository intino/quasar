package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.util.DisplayHelper;
import io.quassar.editor.model.LanguageTool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

public class LanguageToolEditor extends AbstractLanguageToolEditor<EditorBox> {
	private LanguageTool.Type type;
	private Map<String, String> parameters = new HashMap<>();

	public LanguageToolEditor(EditorBox box) {
		super(box);
	}

	public void type(LanguageTool.Type type) {
		this.type = type;
	}

	public void reset() {
		parameters.clear();
	}

	public boolean check() {
		return switch (type) {
			case Docker -> DisplayHelper.check(dockerImageField, this::translate);
			case Site -> DisplayHelper.check(siteUrlField, this::translate);
			case Manual -> DisplayHelper.check(installInstructionsField, this::translate) && DisplayHelper.check(executionInstructionsField, this::translate);
		};
	}

	public void parameters(List<LanguageTool.Parameter> parameters) {
		this.parameters = parameters.stream().collect(toMap(LanguageTool.Parameter::name, LanguageTool.Parameter::value));
	}

	public Map<String, String> parameters() {
		return switch (type) {
			case Docker -> Map.of("url", dockerImageField.value(), "params", dockerImageRunField.value());
			case Site -> Map.of("url", siteUrlField.value());
			case Manual -> Map.of("install", installInstructionsField.value(), "execution", executionInstructionsField.value());
		};
	}

	@Override
	public void init() {
		super.init();
		dockerImageFactoryBlock.onInit(e -> initDockerImageFactory());
		dockerImageFactoryBlock.onShow(e -> refreshDockerImageFactory());
		deploySiteFactoryBlock.onInit(e -> initDeploySiteFactory());
		deploySiteFactoryBlock.onShow(e -> refreshDeploySiteFactory());
		buildInstructionsBlock.onInit(e -> initBuildInstructions());
		buildInstructionsBlock.onShow(e -> refreshBuildInstructions());
	}

	@Override
	public void refresh() {
		super.refresh();
		if (type == LanguageTool.Type.Docker) dockerImageFactoryBlock.show();
		else if (type == LanguageTool.Type.Site) deploySiteFactoryBlock.show();
		else buildInstructionsBlock.show();
	}

	private void initDockerImageFactory() {
		dockerImageField.onChange(e -> parameters.put("url", e.value()));
		dockerImageRunField.onChange(e -> parameters.put("params", e.value()));
	}

	private void refreshDockerImageFactory() {
		dockerImageField.value(parameters.getOrDefault("url", null));
		dockerImageRunField.value(parameters.getOrDefault("params", null));
	}

	private void initDeploySiteFactory() {
		siteUrlField.onChange(e -> parameters.put("url", e.value()));
	}

	private void refreshDeploySiteFactory() {
		siteUrlField.value(parameters.getOrDefault("url", null));
	}

	private void initBuildInstructions() {
		installInstructionsField.onChange(e -> parameters.put("install", e.value()));
		executionInstructionsField.onChange(e -> parameters.put("execution", e.value()));
	}

	private void refreshBuildInstructions() {
		installInstructionsField.value(parameters.getOrDefault("install", null));
		executionInstructionsField.value(parameters.getOrDefault("execution", null));
	}
}