package edu.kaist.mrlab.annotation.mturk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.mturk.AmazonMTurk;
import com.amazonaws.services.mturk.AmazonMTurkClientBuilder;
import com.amazonaws.services.mturk.model.Comparator;
import com.amazonaws.services.mturk.model.CreateHITRequest;
import com.amazonaws.services.mturk.model.CreateHITResult;
import com.amazonaws.services.mturk.model.Locale;
import com.amazonaws.services.mturk.model.QualificationRequirement;

public class CreateWorks {

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
		
		File f = new File(outputPath.toString());
		if(!f.exists()) {
			f.mkdirs();
		}

		loadCorpus();

		BufferedWriter bw = Files.newBufferedWriter(Paths.get("data/hit/work_hit_urls_ids.txt"));
		Path inputPath;
		while ((inputPath = extractInputPath()) != null) {
			if (inputPath.toString().contains("DS_Store") || !inputPath.toString().contains("xml")) {
				continue;
			}

			String QUESTION_XML_FILE_NAME = inputPath.toString();

			System.out.println(QUESTION_XML_FILE_NAME);

			final CreateWorks sandboxApp = new CreateWorks(getSandboxClient());
			final HITInfo hitInfo = sandboxApp.createHIT(QUESTION_XML_FILE_NAME);

			// final CreateWorks prodApp = new CreateWorks(getProdClient());
			// final HITInfo hitInfo = prodApp.createHIT(QUESTION_XML_FILE_NAME);

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

	private CreateWorks(final AmazonMTurk client) {
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
		
		BufferedReader br = Files.newBufferedReader(Paths.get("data/qual/qualification_type_ids.txt"));
		String weedQualificationID = null;
		String workerQualificationID = null;
		String input = null;
		while((input = br.readLine()) != null) {
			if(input.contains("weedQual")) {
				StringTokenizer st = new StringTokenizer(input, "\t");
				st.nextToken(); // name;
				weedQualificationID = st.nextToken();
			} else if(input.contains("workerQual")) {
				StringTokenizer st = new StringTokenizer(input, "\t");
				st.nextToken(); // name;
				workerQualificationID = st.nextToken();
			}
		}

		QualificationRequirement customRequirement = new QualificationRequirement();
		customRequirement.setQualificationTypeId(weedQualificationID);
		customRequirement.setRequiredToPreview(true);
		customRequirement.setComparator(Comparator.GreaterThanOrEqualTo);
		customRequirement.setIntegerValues(Collections.singleton(80));
		
		QualificationRequirement workerRequirement = new QualificationRequirement();
		workerRequirement.setQualificationTypeId(workerQualificationID);
		workerRequirement.setRequiredToPreview(true);
		workerRequirement.setComparator(Comparator.EqualTo);
		workerRequirement.setIntegerValues(Collections.singleton(1));

		qualifications.add(localeRequirement);
		qualifications.add(customRequirement);
		qualifications.add(workerRequirement);

		// Read the question XML into a String
		String question = new String(Files.readAllBytes(Paths.get(questionXmlFile)));

		CreateHITRequest request = new CreateHITRequest();
		request.setMaxAssignments(2);
		// 24 hours
		request.setLifetimeInSeconds(86400L);
		// 50 mins
		request.setAssignmentDurationInSeconds(3000L);
		// Reward is a USD dollar amount - USD$0.20 in the example below
		request.setReward("0.5");
		request.setTitle("KAIST DS Annotation Work");
		request.setKeywords("Korean, relation extraction, kaist, distant supervision, gold standard, 한국어, 관계추출, 카이스트, 원격지도학습, 골드스탠다드");
		request.setDescription("튜토리얼 완료 후 진행할 작업들입니다. 주어진 문장을 읽고, 문장 속 두 개체간 적절한 관계를 선택해주세요.");
		request.setQuestion(question);
		request.setQualificationRequirements(qualifications);

		CreateHITResult result = client.createHIT(request);
		return new HITInfo(result.getHIT().getHITId(), result.getHIT().getHITTypeId());
	}

	private static Path inputPath = Paths.get("data/waiting_work/");
	private static Path outputPath = Paths.get("data/uploaded_work/");

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