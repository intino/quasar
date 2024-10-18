package io.intino.ime.box.orchestator;

import java.util.List;

public class ProjectCreator {

	public ProjectCreator(String name, String lang, List<CodeBucket> buckets){
	}

	public void create() {
	}

	public interface CodeBucket {
		String folder();
		String codeLanguage();
		String scaffold(); // "Intellij"
		String builder(); // "Intellij"
	}
}
