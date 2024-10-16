package site.gnu_gongji.GnuGongji.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class NotificationServiceTest {

    @Test
    public void httpClientErrorParsingTest() throws JsonProcessingException {
        // 가정된 FCM 오류 응답
        String errorResponse = "{" +
                "\"error\": {" +
                "\"code\": 404," +
                "\"message\": \"Requested entity was not found.\"," +
                "\"status\": \"NOT_FOUND\"," +
                "\"details\": [" +
                "{" +
                "\"@type\": \"type.googleapis.com/google.firebase.fcm.v1.FcmError\"," +
                "\"errorCode\": \"UNREGISTERED\"" +
                "}" +
                "]" +
                "}" +
                "}";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(errorResponse);
        JsonNode errorNode = rootNode.path("error");

        int statusCode = errorNode.path("code").asInt();
        String errorMessage = errorNode.path("message").asText();
        String errorCode = errorNode.path("details")
                .get(0)
                .path("errorCode")
                .asText();

        Assertions.assertThat(statusCode).isEqualTo(404);
        Assertions.assertThat(errorMessage).isEqualTo("Requested entity was not found.");
        Assertions.assertThat(errorCode).isEqualTo("UNREGISTERED");
    }
}