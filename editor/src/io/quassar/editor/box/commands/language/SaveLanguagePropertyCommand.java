package io.quassar.editor.box.commands.language;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageProperty;
import io.quassar.editor.model.Visibility;

import java.io.File;
import java.util.List;

public class SaveLanguagePropertyCommand extends Command<Boolean> {
	public Language language;
	public LanguageProperty property;
	public Object value;

	public SaveLanguagePropertyCommand(EditorBox box) {
		super(box);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Boolean execute() {
		if (property == LanguageProperty.Title) language.title((String) value);
		if (property == LanguageProperty.Description) language.description((String) value);
		if (property == LanguageProperty.License) language.license((String) value);
		if (property == LanguageProperty.Citation) language.citation((String) value);
		if (property == LanguageProperty.CitationLink) language.citationLink((String) value);
		if (property == LanguageProperty.Tags) language.tags((List<String>) value);
		if (property == LanguageProperty.License) language.license((String) value);
		if (property == LanguageProperty.Logo) box.languageManager().saveLogo(language, (File) value);
		if (property == LanguageProperty.Visibility) language.visibility((Visibility) value);
		return true;
	}

}
