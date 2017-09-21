package viterbi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import ngrams.Node;

public class Viterbi {

	public static void main(String[] args) throws FileNotFoundException {
		InputStream sents = new FileInputStream(new File(args[1]));
		InputStream probs = new FileInputStream(new File(args[0]));
		// parse prob from probs text file
		HashMap<String, Double> pr = parseProb(probs);
		// get sentences
		ArrayList<String> sentences = parseSent(sents);
		// get Data with viberti
		String[] tags = new String[] {"phi","noun","verb", "inf", "prep"};
		ArrayList<Data> dataset = getTagSeq(tags, sentences,pr);
		
		// print results
		printViterbi(dataset,tags);
		
	}

	

	private static void printViterbi(ArrayList<Data> dataset, String[] tags) {
		for (int i = 0; i < dataset.size(); i++) {
			Data d = dataset.get(i);
			String sentence = d.getSentence();
			String[] words = sentence.toLowerCase().split("\\s+");
			System.out.println(String.format("PROCESSING SENTENCE: %s\n", sentence));
			System.out.println("FINAL VITERBI NETWORK");
			for (int w = 0; w < words.length; w++ ) {
				for (int t = 1; t < tags.length; t++) {
					double log = Math.log(d.getScore(t, w))/Math.log(2);
					System.out.println(String.format("P(%s=%s) = %.4f", words[w],tags[t],log));
				}
			}
			System.out.println("\nFINAL BACKPTR NETWORK");
			for (int w = 1; w < words.length; w++ ) {
				for (int t = 1; t < tags.length; t++) {
					int index = d.getBackPtr(t, w);
					System.out.println(String.format("Backptr(%s=%s) = %s", words[w],tags[t],tags[index]));
				}
			}
			
			double bestTag = 1;
			int tag ;
			int[] seq = d.getSeq();
//			for (int w = 0; w < seq.length; w++) {
//				tag = seq[w];
//				bestTag *= d.getScore(tag, w);
//			}
			tag = seq[words.length-1];
			
			System.out.println(String.format("\nBEST TAG SEQUENCE HAS LOG PROBABILITY = %.4f", (Math.log(d.getScore(tag, words.length-1))/Math.log(2))));
			for (int w = seq.length-1; w >= 0; w--) {
				tag = seq[w];
				System.out.println(String.format("%s -> %s", words[w],tags[tag]));
			}
			System.out.println("\n");
			
		}
	}



	private static ArrayList<Data> getTagSeq(String[] tags, ArrayList<String> sentences, HashMap<String,Double> pr) {
		ArrayList<Data> dataset = new ArrayList<Data>();
		for (int i = 0 ; i < sentences.size(); i++ ) {
			Data data = viberti(tags,sentences.get(i), pr);
			dataset.add(data);
		}
		return dataset;
	}



	private static Data viberti(String[] tags, String sentence, HashMap<String, Double> pr) {
		String[] words = sentence.toLowerCase().split("\\s+");
		Data data = new Data(sentence,tags.length,words.length);
		// initialization step
		double pr_wordtag=0,pr_tagphi = 0;
		for (int t = 1; t < tags.length; t++) {
			String wordtag = words[0] + " " +tags[t]; 
			String tagphi = tags[t] + " " +tags[0]; 
			if (pr.containsKey(wordtag)) {
				pr_wordtag = pr.get(wordtag);
			} else {
				pr_wordtag = .0001;
			}
			
			if (pr.containsKey(tagphi)) {
				pr_tagphi = pr.get(tagphi);
			} else {
				pr_tagphi = .0001;
			}
			data.addScore(t, 0,pr_wordtag * pr_tagphi );
			data.addBackPtr(t, 0, 0);
		}
		// iteration step
		for (int w = 1; w < words.length; w++) {
			for (int t = 1; t < tags.length; t++) {
				double  max = 0,cal = 0,pr_tagtk = 0;
				int maxK = -1;
				for (int k = 1; k < tags.length; k++) {
					String tagtk = tags[t]+ " "+ tags[k];
					if (pr.containsKey(tagtk)) {
						pr_tagtk = pr.get(tagtk);
					} else {
						pr_tagtk = .0001;
					}
					cal = data.getScore(k, w-1)* pr_tagtk;
					if (cal > max) {
						max = cal;
						maxK = k;
					}
				}
				String wordtag = words[w] + " " +tags[t];
				if (pr.containsKey(wordtag)) {
					pr_wordtag = pr.get(wordtag);
				} else {
					pr_wordtag = .0001;
				}
				
				data.addScore(t, w, pr_wordtag*max);
				data.addBackPtr(t, w, maxK);
				
			}
		}
		// Sequence Identification
		int maxT = 1;
		double lastWordMaxScore = data.getScore(1, words.length-1);
		double cmp  = 0;
		for (int t = 2 ; t < tags.length; t ++) {
			cmp = data.getScore(t, words.length-1);
			if (cmp > lastWordMaxScore) {
				lastWordMaxScore = cmp;
				maxT = t;
			}
		}
		data.addSeq(words.length-1, maxT);
		
		for (int w = words.length-2 ; w >= 0; w--) {
			data.addSeq(w, data.getBackPtr(data.getSeq(w+1), w+1));
		}
		return data;
	}



	private static ArrayList<String> parseSent(InputStream sents) {
		ArrayList<String> sentences = new ArrayList<>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(sents));
			String line;
			while ((line = reader.readLine()) != null) {
				sentences.add(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sentences;
	}

	private static HashMap<String, Double> parseProb(InputStream probs) {
		HashMap<String,Double> pr = new HashMap<>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(probs));
			String line;
			while ((line = reader.readLine()) != null) {
				int lastSpace = line.lastIndexOf(" ");
				pr.put(line.substring(0, lastSpace), Double.parseDouble(line.substring(lastSpace+1)));
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return pr;
	}

}
