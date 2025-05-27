package com.fubukicoeur;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MatchInfo {
    private final boolean multiplePhases; 
    private final boolean isFirstPhase; 
    private final String round; 
    private final String player1Tag; 
    private final String player2Tag; 
    private final Set<String> player1Characters; 
    private final Set<String> player2Characters; 
    private final int entrants; 

    /**
     * Constructs a MatchInfo object with the specified parameters.
     *
     * @param round The round of the match.
     * @param player1Tag The tag of player 1.
     * @param player2Tag The tag of player 2.
     * @param player1Characters The characters used by player 1.
     * @param player2Characters The characters used by player 2.
     * @param multiplePhases Indicates if the event has multiple phases.
     * @param entrants The number of entrants in the phase.
     * @param isFirstPhase Indicates if this is the first phase of the event.
     */
    public MatchInfo(String round, String player1Tag, String player2Tag,
            Set<String> player1Characters, Set<String> player2Characters, boolean multiplePhases, int entrants,
            boolean isFirstPhase) {
        this.isFirstPhase = isFirstPhase;
        this.multiplePhases = multiplePhases;
        this.round = round;
        this.player1Tag = player1Tag;
        this.player2Tag = player2Tag;
        this.player1Characters = player1Characters;
        this.player2Characters = player2Characters;
        this.entrants = entrants;
    }

    /**
     * Converts a set of character IDs to their corresponding names.
     *
     * @param ids The set of character IDs.
     * @return A string containing the names of the characters, separated by commas.
     */
    private static String idsToNames(Set<String> ids) {
        return ids.stream()
                .map(id -> CHARACTER_MAP.getOrDefault(id, "Unknown")) // Default to "Unknown" if the ID is not found
                .collect(Collectors.joining(", "));
    }

    /**
     * Calculates the top cut based on the number of entrants.
     * The top cut is the next power of 2 greater than or equal to the number of
     * entrants.
     *
     * @param entrants The number of entrants in the phase.
     * @return The top cut value.
     */
    private int calculateTopCut(int entrants) {
        int top = 2;
        while (top < entrants) {
            top *= 2;
        }
        return top;
    }

    /**
     * Returns a string representation of the match information.
     * The format is: "Phase - Player (Character,...) VS Player (Character,...)".
     * If multiple phases are present, it includes the phase information.
     *
     * @return A formatted string representing the match information.
     */
    @Override
    public String toString() {
        String phasePrefix = "";

        // Determine the phase prefix based on whether there are multiple phases and if
        // this is the first phase or a top cut.
        // I'm not sure if it will work with every tournament, i tested it with Kagaribi 13, Kof 5
        // and smaller tournaments from my region and it seems fine.
        if (multiplePhases) {
            if (isFirstPhase) {
                phasePrefix = "Pools";
            } else if (entrants > 0) {
                int top = calculateTopCut(entrants);
                phasePrefix = "Top " + top;
            }
        }

        String formattedPrefix = phasePrefix.isEmpty() ? "" : phasePrefix + " ";

        return String.format("%s%s - %s (%s) VS %s (%s)",
                formattedPrefix,
                round,
                player1Tag, idsToNames(player1Characters),
                player2Tag, idsToNames(player2Characters));
    }


    // Map of character IDs to their names
    // This map is used to convert character IDs to their corresponding names.
    // I used a static map because this app (for now) only supports Super Smash Bros. Ultimate,
    private static final Map<String, String> CHARACTER_MAP = Map.ofEntries(
            Map.entry("1271", "Bayonetta"),
            Map.entry("1272", "Bowser Jr."),
            Map.entry("1273", "Bowser"),
            Map.entry("1274", "Captain Falcon"),
            Map.entry("1275", "Cloud"),
            Map.entry("1276", "Corrin"),
            Map.entry("1277", "Daisy"),
            Map.entry("1278", "Dark Pit"),
            Map.entry("1279", "Diddy Kong"),
            Map.entry("1280", "Donkey Kong"),
            Map.entry("1282", "Dr. Mario"),
            Map.entry("1283", "Duck Hunt"),
            Map.entry("1285", "Falco"),
            Map.entry("1286", "Fox"),
            Map.entry("1287", "Ganondorf"),
            Map.entry("1289", "Greninja"),
            Map.entry("1290", "Ice Climbers"),
            Map.entry("1291", "Ike"),
            Map.entry("1292", "Inkling"),
            Map.entry("1293", "Jigglypuff"),
            Map.entry("1294", "King Dedede"),
            Map.entry("1295", "Kirby"),
            Map.entry("1296", "Link"),
            Map.entry("1297", "Little Mac"),
            Map.entry("1298", "Lucario"),
            Map.entry("1299", "Lucas"),
            Map.entry("1300", "Lucina"),
            Map.entry("1301", "Luigi"),
            Map.entry("1302", "Mario"),
            Map.entry("1304", "Marth"),
            Map.entry("1305", "Mega Man"),
            Map.entry("1307", "Meta Knight"),
            Map.entry("1310", "Mewtwo"),
            Map.entry("1311", "Mii Brawler"),
            Map.entry("1313", "Ness"),
            Map.entry("1314", "Olimar"),
            Map.entry("1315", "Pac-Man"),
            Map.entry("1316", "Palutena"),
            Map.entry("1317", "Peach"),
            Map.entry("1318", "Pichu"),
            Map.entry("1319", "Pikachu"),
            Map.entry("1320", "Pit"),
            Map.entry("1321", "Pokemon Trainer"),
            Map.entry("1322", "Ridley"),
            Map.entry("1323", "R.O.B."),
            Map.entry("1324", "Robin"),
            Map.entry("1325", "Rosalina"),
            Map.entry("1326", "Roy"),
            Map.entry("1327", "Ryu"),
            Map.entry("1328", "Samus"),
            Map.entry("1329", "Sheik"),
            Map.entry("1330", "Shulk"),
            Map.entry("1331", "Snake"),
            Map.entry("1332", "Sonic"),
            Map.entry("1333", "Toon Link"),
            Map.entry("1334", "Villager"),
            Map.entry("1335", "Wario"),
            Map.entry("1336", "Wii Fit Trainer"),
            Map.entry("1337", "Wolf"),
            Map.entry("1338", "Yoshi"),
            Map.entry("1339", "Young Link"),
            Map.entry("1340", "Zelda"),
            Map.entry("1341", "Zero Suit Samus"),
            Map.entry("1405", "Mr. Game & Watch"),
            Map.entry("1406", "Incineroar"),
            Map.entry("1407", "King K. Rool"),
            Map.entry("1408", "Dark Samus"),
            Map.entry("1409", "Chrom"),
            Map.entry("1410", "Ken"),
            Map.entry("1411", "Simon"),
            Map.entry("1412", "Richter"),
            Map.entry("1413", "Isabelle"),
            Map.entry("1414", "Mii Swordfighter"),
            Map.entry("1415", "Mii Gunner"),
            Map.entry("1441", "Piranha Plant"),
            Map.entry("1453", "Joker"),
            Map.entry("1526", "Hero"),
            Map.entry("1530", "Banjo Kazooie"),
            Map.entry("1532", "Terry"),
            Map.entry("1539", "Byleth"),
            Map.entry("1746", "Random"),
            Map.entry("1747", "Min Min"),
            Map.entry("1766", "Steve"),
            Map.entry("1777", "Sephiroth"),
            Map.entry("1795", "Aegis"),
            Map.entry("1846", "Kazuya"),
            Map.entry("1897", "Sora"));

}
