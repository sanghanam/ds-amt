package edu.kaist.mrlab.annotation.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class Test2 {
	public static void main(String[] ar) throws Exception {
		BufferedReader br = Files.newBufferedReader(Paths.get("data/ds/work_ids_recover"));
		BufferedReader br2 = Files
				.newBufferedReader(Paths.get("data/ds/kowiki-20170701-kbox_initial-wikilink-tutorial-hand.txt"));
		BufferedWriter bw = Files.newBufferedWriter(Paths.get("data/ds/work_ids_recover_answer"));
		String input = null;

		Map<String, String> answerMap = new HashMap<>();
		while ((input = br2.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(input, "\t");
			String sbj = st.nextToken();
			String obj = st.nextToken();
			String prd = st.nextToken();
			String stc = st.nextToken();
			String answer = st.nextToken();
			String key = sbj + "\t" + obj + "\t" + prd + "\t" + stc;
			answerMap.put(key, answer);
		}
		
		while ((input = br.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(input, "\t");
			String sbj = st.nextToken();
			String obj = st.nextToken();
			String prd = st.nextToken();
			String stc = st.nextToken();
			String id = st.nextToken();
			String key = sbj + "\t" + obj + "\t" + prd + "\t" + stc;
			String answer = answerMap.get(key);
			bw.write(key + "\t" + id + "\t" + answer + "\n");
		}
		bw.close();
	}
}
