package edu.kaist.mrlab.annotation.ds;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

public class RelationFilter {

	public static enum AdmittedRelation {
		country, publisher, locatedInArea, occupation, city, artist, league, party, child, ground, location, genre, musicalArtist, channel, spouse, team, battle, commander, relative, starring, nationalTeam, industry, parent, leaderName, populationPlace, region, language, award, knownFor, birthPlace, predecessor, club, currentMember, tenant, author, routeEnd, capital, producer, keyPerson, director, computingPlatform, nationality, album, product, distributor, headquarter, owningOrganisation, ideology, manufacturer, place, developer, languageFamily, opponent, recordLabel, foundedBy, routeStart, instrument, operator, deathPlace, managerClub, owner, residence, successor, parentCompany, notableWork, officialLanguage, youthClub, education, garrison, operatingSystem, creativeDirector, writer, vicePresident, recordedIn, religion, influenced, largestCity, previousWork, basedOn, subsequentWork, pastMember, field, influencedBy, colour, restingPlace, composer, hubAirport, sourceMountain, musicalBand
	}

	public static void main(String[] ar) throws Exception {

		Set<String> relSet = new HashSet<>();
		for (AdmittedRelation adr : AdmittedRelation.values()) {
			relSet.add(adr.toString());
		}

		BufferedReader br = Files.newBufferedReader(Paths.get("data/ds/kowiki-20170701-kbox_initial-wikilink.txt"));
		BufferedWriter bw = Files
				.newBufferedWriter(Paths.get("data/ds/kowiki-20170701-kbox_initial-wikilink-work.txt"));
		String input = null;
		while ((input = br.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(input, "\t");
			String sbj = st.nextToken();
			String obj = st.nextToken();
			String prd = st.nextToken();
			String stc = st.nextToken();

			if (relSet.contains(prd)) {
				stc = stc.replace(" << ", " [[ ");
				stc = stc.replace(" >> ", " ]] ");
				stc = stc.replace("<", "《");
				stc = stc.replace(">", "》");
				stc = stc.replace("𥘺", "");
				bw.write(sbj + "\t" + obj + "\t" + prd + "\t" + stc + "\n");
			}

		}
		bw.close();
	}
}
