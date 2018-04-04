package edu.kaist.mrlab.annotation.mturk;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.mturk.AmazonMTurk;
import com.amazonaws.services.mturk.AmazonMTurkClientBuilder;
import com.amazonaws.services.mturk.model.CreateQualificationTypeRequest;
import com.amazonaws.services.mturk.model.CreateQualificationTypeResult;

public class CreateQualificationType {

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

		BufferedWriter bw = Files.newBufferedWriter(Paths.get("data/qual/qualification_type_ids.txt"));

		final CreateQualificationType sandboxApp = new CreateQualificationType(getSandboxClient());
		final QUALInfo weedQual = sandboxApp.createWeedoutQualification();
		final QUALInfo workerQual = sandboxApp.createWorkerIDQualification();

		System.out.println("Your Weedout Qualification Type is: " + weedQual.getQualTypeID());
		System.out.println("Your Worker Qualification Type is: " + workerQual.getQualTypeID());

		bw.write("weedQual" + "\t" + weedQual.getQualTypeID() + "\n");
		bw.write("workerQual" + "\t" + workerQual.getQualTypeID() + "\n");

		bw.close();
	}

	private final AmazonMTurk client;

	private CreateQualificationType(final AmazonMTurk client) {
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

	private static final class QUALInfo {
		private final String qualTypeID;

		private QUALInfo(final String qualTypeID) {
			this.qualTypeID = qualTypeID;
		}

		private String getQualTypeID() {
			return this.qualTypeID;
		}
	}

	private QUALInfo createWeedoutQualification() throws IOException {

		String qualification = new String(Files.readAllBytes(Paths.get("data/weedout_question.xml")));
		String answer = new String(Files.readAllBytes(Paths.get("data/weedout_answer.xml")));

		CreateQualificationTypeRequest createQual = new CreateQualificationTypeRequest();
		createQual.setName("Weed-out test");
		createQual.setDescription("이 테스트를 80점 이상으로 통과하면 작업을 시작할 수 있습니다.");
		createQual.setQualificationTypeStatus("Active");
		createQual.setTest(qualification);
		createQual.setTestDurationInSeconds(300L);
		createQual.setAnswerKey(answer);
		createQual.setAutoGranted(false);
		createQual.setRetryDelayInSeconds(86400L);
		createQual
				.setKeywords("Korean, relation extraction, kaist, distant supervision, gold standard, 한국어, 관계추출, 카이스트, 원격지도학습, 골드스탠다드");

		CreateQualificationTypeResult qualResult = client.createQualificationType(createQual);
		return new QUALInfo(qualResult.getQualificationType().getQualificationTypeId());

	}

	private QUALInfo createWorkerIDQualification() throws IOException {

		CreateQualificationTypeRequest createQual = new CreateQualificationTypeRequest();
		createQual.setName("Tutorial test");
		createQual.setDescription("Tutorial을 완료하면 자동 작업 승인됩니다.");
		createQual.setQualificationTypeStatus("Active");
		createQual.setKeywords(
				"Korean, relation extraction, kaist, distant supervision, gold standard, 한국어, 관계추출, 카이스트, 원격지도학습, 골드스탠다드, 튜토리얼, 교육, education, tutorial");

		CreateQualificationTypeResult qualResult = client.createQualificationType(createQual);
		return new QUALInfo(qualResult.getQualificationType().getQualificationTypeId());

	}

}