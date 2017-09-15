package ngrams;

import java.util.HashMap;

public class Node {
	private String word;
	private int size;
	private HashMap<String, Integer> connectWith;
	
	public Node (String word) {
		this.word = word;
		this.connectWith = new HashMap<>();
		this.size = 0;
	}
	
	public int getNumberOfBigram() {
		return this.connectWith.size();
	}
	public void addBigram (String word) {
		size += 1;
		if (connectWith.containsKey(word)) {
			int freq = connectWith.get(word)+ 1;
			connectWith.replace(word, freq);
		} else {
			connectWith.put(word, 1);
		}
	}
	public int getBigramFrequency(String word) {
		if (connectWith.containsKey(word)) {
			return connectWith.get(word);
		}
		return 0;
	}
	public int size() {
		return this.size;
	}
}