package viterbi;

import java.util.HashMap;
import java.util.Map;

public class Data {
	private String sentence;
	private double[][] Score;
	private int[][] BackPtr;
	private int[] Seq;
	
	public Data(String s, int tag_size, int word_size) {
		this.sentence = s;
		Score = new double[tag_size][word_size];
		BackPtr = new int[tag_size][word_size];
		Seq = new int[word_size];
	}
	public void addScore(int t, int w, Double value) {
		Score[t][w] = value;
	}
	public void addBackPtr(int t, int w, int k) {
		BackPtr[t][w] = k;
	}
	public String getSentence() {
		return sentence;
	}
	public double getScore(int t, int w) {
		return Score[t][w];
	}
	public int getBackPtr(int t, int w) {
		return BackPtr[t][w];
	}
	public void addSeq (int w, int b) {
		Seq[w] = b;
	}
	public int getSeq (int w) {
		return Seq[w];
	}
	public int[] getSeq() {
		return Seq;
	}
	
}
