package io.quassar.editor;

import io.quassar.editor.box.util.ArtifactoryHelper;
import org.junit.Assert;
import org.junit.Test;

public class ArtifactoryHelper_ {

	@Test
	public void test() {
		String path = "quassar/visora/java-reader/1.0.20/java-reader-1.0.20.pom";
		Assert.assertEquals("quassar", ArtifactoryHelper.groupId(path));
		Assert.assertEquals("visora", ArtifactoryHelper.artifactId(path));
		Assert.assertEquals("1.0.20", ArtifactoryHelper.version(path));
		Assert.assertEquals("java-reader", ArtifactoryHelper.file(path));
	}


}
