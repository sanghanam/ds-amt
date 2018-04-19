package edu.kaist.mrlab.annotation.mturk;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.mturk.AmazonMTurk;
import com.amazonaws.services.mturk.AmazonMTurkClientBuilder;
import com.amazonaws.services.mturk.model.Comparator;
import com.amazonaws.services.mturk.model.CreateHITRequest;
import com.amazonaws.services.mturk.model.CreateHITResult;
import com.amazonaws.services.mturk.model.Locale;
import com.amazonaws.services.mturk.model.QualificationRequirement;

public class CreateTutorials {

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

		loadCorpus();

		File f = new File(outputPath.toString());
		if (!f.exists()) {
			f.mkdirs();
		}

		BufferedWriter bw = Files.newBufferedWriter(Paths.get("data/hit/tutorial_hit_urls_ids.txt"));
		Path inputPath;
		while ((inputPath = extractInputPath()) != null) {
			if (inputPath.toString().contains("DS_Store") || !inputPath.toString().contains("xml")) {
				continue;
			}

			String QUESTION_XML_FILE_NAME = inputPath.toString();

			System.out.println(QUESTION_XML_FILE_NAME);

			final CreateTutorials sandboxApp = new CreateTutorials(getSandboxClient());
			final HITInfo hitInfo = sandboxApp.createHIT(QUESTION_XML_FILE_NAME);

			System.out.println("Your HIT has been created. You can see it at this link:");

			System.out.println("https://workersandbox.mturk.com/mturk/preview?groupId=" + hitInfo.getHITTypeId());
			// System.out.println("https://www.mturk.com/mturk/preview?groupId=" +
			// hitInfo.getHITTypeId());

			System.out.println("Your HIT ID is: " + hitInfo.getHITId());

			bw.write(hitInfo.getHITTypeId().toString() + "\t" + hitInfo.getHITId().toString() + "\n");

			Files.move(inputPath, outputPath.resolve(inputPath.getFileName()));

		}
		bw.close();
	}

	private final AmazonMTurk client;

	private CreateTutorials(final AmazonMTurk client) {
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

		qualifications.add(localeRequirement);

		// Read the question XML into a String
		String question = new String(Files.readAllBytes(Paths.get(questionXmlFile)));

		CreateHITRequest request = new CreateHITRequest();
		request.setMaxAssignments(10);
		request.setLifetimeInSeconds(10000L);
		request.setAssignmentDurationInSeconds(3000L);
		// Reward is a USD dollar amount - USD$0.20 in the example below
		request.setReward("0.00");
		// request.setTitle("두 개체(사람, 사물, 장소 등)간 관계 태깅을 위한 튜토리얼");
		// request.setKeywords("Korean, relation extraction, kaist, distant supervision,
		// gold standard, 한국어, 관계추출, 카이스트, 원격지도학습, 골드스탠다드, 튜토리얼, tutorial");
		// request.setDescription("본격적인 관계 추출 태깅 작업을 위한 교육 단계 (Tutorial) 입니다. 이 문제를 모두
		// 풀어 학습을 완료한 사람만 본격적인 관계추출 태깅 작업을 시작할 수 있습니다.");
		request.setTitle("KAIST DS Annotation Tutorial1");
		request.setKeywords(
				"Korean, Text, relation extraction, kaist, distant supervision, gold standard, 한국어, 관계추출, 카이스트, 원격지도학습, 골드스탠다드, 튜토리얼, 교육, tutorial, education, 텍스트");
		request.setDescription("튜토리얼 문제들입니다. 이 문제를 모두 풀고 학습하면 본격적인 태깅 작업을 시작할 수 있습니다.");
		request.setQuestion(question);
		request.setQualificationRequirements(qualifications);

		CreateHITResult result = client.createHIT(request);
		return new HITInfo(result.getHIT().getHITId(), result.getHIT().getHITTypeId());
	}

	private static Path inputPath = Paths.get("data/waiting_tutorial/");
	private static Path outputPath = Paths.get("data/uploaded_tutorial/");

	private static ArrayList<String> fileList;
	private static ArrayList<Path> filePathList;

	public static void loadCorpus() throws Exception {
		fileList = new ArrayList<>();
		filePathList = Files.walk(inputPath).filter(p -> Files.isRegularFile(p))
				.collect(Collectors.toCollection(ArrayList::new));
		System.out.println("Number of file paths: " + filePathList.size());
		fileList = new ArrayList<>(new HashSet<>(fileList));
	}

	private static synchronized Path extractInputPath() {
		if (filePathList.isEmpty()) {
			return null;
		} else {
			return filePathList.remove(filePathList.size() - 1);
		}
	}

}