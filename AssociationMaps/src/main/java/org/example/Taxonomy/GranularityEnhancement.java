package org.example.Taxonomy;

import java.util.*;

public class GranularityEnhancement {

	public enum Granularity {
		DOCUMENT,
		PARAGRAPH,
		SENTENCE,
		CHUNK
	}

	private Granularity granularity = Granularity.DOCUMENT;
	private double chunkThr = 0.75; //Default value

	public  void setGranularity(Granularity g) {
		this.granularity = g;
	}

	public  Granularity getGranularity(){
		return this.granularity;
	}

	public   void setChunkThr(double thr){
		this.chunkThr=thr;
	}

	public  double getChunkThr(){
		return this.chunkThr;
	}

	public List<String[]> segmentText(String content) {
		List<String[]> segments = new ArrayList<>();

		switch (granularity) {
			case DOCUMENT:
				segments.add(content.split("\\s+"));
				break;

			case PARAGRAPH:
				for (String paragraph : content.split("\\n\\n+")) {
					if (!paragraph.trim().isEmpty())
						segments.add(paragraph.split("\\s+"));
				}
				break;

			case SENTENCE:
				for (String sentence : content.split("(?<=[.!?])\\s+")) {
					if (!sentence.trim().isEmpty())
						segments.add(sentence.split("\\s+"));
				}
				break;

			case CHUNK:
				segments.addAll(semanticChunking(content, chunkThr));
				break;
		}

		return segments;
	}

	private List<String[]> semanticChunking(String text, double simThreshold) {
		List<String> sentences = Arrays.asList(text.split("(?<=[.!?])\\s+"));
		List<String[]> chunks = new ArrayList<>();
		List<String> currentChunk = new ArrayList<>();

		Map<String, Set<String>> sentWords = new HashMap<>();
		for (String s : sentences) {
			Set<String> words = new HashSet<>(Arrays.asList(s.toLowerCase().split("\\W+")));
			sentWords.put(s, words);
		}

		String prev = null;
		for (String s : sentences) {
			if (prev == null) {
				currentChunk.add(s);
				prev = s;
				continue;
			}
			double sim = jaccard(sentWords.get(prev), sentWords.get(s));
			if (sim < simThreshold) {
				chunks.add(String.join(" ", currentChunk).split("\\s+"));
				currentChunk.clear();
			}
			currentChunk.add(s);
			prev = s;
		}

		if (!currentChunk.isEmpty())
			chunks.add(String.join(" ", currentChunk).split("\\s+"));

		return chunks;
	}

	private double jaccard(Set<String> a, Set<String> b) {
		if (a.isEmpty() && b.isEmpty()) return 1.0;
		Set<String> intersection = new HashSet<>(a);
		intersection.retainAll(b);
		Set<String> union = new HashSet<>(a);
		union.addAll(b);
		return (double) intersection.size() / union.size();
	}


}
