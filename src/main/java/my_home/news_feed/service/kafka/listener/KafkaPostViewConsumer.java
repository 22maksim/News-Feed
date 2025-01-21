package my_home.news_feed.service.kafka.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my_home.news_feed.model.event.PostLikeEvent;
import my_home.news_feed.model.event.PostViewsEvent;
import my_home.news_feed.service.post.PostService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaPostViewConsumer {
    private final ObjectMapper mapper;
    private final PostService postServiceImpl;

    @KafkaListener(topics = {"${topic.kafka.post-views}"}, concurrency = "3", groupId = "post-views-group")
    public void listen(String message, Acknowledgment ack) {
        if (message == null) {
            log.error("Message is null or empty");
            ack.acknowledge();
            return;
        }
        try {
            PostViewsEvent event = mapper.convertValue(message, PostViewsEvent.class);

            CompletableFuture<Void> future = postServiceImpl.eventViewPost(event);

            future.whenComplete((v, e) -> {
                if (e != null) {
                    log.error("Error occurred while processing event", e);
                } else {
                    ack.acknowledge();
                    log.info("Successfully processed event");
                }
            });
        } catch (IllegalArgumentException ex) {
            log.error("Json is uncorrected", ex);
        }
    }
}
