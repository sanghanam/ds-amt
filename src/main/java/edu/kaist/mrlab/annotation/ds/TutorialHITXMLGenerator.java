package edu.kaist.mrlab.annotation.ds;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

public class TutorialHITXMLGenerator {

	private static int howManyQuestionsInHIT = 20;

	private static String xmlHead = "<HTMLQuestion\n"
			+ "	xmlns=\"http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2011-11-11/HTMLQuestion.xsd\">\n"
			+ "	<HTMLContent><![CDATA[<!DOCTYPE html> <html> <head>  <meta http-equiv='Content-Type' content='text/html; charset=UTF-8'/> <script type='text/javascript' src='https://s3.amazonaws.com/mturk-public/externalHIT_v1.js'></script>\n"
			+ "<script type=\"text/javascript\">" + "\n";

	private static String checkScript = "function check(obj) {\n" + "        var name = obj.name;\n"
			+ "        var value = obj.value;\n" + "        if (answer[name] === value){\n"
			+ "            var btn = document.getElementsByName(name);\n" + "                 \n" + "        } else {\n"
			+ "            alert(\"정답 아닙니다. 다시 생각해보세요.\");\n" + "             obj.checked = false;\n" + "        }\n"
			+ "        allCheck();\n" + "    }\n" + " function allCheck() {\n"
			+ "        var char = document.getElementById('mturk_form').childNodes;\n"
			+ "        // console.log(char);\n" + "        var submit_btn;\n" + "        var radio_count = 0;\n"
			+ "        var checked = 0;\n" + "        for (var h = 0; h < char.length; h++){\n"
			+ "            // console.log(char[h].nodeName);\n" + "            if (char[h].nodeName === 'P'){\n"
			+ "                var input = char[h].childNodes;\n"
			+ "                for (var b = 0; b < input.length; b ++){\n"
			+ "                    if (input[b].type === 'radio'){\n" + "                        radio_count++;\n"
			+ "                        if (input[b].checked === true){\n" + "                            checked ++;\n"
			+ "                        }\n" + "                    }if (input[b].type === 'submit'){\n"
			+ "                        submit_btn = input[b];\n" + "                    }\n" + "                }\n"
			+ "            }\n" + "        }\n" + "        if (checked === radio_count/2){\n"
			+ "            submit_btn.disabled = '';\n" + "        } else {\n"
			+ "            submit_btn.disabled = 'true';\n" + "        }\n" + "    }   </script>\n" + "    </head>\n"
			+ "    <body> <form name='mturk_form' method='post' id='mturk_form' action='https://www.mturk.com/mturk/externalSubmit'>\n"
			+ " <input type='hidden' value='' name='assignmentId' id='assignmentId'/>\n";
	private static String xmlTail = "<p><input type='submit' id='submitButton' disabled='true' value='Submit' /></p></form> 	<script language='Javascript'>turkSetAssignmentID();</script> \n"
			+ "		\n" + "	</body> </html> ]]>\n" + "	</HTMLContent>\n" + "	<FrameHeight>600</FrameHeight>\n"
			+ "</HTMLQuestion>\n" + "";
	private static String form = "<br> ";

	private Map<String, Set<String>> dsMap = new HashMap<>();
	private Map<String, String> defMap = new HashMap<>();

	public void loadDS() throws Exception {
		// BufferedReader br = Files
		// .newBufferedReader(Paths.get("data/ds/kowiki-20170701-kbox_initial-wikilink-tutorial-hand.txt"));
		BufferedReader br = Files.newBufferedReader(Paths.get("data/gold/gold_standard_ver1.txt"));

		String input = null;
		while ((input = br.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(input, "\t");
			String sbj = st.nextToken();
			String obj = st.nextToken();
			String prd = st.nextToken();
			String stc = st.nextToken();
			String id = st.nextToken();
			String answer = st.nextToken();

			String tmp = sbj + "\t" + obj + "\t" + stc + "\t" + answer;

			if (dsMap.containsKey(tmp)) {
				Set<String> temp = dsMap.get(tmp);
				temp.add(prd + "\t" + id);
				dsMap.put(tmp, temp);
			} else {
				Set<String> temp = new HashSet<>();
				temp.add(prd + "\t" + id);
				dsMap.put(tmp, temp);
			}

		}
		br.close();
	}

	public void loadDef() throws Exception {
		BufferedReader br = Files.newBufferedReader(Paths.get("data/gold/prd_def.txt"));

		String input = null;
		while ((input = br.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(input, "\t");
			String prd = st.nextToken();
			String def = st.nextToken();
			defMap.put(prd, def);
		}
	}

	public void generateXML() throws Exception {

		BufferedWriter gold = Files.newBufferedWriter(Paths.get("data/hit/gold_answer"));

		int count = 0;
		Path outFolder = Paths.get("data/waiting_tutorial/");
		File f = new File(outFolder.toString());
		if (!f.exists()) {
			f.mkdirs();
		}
		BufferedWriter bw = null;

		List<String> varAnswerList = new ArrayList<>();
		List<String> questionList = new ArrayList<>();

		for (String key : dsMap.keySet()) {

			if ((count == 0) || (count % howManyQuestionsInHIT == 0)) {
				Path filePath = Paths.get(count + "_" + (count + howManyQuestionsInHIT) + ".xml");
				bw = Files.newBufferedWriter(Paths.get(outFolder.toString(), filePath.toString()));
			}

			String h2 = "<h4> 파란색(항목 주제)과 붉은색(대상)으로 표시된 두 개체 사이의 관계로 적절한 답을 모두 고르세요. "
					+ "이때, 문장에서 명확하게 표현하고 있는 관계만 정답으로 인정됩니다. 즉, 추론으로 알아낼 수 있는 사실은 허용되지 않습니다. </h4>";
			questionList.add(h2);

			String h1 = "<h4>";
			Set<String> prdSet = dsMap.get(key);
			for (String prdId : prdSet) {
				StringTokenizer stp = new StringTokenizer(prdId);
				String prd = stp.nextToken();
				h1 += prd + ", ";
			}
			h1 = h1.substring(0, h1.length() - 2);
			h1 += " 관계의 정의:</h4>";
			questionList.add(h1);

			StringTokenizer st = new StringTokenizer(key, "\t");
			String sbj = st.nextToken();
			String obj = st.nextToken();
			String stc = st.nextToken();
			String answer = st.nextToken();

			String h3 = "";
			for (String prdId : prdSet) {
				StringTokenizer stp = new StringTokenizer(prdId);
				String prd = stp.nextToken();
				String def = defMap.get(prd);
				h3 += "<h4> " + prd + " : " + def + "</h4>\n";
				questionList.add(h3);
			}

			sbj = sbj.replace("&", "&amp;");
			obj = obj.replace("&", "&amp;");
			stc = stc.replace("&", "&amp;");
			stc = stc.replace(" [[ _sbj_ ]] ", "<a href=\"http://ko.dbpedia.org/resource/" + sbj
					+ "\" target=\"_blank\"><font color=blue>" + sbj + "</font></a>");
			stc = stc.replace(" [[ _obj_ ]] ", "<a href=\"http://ko.dbpedia.org/resource/" + obj
					+ "\" target=\"_blank\"><font color=tomato>" + obj + "</font></a>");
			stc = stc.replace(" [[ ", "");
			stc = stc.replace(" ]] ", "");
			String question = "<font size=\"4\"><b>문장 : </b> " + stc + "</font><br>";
			questionList.add(question);
			for (String prdId : prdSet) {
				StringTokenizer stp = new StringTokenizer(prdId);
				String prd = stp.nextToken();
				String def = defMap.get(prd);
				String defNL = def;
				defNL = defNL.replace("항목 주제인", "항목 주제 (이)라는");
				defNL = defNL.replace("항목 주제", "<font color=blue>" + sbj + "</font>");
				defNL = defNL + "은(는) " + "<font color=tomato>" + obj + "</font>" + "인가요?";
				questionList.add("<font size=\"4\"><b>질문 : </b>" + defNL + "</font><br><br>\n");
			}

			questionList.add(form);

			String radio = "";
			for (String prdId : prdSet) {

				StringTokenizer stp = new StringTokenizer(prdId);
				String prd = stp.nextToken();
				String id = stp.nextToken();

				String varAnswer = "'" + id + "' : '" + answer + "'";
				varAnswerList.add(varAnswer);

				gold.write(id + "\t" + answer + "\n");

				radio += "<p><b>" + prd + "</b>&nbsp;&nbsp;&nbsp;&nbsp;그렇다&nbsp;&nbsp;<input type=\"radio\" name=\""
						+ id
						+ "\" value=\"yes\" onclick=\"check(this)\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;아니다&nbsp;&nbsp;<input type=\"radio\" name=\""
						+ id + "\" value=\"no\" onclick=\"check(this)\">&nbsp;&nbsp; <b>" + "</b></p>\n";

			}
			questionList.add(radio);
			questionList.add("<br><br><br><br>");

			count++;

			if (count % howManyQuestionsInHIT == 0) {
				bw.write(xmlHead + "\n");
				String var = "var answer = {";
				for (String varAns : varAnswerList) {
					var += varAns + ", ";
				}
				var = var.substring(0, var.length() - 2);
				var += "};";
				bw.write(var + "\n");
				bw.write(checkScript);
				for (String ques : questionList) {
					bw.write(ques + "\n");
				}
				bw.write(xmlTail + "\n");
				bw.close();
				varAnswerList.clear();
				questionList.clear();
			}

		}
		
		bw.write(xmlHead + "\n");
		String var = "var answer = {";
		for (String varAns : varAnswerList) {
			var += varAns + ", ";
		}
		var = var.substring(0, var.length() - 2);
		var += "};";
		bw.write(var + "\n");
		bw.write(checkScript);
		for (String ques : questionList) {
			bw.write(ques + "\n");
		}
		bw.write(xmlTail + "\n");
		bw.close();
		
		gold.close();

	}

	public static void main(String[] ar) throws Exception {
		TutorialHITXMLGenerator hxg = new TutorialHITXMLGenerator();
		hxg.loadDS();
		hxg.loadDef();
		hxg.generateXML();
	}
}
