package ner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

public class NerML {
	private final static String PHI = "PHI";
	private final static String PHIPOS = "PHIPOS";
	private final static String OMEGA = "OMEGA";
	private final static String OMEGAPOS = "OMEGAPOS";
	private final static String UNK = "UNK";
	private final static String UNKPOS = "UNKPOS";

	public static void main(String[] args) throws IOException {
		// hashset of ftype
		HashSet<String> ftype = new HashSet<>();
		for (int i = 3; i < args.length; i++) {
			ftype.add(args[i]);
		}

		HashSet<String> locs = getLocations(args[2]);
		ArrayList<ArrayList<Word>> train = getWords(args[0]);

		HashSet<String> trainwords = new HashSet<>();
		HashSet<String> trainpos = new HashSet<>();
		int train_size = train.size();
		for (int i = 0; i < train_size; i++) {
			ArrayList<Word> s = train.get(i);
			int s_size = s.size();
			for (int j = 0; j < s_size; j++) {
				Word w = s.get(j);
				trainwords.add(w.getWord());
				trainpos.add(w.getPos());
			}
		}

		HashMap<String, Integer> feature = generateTrainFeature(train, ftype);
		// train.readable
		ArrayList<Node> trainNodes = generateTrainNodes(train, ftype, locs);
		generateReadableFile(trainNodes, "train.txt.readable");
		// test.readable

		ArrayList<ArrayList<Word>> test = getWords(args[1]);
		ArrayList<Node> testNodes = generateTestNodes(test, ftype, locs, trainwords, trainpos);
		generateReadableFile(testNodes, "test.txt.readable");

		// SET UP VECTOR
		HashMap<String, Integer> integerLabel = new HashMap<>();
		integerLabel.put("O", 0);
		integerLabel.put("B-PER", 1);
		integerLabel.put("I-PER", 2);
		integerLabel.put("B-LOC", 3);
		integerLabel.put("I-LOC", 4);
		integerLabel.put("B-ORG", 5);
		integerLabel.put("I-ORG", 6);
		// train.vector
		generateVector(trainNodes, feature, integerLabel, "train.txt.vector");
		// // test.vector
		// generateVector(testNodes, feature, integerLabel, "test.txt.vector");

	}

	private static HashSet<String> getLocations(String filename) throws FileNotFoundException {
		InputStream in = new FileInputStream(new File(filename));
		HashSet<String> data = new HashSet<>();
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = reader.readLine()) != null) {
				data.add(line);
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
		return data;
	}

	private static ArrayList<ArrayList<Word>> getWords(String filename) throws FileNotFoundException, IOException {
		// InputStream in = new FileInputStream(new File(filename));
		ArrayList<ArrayList<Word>> data = new ArrayList<>();
		ArrayList<Word> sentence;
		String[] split;
		Scanner reader = new Scanner(new FileReader(filename));
		String line;

		while (reader.hasNextLine()) {
			sentence = new ArrayList<>();
			while (reader.hasNextLine() && (line = reader.nextLine()).isEmpty() == false) {
				// System.out.println(line);
				split = line.split("\\s+");
				sentence.add(new Word(split[0], split[1], split[2]));
			}
			if (sentence.isEmpty() == false) {
				data.add(sentence);
			}
		}

		reader.close();

		return data;
	}

	private static HashMap<String, Integer> generateTrainFeature(ArrayList<ArrayList<Word>> train,
			HashSet<String> ftype) {
		HashMap<String, Integer> data = new HashMap<>();
		int al_size = train.size();
		int t_size = -1;
		int count = 0;
		String key = null;
		String pos = null;

		ArrayList<Word> temp;
		// all words
		for (int i = 0; i < al_size; i++) {
			temp = train.get(i);
			t_size = temp.size();
			for (int j = 0; j < t_size; j++) {
				key = "word-" + temp.get(j).getWord();
				if (data.containsKey(key) == false) {
					data.put(key, count++);
				}
				pos = temp.get(j).getPos();
				key = "pos-" + pos;
				if (data.containsKey(key) == false) {
					data.put(key, count++);
				}
				key = "prev-pos-" + pos;
				if (data.containsKey(key) == false) {
					data.put(key, count++);
				}
				key = "next-pos-" + pos;
				if (data.containsKey(key) == false) {
					data.put(key, count++);
				}
			}
		}
		data.put("word-UNK", count++);
		// all pos
		data.put("prev-pos-PHIPOS", count++);
		data.put("next-pos-OMEGAPOS", count++);

		if (ftype.contains("ABBR")) {
			data.put("abbreviated", count++);
		}
		if (ftype.contains("CAP")) {
			data.put("capitalized", count++);
		}
		if (ftype.contains("LOCATION")) {
			data.put("islocation", count++);
		}
		return data;
	}

	private static ArrayList<Node> generateTrainNodes(ArrayList<ArrayList<Word>> inputFile, HashSet<String> ftype,
			HashSet<String> locs) {
		int inputLength = inputFile.size();
		ArrayList<Node> data = new ArrayList<Node>();
		ArrayList<Node> temp;
		for (int i = 0; i < inputLength; i++) {
			temp = generateSentenceTrainNodes(inputFile.get(i), ftype, locs);
			data.addAll(temp);
		}
		return data;
	}

	private static ArrayList<Node> generateSentenceTrainNodes(ArrayList<Word> sentence, HashSet<String> ftype,
			HashSet<String> locs) {
		ArrayList<Node> data = new ArrayList<>();
		int s_len = sentence.size();

		Node current = null;
		String wordcon, prev_word, next_word, pos, prev_pos, next_pos, poscon = null;

		for (int i = 0; i < s_len; i++) {
			current = new Node(sentence.get(i).getLabel(), sentence.get(i).getWord());
			// wordcon
			prev_word = i - 1 >= 0 ? sentence.get(i - 1).getWord() : PHI;
			next_word = i + 1 < s_len ? sentence.get(i + 1).getWord() : OMEGA;
			wordcon = ftype.contains("WORDCON") ? prev_word + " " + next_word : "n/a";
			current.setWORDCON(wordcon);

			pos = ftype.contains("POS") ? sentence.get(i).getPos() : "n/a";
			current.setPOS(pos);
			// poscon
			prev_pos = i - 1 >= 0 ? sentence.get(i - 1).getPos() : PHIPOS;
			next_pos = i + 1 < s_len ? sentence.get(i + 1).getPos() : OMEGAPOS;
			poscon = ftype.contains("POSCON") ? prev_pos + " " + next_pos : "n/a";
			current.setPOSCON(poscon);

			current.setABBR(ftype.contains("ABBR"));
			current.setCAP(ftype.contains("CAP"));
			current.setLocation(ftype.contains("LOCATION"), locs.contains(sentence.get(i).getWord()));

			data.add(current);
		}

		return data;
	}

	private static void generateReadableFile(ArrayList<Node> nodes, String filename) throws IOException {
		File file = new File(filename);
		if (!file.exists()) {
			file.createNewFile();
		}

		FileWriter fw = null;
		BufferedWriter writer = null;
		int size = nodes.size();
		String[] all = null;
		try {
			fw = new FileWriter(file);
			writer = new BufferedWriter(fw);
			for (int i = 0; i < size; i++) {
				all = nodes.get(i).getAll();

				writer.write("WORD: " + all[1]);
				writer.newLine();
				writer.write("WORDCON: " + all[2]);
				writer.newLine();
				writer.write("POS: " + all[3]);
				writer.newLine();
				writer.write("POSCON: " + all[4]);
				writer.newLine();
				writer.write("ABBR: " + all[5]);
				writer.newLine();
				writer.write("CAP: " + all[6]);
				writer.newLine();
				writer.write("LOCATION: " + all[7]);

				writer.newLine();
				writer.newLine();
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private static ArrayList<Node> generateTestNodes(ArrayList<ArrayList<Word>> test, HashSet<String> ftype,
			HashSet<String> locs, HashSet<String> trainwords, HashSet<String> trainpos) {
		int inputLength = test.size();
		ArrayList<Node> data = new ArrayList<Node>();
		ArrayList<Node> temp;
		for (int i = 0; i < inputLength; i++) {
			temp = generateSentenceTestNodes(test.get(i), ftype, locs, trainwords, trainpos);
			data.addAll(temp);
		}
		return data;
	}

	private static ArrayList<Node> generateSentenceTestNodes(ArrayList<Word> sentence, HashSet<String> ftype,
			HashSet<String> locs, HashSet<String> trainwords, HashSet<String> trainpos) {
		ArrayList<Node> data = new ArrayList<>();
		int s_len = sentence.size();
		Node current = null;
		String wordcon, prev_word, next_word, pos, prev_pos, next_pos, poscon = null;

		if (!trainwords.contains(sentence.get(0).getWord())) {
			sentence.get(0).setWord(UNK);
		}
		if (!trainpos.contains(sentence.get(0).getPos())) {
			sentence.get(0).setPos(UNKPOS);
		}
		current = new Node(sentence.get(0).getLabel(), sentence.get(0).getWord());

		if (!trainwords.contains(sentence.get(1).getWord())) {
			sentence.get(1).setWord(UNK);
		}
		wordcon = ftype.contains("WORDCON") ? PHI + " " + sentence.get(1).getWord() : "n/a";
		current.setWORDCON(wordcon);

		pos = ftype.contains("POS") ? sentence.get(0).getPos() : "n/a";
		current.setPOS(pos);

		if (!trainpos.contains(sentence.get(1).getPos())) {
			sentence.get(1).setPos(UNKPOS);
		}
		poscon = ftype.contains("POSCON") ? PHIPOS + " " + sentence.get(1).getPos() : "n/a";
		current.setPOSCON(poscon);

		current.setABBR(ftype.contains("ABBR"), sentence.get(0).getOrigin());
		current.setCAP(ftype.contains("CAP"), sentence.get(0).getOrigin());
		current.setLocation(ftype.contains("LOCATION"), locs.contains(sentence.get(0).getOrigin()));

		data.add(current);
		for (int i = 1; i < s_len - 1; i++) {
			current = new Node(sentence.get(i).getLabel(), sentence.get(i).getWord());

			if (!trainwords.contains(sentence.get(i + 1).getWord())) {
				sentence.get(i + 1).setWord(UNK);
			}
			wordcon = ftype.contains("WORDCON") ? sentence.get(i - 1).getWord() + " " + sentence.get(i + 1).getWord()
					: "n/a";
			current.setWORDCON(wordcon);

			pos = ftype.contains("POS") ? sentence.get(i).getPos() : "n/a";
			current.setPOS(pos);

			if (!trainpos.contains(sentence.get(i + 1).getPos())) {
				sentence.get(i + 1).setPos(UNKPOS);
			}
			poscon = ftype.contains("POSCON") ? sentence.get(i - 1).getPos() + " " + sentence.get(i + 1).getPos()
					: "n/a";
			current.setPOSCON(poscon);

			current.setABBR(ftype.contains("ABBR"), sentence.get(i).getOrigin());
			current.setCAP(ftype.contains("CAP"), sentence.get(i).getOrigin());
			current.setLocation(ftype.contains("LOCATION"), locs.contains(sentence.get(i).getOrigin()));

			data.add(current);
		}
		current = new Node(sentence.get(s_len - 1).getLabel(), sentence.get(s_len - 1).getWord());

		wordcon = ftype.contains("WORDCON") ? sentence.get(s_len - 2).getWord() + " " + OMEGA : "n/a";
		current.setWORDCON(wordcon);

		pos = ftype.contains("POS") ? sentence.get(s_len - 1).getPos() : "n/a";
		current.setPOS(pos);

		poscon = ftype.contains("POSCON") ? sentence.get(s_len - 2).getPos() + " " + OMEGAPOS : "n/a";
		current.setPOSCON(poscon);

		current.setABBR(ftype.contains("ABBR"), sentence.get(s_len - 1).getOrigin());
		current.setCAP(ftype.contains("CAP"), sentence.get(s_len - 1).getOrigin());
		current.setLocation(ftype.contains("LOCATION"), locs.contains(sentence.get(s_len - 1).getOrigin()));

		data.add(current);
		return data;
	}
	private static void generateVector(ArrayList<Node> nodes, HashMap<String, Integer> feature,
			HashMap<String, Integer> integerLabel, String filename) throws IOException {
		File file = new File(filename);
		if (!file.exists()) {
			file.createNewFile();
		}

		FileWriter fw = null;
		BufferedWriter writer = null;
		int size = nodes.size();
		String[] all = null;
		String[] split = null;
		try {
			fw = new FileWriter(file);
			writer = new BufferedWriter(fw);
			StringBuilder sb = new StringBuilder();
			// first line
			int i = 0;
			all = nodes.get(i).getAll();
			// label
			sb.append(integerLabel.get(all[0]));
			sb.append(" ");
			// features
			split = all[4].split("\\s+");
			ArrayList<Integer> index = new ArrayList<>();
			index.add(feature.get("pos-" + all[3]));
			index.add(feature.get("prev-pos-" + split[0]));
			index.add(feature.get("next-pos-" + split[1]));

			if (all[5].equals("yes")) {
				index.add(feature.get("abbreviated"));
			}
			if (all[6].equals("yes")) {
				index.add(feature.get("capitalized"));
			}
			if (all[7].equals("yes")) {
				index.add(feature.get("islocation"));
			}
			Collections.sort(index);
			// pos, prev pos, next pos
			for (int j = 0; j < index.size(); j++) {
				sb.append(index.get(j));
				sb.append(":1 ");
			}
			writer.write(sb.toString());
			sb.setLength(0);

			for (i = 1; i < size; i++) {
				writer.newLine();
				all = nodes.get(i).getAll();
				// label
				sb.append(integerLabel.get(all[0]));
				sb.append(" ");
				// features
				split = all[4].split("\\s+");
				index = new ArrayList<>();
				index.add(feature.get("pos-" + all[3]));
				index.add(feature.get("prev-pos-" + split[0]));
				index.add(feature.get("next-pos-" + split[1]));

				if (all[5].equals("yes")) {
					index.add(feature.get("abbreviated"));
				}
				if (all[6].equals("yes")) {
					index.add(feature.get("capitalized"));
				}
				if (all[7].equals("yes")) {
					index.add(feature.get("islocation"));
				}
				Collections.sort(index);
				// pos, prev pos, next pos
				for (int j = 0; j < index.size(); j++) {
					sb.append(index.get(j));
					sb.append(":1 ");
				}
				writer.write(sb.toString());
				sb.setLength(0);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e1) {
			System.out.println(all[1]);
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
}
