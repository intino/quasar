package io.intino.ime.box.ui.model;

import io.intino.ime.model.Language;

import java.util.ArrayList;
import java.util.List;

public class SearchItem {
	private final String name;
	private List<Language> languageList;

	public SearchItem(String name) {
		this.name = name;
		this.languageList = new ArrayList<>();
	}

	public String name() {
		return name;
	}

	public List<Language> languageList() {
		return languageList;
	}

	public SearchItem languageList(List<Language> languageList) {
		this.languageList = languageList;
		return this;
	}

	public void addAll(List<Language> languages) {
		languageList.addAll(languages);
	}
}
