package ner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CompareFiles {

	public static void main(String[] args) throws IOException {
		InputStream in1 = new FileInputStream(new File(args[0]));
		InputStream in2 = new FileInputStream(new File(args[1]));
		BufferedReader reader1 = null;
		BufferedReader reader2 = null;
		try {
			reader1 = new BufferedReader(new InputStreamReader(in1));
			reader2 = new BufferedReader(new InputStreamReader(in2));
			String line1,line2;
			while ((line1 = reader1.readLine()) != null && (line2 = reader2.readLine()) != null) {
				if (! line1.equals(line2)) {
					System.out.println("my " + line1 + " her "+line2);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			reader1.close();
			reader2.close();
		}
	}

}
