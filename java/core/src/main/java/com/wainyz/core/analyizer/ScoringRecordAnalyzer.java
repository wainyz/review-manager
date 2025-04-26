package com.wainyz.core.analyizer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.wainyz.core.CoreApplication;
import com.wainyz.core.pojo.domin.ScoringResponse;

/**
 * @author Yanion_gwgzh
 */
public class ScoringRecordAnalyzer {
    public static final ObjectMapper OBJECT_MAPPER = CoreApplication.OBJECT_MAPPER;
    public static ScoringResponse jsonNodeToScoringResponse(JsonNode node) {
        ScoringResponse scoringResponse = new ScoringResponse();
        scoringResponse.setAdvice(node.get("advice").textValue());
        scoringResponse.setSimilar(node.get("similar").textValue());
        ArrayNode arrayNode = (ArrayNode) node.get("scores");
        int index = 0;
        int[] scoringRecords = new int[arrayNode.size()];
        for (JsonNode jsonNode: arrayNode){
            scoringRecords[index++] = jsonNode.intValue();
        }
        scoringResponse.setScores(scoringRecords);
        return scoringResponse;
    }
    public static ScoringResponse jsonNodeToScoringResponse(String jsonString) throws JsonProcessingException {
        JsonNode node = OBJECT_MAPPER.readTree(jsonString);
        return jsonNodeToScoringResponse(node);
    }
}
