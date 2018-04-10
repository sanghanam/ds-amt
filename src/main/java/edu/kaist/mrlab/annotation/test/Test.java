package edu.kaist.mrlab.annotation.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.StringTokenizer;

public class Test {
	public static void main(String[] ar) throws Exception {
		BufferedReader br = Files.newBufferedReader(Paths.get("data/ds/work_ids"));
		BufferedWriter bw = Files.newBufferedWriter(Paths.get("data/ds/work_ids_recover"));
		String input = null;

		int num = -1;
		int idx = 1;
		int diff = 0;
		while ((input = br.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(input, "\t");
			if (st.countTokens() == 5) {
				String sbj = st.nextToken();
				String obj = st.nextToken();
				String prd = st.nextToken();
				String stc = st.nextToken();
				String id = st.nextToken();
				num = Integer.parseInt(id.substring(id.length() - idx, id.length()));
				String relation = id.substring(0, id.length() - idx);
				if (num == 9) {
					idx++;
				} else if (num == 99) {
					idx++;
				}

				bw.write(sbj + "\t" + obj + "\t" + prd + "\t" + stc + "\t" + relation + (num + diff) + "\n");
			}
			if (st.countTokens() == 1) {
				String id = st.nextToken();
				num = Integer.parseInt(id.replace("isPartOfMilitaryConflict", ""));
				diff++;
			}

		}
		bw.close();
	}
}
