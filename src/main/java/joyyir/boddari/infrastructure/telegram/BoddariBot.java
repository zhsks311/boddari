package joyyir.boddari.infrastructure.telegram;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Repository
public class BoddariBot {
    private final RestTemplate restTemplate;
    private final String token;
    private final String chatId;

    public BoddariBot(RestTemplate restTemplate,
                      @Value("${constant.telegram-boddaribot.access-token}") String token,
                      @Value("${constant.telegram-boddaribot.chat-id}") String chatId) {
        this.restTemplate = restTemplate;
        this.token = token;
        this.chatId = chatId;
    }

    public void send(String message) {
        HttpEntity<?> entity = new HttpEntity<>(Map.of("chat_id", chatId, "text", message));
        restTemplate.exchange("https://api.telegram.org/bot" + token + "/sendMessage", HttpMethod.POST, entity, String.class);
    }
}
