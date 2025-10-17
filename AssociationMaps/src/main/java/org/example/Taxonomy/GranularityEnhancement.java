package org.example.Taxonomy;

public class GranularityEnhancement {

	public enum Granularity {
		DOCUMENT,
		PARAGRAPH,
		SENTENCE,
		CHUNK
	}

	private Granularity granularity;

	public void setGranularity(Granularity g) {
		this.granularity = g;
	}

}
