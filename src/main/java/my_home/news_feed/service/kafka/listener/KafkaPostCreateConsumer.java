package my_home.news_feed.service.kafka.listener;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my_home.news_feed.mapper.PostMapper;
import my_home.news_feed.model.Post;
import my_home.news_feed.model.event.PostCreateEvent;
import my_home.news_feed.model.properties.SizeProperties;
import my_home.news_feed.service.post.PostEventsService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaPostCreateConsumer {
    private final ObjectMapper mapper;
    private final PostEventsService postEventsServiceImpl;
    private final SizeProperties sizeProperties;
    private final PostMapper postMapper;

    @KafkaListener(topics = "${topic.kafka.post-create}", groupId = "post-group-id", concurrency = "3")
    public void onMessage(String message, Acknowledgment ack) {
        if (message == null) {
            log.error("Message is null");
            ack.acknowledge();
            return;
        }
        try {
            PostCreateEvent postCreateEvent = mapper.convertValue(message, PostCreateEvent.class);
            List<Long> userIds = mapper.readValue(postCreateEvent.getAuthorSubscriberIdsJson(), new TypeReference<>() {});
            Post post = postMapper.toEntity(postCreateEvent);
            post.setTtl(Duration.ofHours(sizeProperties.ttl()).toMillis());

            List<CompletableFuture<Void>> futures = userIds.stream()
                            .map(event -> postEventsServiceImpl.eventCreatePost(event, post))
                                    .toList();
            CompletableFuture<Void> allFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            allFuture.whenComplete((result, ex) -> {
                if (ex == null) {
                    ack.acknowledge();
                    log.info("Received post create event: Id: {}. Title: {}", postCreateEvent.getId(), postCreateEvent.getTitle());
                }
                else {
                    log.error("Error occurred while processing event: {}", postCreateEvent.getId(), ex);
                }
            }).join();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
