package edu.kaist.mrlab.annotation.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.mturk.AmazonMTurk;
import com.amazonaws.services.mturk.AmazonMTurkClientBuilder;
import com.amazonaws.services.mturk.model.Comparator;
import com.amazonaws.services.mturk.model.CreateHITRequest;
import com.amazonaws.services.mturk.model.CreateHITResult;
import com.amazonaws.services.mturk.model.Locale;
import com.amazonaws.services.mturk.model.QualificationRequirement;

public class CreateHIT {

//	private static final String QUESTION_XML_FILE_NAME = "data/annotation_sample.xml";
	 private static final String QUESTION_XML_FILE_NAME = "data/waiting_innerwork_tutorial/0_20.xml";

	private static final String SANDBOX_ENDPOINT = "mturk-requester-sandbox.us-east-1.amazonaws.com";
	private static final String PROD_ENDPOINT = "https://mturk-requester.us-east-1.amazonaws.com";
	private static final String SIGNING_REGION = "us-east-1";

	public static void main(final String[] argv) throws Exception {
		/*
		 * Use the Amazon Mechanical Turk Sandbox to publish test Human Intelligence
		 * Tasks (HITs) without paying any money. Sign up for a Sandbox account at
		 * https://requestersandbox.mturk.com/ with the same credentials as your main
		 * MTurk account
		 * 
		 * Switch to getProdClient() in production. Uncomment line 60, 61, & 66 below to
		 * create your HIT in production.
		 * 
		 */

		final CreateHIT sandboxApp = new CreateHIT(getSandboxClient());
		final HITInfo hitInfo = sandboxApp.createHIT(QUESTION_XML_FILE_NAME);

		// final CreateHITTutorial prodApp = new CreateHITTutorial(getProdClient());
		// final HITInfo hitInfo = prodApp.createHIT(QUESTION_XML_FILE_NAME);

		System.out.println("Your HIT has been created. You can see it at this link:");

		System.out.println("https://workersandbox.mturk.com/mturk/preview?groupId=" + hitInfo.getHITTypeId());
		// System.out.println("https://www.mturk.com/mturk/preview?groupId=" +
		// hitInfo.getHITTypeId());

		System.out.println("Your HIT ID is: " + hitInfo.getHITId());

	}

	private final AmazonMTurk client;

	private CreateHIT(final AmazonMTurk client) {
		this.client = client;
	}

	private static AmazonMTurk getSandboxClient() {
		AmazonMTurkClientBuilder builder = AmazonMTurkClientBuilder.standard();
		builder.setEndpointConfiguration(new EndpointConfiguration(SANDBOX_ENDPOINT, SIGNING_REGION));
		return builder.build();
	}

	private static AmazonMTurk getProdClient() {
		AmazonMTurkClientBuilder builder = AmazonMTurkClientBuilder.standard();
		builder.setEndpointConfiguration(new EndpointConfiguration(PROD_ENDPOINT, SIGNING_REGION));
		return builder.build();
	}

	private static final class HITInfo {
		private final String hitId;
		private final String hitTypeId;

		private HITInfo(final String hitId, final String hitTypeId) {
			this.hitId = hitId;
			this.hitTypeId = hitTypeId;
		}

		private String getHITId() {
			return this.hitId;
		}

		private String getHITTypeId() {
			return this.hitTypeId;
		}

	}

	private HITInfo createHIT(final String questionXmlFile) throws IOException {

		List<QualificationRequirement> qualifications = new ArrayList<>();

		// QualificationRequirement: Locale IN (KR)
		QualificationRequirement localeRequirement = new QualificationRequirement();
		localeRequirement.setQualificationTypeId("00000000000000000071");
		localeRequirement.setComparator(Comparator.In);
		List<Locale> localeValues = new ArrayList<>();
		localeValues.add(new Locale().withCountry("KR"));
		localeRequirement.setLocaleValues(localeValues);
		localeRequirement.setRequiredToPreview(true);

		QualificationRequirement weedoutRequirement = new QualificationRequirement();
		weedoutRequirement.setQualificationTypeId("3OSEKP8FGNM91XB7E7X45Z740D42K7");
		weedoutRequirement.setRequiredToPreview(true);
		weedoutRequirement.setComparator(Comparator.GreaterThan);
		weedoutRequirement.setIntegerValues(Collections.singleton(80));

		QualificationRequirement workerRequirement = new QualificationRequirement();
		workerRequirement.setQualificationTypeId("3T04ZEB6XQEVOHL8KZMD9S7KFB15AW");
		workerRequirement.setRequiredToPreview(true);
		workerRequirement.setComparator(Comparator.EqualTo);
		workerRequirement.setIntegerValues(Collections.singleton(1));
		
		qualifications.add(localeRequirement);
//		qualifications.add(weedoutRequirement);
//		qualifications.add(workerRequirement);

		// Read the question XML into a String
		String question = new String(Files.readAllBytes(Paths.get(questionXmlFile)));

		CreateHITRequest request = new CreateHITRequest();
		request.setMaxAssignments(1);
		request.setLifetimeInSeconds(1500L);
		request.setAssignmentDurationInSeconds(1000L);
		// Reward is a USD dollar amount - USD$0.20 in the example below
		request.setReward("0.01");
		request.setTitle("KAIST Test");
		request.setKeywords("Korean, relation extraction, kaist, distant supervision, gold standard");
		request.setDescription("Testing...");
		request.setQuestion(question);
		request.setQualificationRequirements(qualifications);

		CreateHITResult result = client.createHIT(request);
		return new HITInfo(result.getHIT().getHITId(), result.getHIT().getHITTypeId());
	}

}