package my_home.news_feed.service.kafka.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my_home.news_feed.model.event.PostLikeEvent;
import my_home.news_feed.service.post.PostEventsService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaLikePostConsumer {
    private final ObjectMapper mapper;
    private final PostEventsService postEventsServiceImpl;

    @KafkaListener(topics = {"${topic.kafka.likes}"}, concurrency = "3", groupId = "likes-id")
    public void listen(String message, Acknowledgment ack) {
        if (message == null) {
            log.error("Message is null");
            ack.acknowledge();
            return;
        }
        PostLikeEvent event = null;
        try {
            event = mapper.convertValue(message, PostLikeEvent.class);
        } catch (IllegalArgumentException e) {
            log.error("Invalid message {}", message);
        }
        assert event != null;

        CompletableFuture<Void> future = postEventsServiceImpl.eventLikePost(event);
        future.whenComplete((r, e) -> {
            if (e != null) {
                log.error("Error while posting event like", e);
            } else {
                ack.acknowledge();
            }
        });
    }
}
