package com.fubukicoeur;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The goal of this is to make all the needed calls the the API,
 * right now i use a post request method and the getAllStreamedSetDetails,
 * i'm not sure if thats the best way to do that because i'm very new to those things
 * this works with the MatchInfo class.
 */
public class ApiCalls {
  private final String apiToken; 
  private final String eventSlug; 
  private static final String API_URL = "https://api.start.gg/gql/alpha"; 
  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Constructor for this class.
   *
   * @param apiToken The API token for authentication.
   * @param eventSlug The slug of the event to query.
   */
  public ApiCalls(String apiToken, String eventSlug) {
    this.apiToken = apiToken;
    this.eventSlug = eventSlug;
  }

  /**
   * Performs a POST request to the Start.gg API with the specified query and variables.
   *
   * @param query The GraphQL query to execute.
   * @param variables The variables for the GraphQL query.
   * @return The response body as a string.
   */
  private String performPostRequest(String query, Map<String, Object> variables) throws Exception {
    String requestBodyJson = objectMapper.writeValueAsString(Map.of("query", query, "variables", variables));

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(API_URL))
        .header("Content-Type", "application/json")
        .header("Authorization", "Bearer " + apiToken)
        .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
        .build();

    HttpClient client = HttpClient.newHttpClient();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    return response.body();
  }


  /**
   * Retrieves all streamed set details for the specified event.
   *
   * @param progressCallback Optional callback to report progress updates
   * @return A list of MatchInfo objects containing details about each streamed match.
   * @throws Exception If an error occurs while fetching the data.
   */
  public List<MatchInfo> getAllStreamedSetDetails(App.ProgressCallback progressCallback) throws Exception {
    // The query might not be the best, i don't really know i tried different queries untinl it worked
    String query = """
            query($eventSlug: String!, $page: Int!) {
              event(slug: $eventSlug) {
                phases {
                  id
                  numSeeds
                  name
                }
                sets(page: $page, perPage: 30) { 
                  pageInfo {
                    totalPages
                  }
                  nodes {
                    id
                    fullRoundText
                    stream {
                      streamName
                    }
                    phaseGroup {
                      phase {
                        id
                        name
                      }
                    }
                    slots {
                      entrant {
                        name
                        participants {
                          player {
                            gamerTag
                          }
                        }
                      }
                    }
                    games {
                      selections {
                        selectionType
                        selectionValue
                        entrant {
                          name
                        }
                      }
                    }
                  }
                }
              }
            }
        """;

    // We create a list that will store the matchs with a streamname element
    List<MatchInfo> matches = new ArrayList<>();
    int page = 1;
    int totalPages = 1;

    Map<String, Integer> phaseNameToEntrants = new HashMap<>();
    String firstPhaseId = null;
    boolean multiplePhases = false;

    do {
      if (progressCallback != null) {
        progressCallback.onProgress(page, totalPages, eventSlug);
      }
      

      System.out.println("Fetching page " + page + "/" + totalPages + " of sets for event: " + eventSlug);
      
      Map<String, Object> variables = Map.of("eventSlug", eventSlug, "page", page);
      String response = performPostRequest(query, variables);
      JsonNode root = objectMapper.readTree(response).path("data").path("event");

      if (page == 1) {
        JsonNode phasesNode = root.path("phases");
        if (phasesNode.isArray()) {
          multiplePhases = phasesNode.size() > 1;

          for (int i = 0; i < phasesNode.size(); i++) {
            JsonNode phaseNode = phasesNode.get(i);
            String name = phaseNode.path("name").asText();
            int entrants = phaseNode.path("numSeeds").asInt(0);
            String id = phaseNode.path("id").asText();

            phaseNameToEntrants.put(name, entrants);
            if (i == 0) {
              firstPhaseId = id; 
            }
          }
        }
      }

      JsonNode setsNode = root.path("sets");
      JsonNode nodes = setsNode.path("nodes");

      if (page == 1) {
        totalPages = setsNode.path("pageInfo").path("totalPages").asInt(1);
      }

      for (JsonNode setNode : nodes) {
        if (setNode.path("stream").isNull()) continue;

        String round = setNode.path("fullRoundText").asText("Unknown");
        JsonNode phaseNode = setNode.path("phaseGroup").path("phase");

        String phaseName = phaseNode.path("name").asText("Unknown");
        String phaseId = phaseNode.path("id").asText("");
        int entrants = phaseNameToEntrants.getOrDefault(phaseName, 0);
        boolean isFirstPhase = phaseId.equals(firstPhaseId);

        // Players
        List<String> playerTags = new ArrayList<>();
        List<String> entrantNames = new ArrayList<>();
        for (JsonNode slot : setNode.path("slots")) {
          JsonNode entrant = slot.path("entrant");
          if (entrant.isMissingNode()) continue;

          JsonNode participants = entrant.path("participants");
          if (!participants.isArray() || participants.size() == 0) continue;

          JsonNode player = participants.get(0).path("player");
          if (player.isMissingNode() || player.path("gamerTag").isMissingNode()) continue;

          entrantNames.add(entrant.path("name").asText());
          playerTags.add(player.path("gamerTag").asText());
        }

        if (playerTags.size() < 2 || entrantNames.size() < 2) continue;

        // Character selections
        Map<String, Set<String>> charactersUsed = new HashMap<>();
        for (JsonNode game : setNode.path("games")) {
          for (JsonNode sel : game.path("selections")) {
            if (!"CHARACTER".equals(sel.path("selectionType").asText())) continue;

            String entrantName = sel.path("entrant").path("name").asText();
            String charId = sel.path("selectionValue").asText();

            charactersUsed.computeIfAbsent(entrantName, _ -> new HashSet<>()).add(charId);
          }
        }

        Set<String> player1Chars = charactersUsed.getOrDefault(entrantNames.get(0), Set.of());
        Set<String> player2Chars = charactersUsed.getOrDefault(entrantNames.get(1), Set.of());

        MatchInfo match = new MatchInfo(
            round,
            playerTags.get(0),
            playerTags.get(1),
            player1Chars,
            player2Chars,
            multiplePhases,
            entrants,
            isFirstPhase
        );

        matches.add(match);
      }

      page++;
    } while (page <= totalPages);

    return matches;
  }
}