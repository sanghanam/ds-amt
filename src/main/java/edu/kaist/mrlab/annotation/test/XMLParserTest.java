package edu.kaist.mrlab.annotation.test;

import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class XMLParserTest {
	public static void main(String[] ar) throws Exception {
		
		List<String> identifierList = new ArrayList<String>();
		List<String> answerList = new ArrayList<String>();
		
		String answerXML = new String(Files.readAllBytes(Paths.get("data/answer_sample.xml")));
		
		InputSource is = new InputSource(new StringReader(answerXML));
		Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
		XPath xpath = XPathFactory.newInstance().newXPath();
		NodeList identifiers = (NodeList)xpath.evaluate("//Answer/QuestionIdentifier", document, XPathConstants.NODESET);
		for(int i = 0; i < identifiers.getLength(); i++) {
			Node identifier = identifiers.item(i);
			String relation = identifier.getTextContent();
			identifierList.add(relation);
		}
		
		NodeList texts = (NodeList)xpath.evaluate("//Answer/FreeText", document, XPathConstants.NODESET);
		for(int i = 0; i < texts.getLength(); i++) {
			Node text = texts.item(i);
			String answer = text.getTextContent();
			answerList.add(answer);
		}
		
		for(int i = 0; i < identifierList.size(); i++) {
			System.out.println(identifierList.get(i) + answerList.get(i));
		}
		
		
		
		
	}
}
