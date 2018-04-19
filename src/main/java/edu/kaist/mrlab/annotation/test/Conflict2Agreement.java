package edu.kaist.mrlab.annotation.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.StringTokenizer;

public class Conflict2Agreement {
	public static void main(String[] ar) throws Exception {

		BufferedReader br = Files.newBufferedReader(Paths.get("data/result/conflict_content.txt"));
		BufferedWriter bw = Files.newBufferedWriter(Paths.get("data/result/conflict2agreement_content.txt"));

		String input = null;
		while ((input = br.readLine()) != null) {

			int yesC = 0;
			int noC = 0;

			StringTokenizer st = new StringTokenizer(input, "\t");
			String sbj = st.nextToken();
			String obj = st.nextToken();
			String prd = st.nextToken();
			String stc = st.nextToken();
			String id = st.nextToken();
			String a1 = st.nextToken();
			String a2 = st.nextToken();
			String a3 = st.nextToken();

			if (a1.equals("yes")) {
				yesC++;
			} else {
				noC++;
			}

			if (a2.equals("yes")) {
				yesC++;
			} else {
				noC++;
			}

			if (a3.equals("yes")) {
				yesC++;
			} else {
				noC++;
			}

			if (yesC > noC) {
				bw.write(sbj + "\t" + obj + "\t" + prd + "\t" + stc + "\t" + id + "\t" + "yes" + "\n");
			} else {
				bw.write(sbj + "\t" + obj + "\t" + prd + "\t" + stc + "\t" + id + "\t" + "no" + "\n");
			}
		}
		bw.close();

	}
}
