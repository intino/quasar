package io.quassar.editor;

import io.quassar.editor.box.util.ArtifactoryHelper;
import io.quassar.editor.model.GavCoordinates;
import org.junit.Assert;
import org.junit.Test;

public class ArtifactoryHelper_ {

	@Test
	public void test() {
		String path = "io/quassar/visora/reader-java/1.0.3/reader-java-1.0.3.pom";
		GavCoordinates coordinates = ArtifactoryHelper.parse(path);
		Assert.assertNotNull(coordinates);
		Assert.assertEquals("io.quassar.visora", coordinates.groupId());
		Assert.assertEquals("reader-java", coordinates.artifactId());
		Assert.assertEquals("1.0.3", coordinates.version());
	}


}
