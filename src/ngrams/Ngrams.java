package ngrams;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class Ngrams {

	public static void main(String[] args) throws FileNotFoundException {
		// InputStream train = ;
		InputStream test = new FileInputStream(new File(args[2]));
		InputStream in = new FileInputStream(new File(args[0]));
		HashMap<String, Integer> unigrams = null;
		HashMap<String, Node> bigrams = null;
		BufferedReader reader = null;
		// build unigrams and bigrams hashmap	
		try {
			unigrams = new HashMap<String, Integer>();
			bigrams = new HashMap<String, Node>();
			reader = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] st = line.toLowerCase().split("\\s+");
				Node node = null;
				String pre = "PHI";
				for (int i = 0; i < st.length; i++) {
					// add to unigrams
					if (unigrams.containsKey(st[i])) {
						int freq = unigrams.get(st[i]) + 1;
						unigrams.replace(st[i], freq);
					} else {
						unigrams.put(st[i], 1);
					}
					// add to bigrams
					if (bigrams.containsKey(pre)) {
						node = bigrams.get(pre);
					} else {
						node = new Node(pre);
					}
					node.addBigram(st[i]);
					bigrams.put(pre,node);
					pre = st[i];
				}
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
		ArrayList<Data> prob = sentenceProbability(test, unigrams, bigrams);

		printSentenceProb(prob);

	}

	private static void printSentenceProb(ArrayList<Data> prob) {
		double uni_prob;
		double unsmooth_bi_prob;
		double smooth_bi_prob;
		for (int i = 0; i < prob.size(); i++) {
			Data d = prob.get(i);
			uni_prob = d.getUnSmoothUnigrams();
			unsmooth_bi_prob = d.getUnSmoothBigrams();
			smooth_bi_prob = d.getSmoothBigrams();

			System.out.println(String.format("S = %s\n", d.getSentence()));

			if (uni_prob > 0) {
				System.out.println(
						String.format("Unsmoothed Unigrams, logprob(S) = %.4f", (Math.log(uni_prob) / Math.log(2))));
			} else {
				System.out.println("Unsmoothed Unigrams, logprob(S) = undefined");
			}

			if (unsmooth_bi_prob > 0) {
				System.out.println(String.format("Unsmoothed Bigrams, logprob(S) = %.4f",
						(Math.log(unsmooth_bi_prob) / Math.log(2))));
			} else {
				System.out.println("Unsmoothed Bigrams, logprob(S) = undefined");
			}

			if (smooth_bi_prob > 0) {
				System.out.println(
						String.format("Smoothed Bigrams, logprob(S) = %.4f", (Math.log(smooth_bi_prob) / Math.log(2))));
			} else {
				System.out.println("Smoothed Bigrams, logprob(S) = undefined");
			}
			System.out.println();
		}
	}

	private static ArrayList<Data> sentenceProbability(InputStream test, HashMap<String, Integer> unigrams,
			HashMap<String, Node> bigrams) {
		int unigrams_count = countUnigrams(unigrams);
		int biNoSmooth_count = 0;
		int biSmooth_count = 0;
		for (String word : bigrams.keySet()) {
			biNoSmooth_count += bigrams.get(word).size();// total frequencies of existed bigrams
			biSmooth_count += bigrams.get(word).getNumberOfBigram(); // number of possible bigrams start with this word
		}
		biSmooth_count += biNoSmooth_count;

		ArrayList<Data> prob = new ArrayList<Data>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(test));
			String line;
			while ((line = reader.readLine()) != null) {
				Data d = new Data(line);
				String[] st = line.toLowerCase().split("\\s+");

				//Node node = bigrams.get("PHI");
				double uni_prob = 1;
				double unsmooth_bi_prob = 1;//(double) node.getBigramFrequency(st[0]) / (double) biNoSmooth_count;
				double smooth_bi_prob = 1;//(double) (node.getBigramFrequency(st[0]) + 1) / (double) biSmooth_count;
				int i;
				String pre = "PHI";
				Node node = null;
				for (i = 0; i < st.length; i++) {
					node = bigrams.get(pre);
					uni_prob *= (double) unigrams.get(st[i]) / (double) unigrams_count;
					unsmooth_bi_prob *= (double) node.getBigramFrequency(st[i]) / (double) biNoSmooth_count;
					smooth_bi_prob *= (double) (node.getBigramFrequency(st[i]) + 1) / (double) biSmooth_count;
					pre = st[i];
				}
				
				d.setUnSmoothUnigrams(uni_prob);
				d.setUnSmoothBigrams(unsmooth_bi_prob);
				d.setSmoothBigrams(smooth_bi_prob);

				prob.add(d);
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
		return prob;
	}

	/*
	 * Returns the number of element in this map
	 */
	private static int countUnigrams(HashMap<String, Integer> unigrams) {
		int count = 0;
		for (String word : unigrams.keySet()) {
			count += unigrams.get(word);
		}
		return count;
	}

}
