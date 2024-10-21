package io.intino.ime.box.scaffolds;

public interface Scaffold {
	void build();
	String srcPath();
	String outPath();
	String genPath();
	String resPath();
}
