package my_home.news_feed.service.kafka.listener;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my_home.news_feed.model.Post;
import my_home.news_feed.model.event.PostCreateEvent;
import my_home.news_feed.model.properties.SizeProperties;
import my_home.news_feed.service.post.PostService;
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
    private final PostService postServiceImpl;
    private final SizeProperties sizeProperties;

    @KafkaListener(topics = "${topic.kafka.post-create}", groupId = "post-group-id", concurrency = "3")
    public void onMessage(String message, Acknowledgment acknowledgment) {
        try {
            PostCreateEvent postCreateEvent = mapper.convertValue(message, PostCreateEvent.class);
            List<Long> userIds = mapper.readValue(postCreateEvent.getAuthorSubscriberIdsJson(), new TypeReference<>() {});
            Post post = getPost(postCreateEvent);
            post.setId(postCreateEvent.getId());

            List<CompletableFuture<Void>> futures = userIds.stream()
                            .map(event -> postServiceImpl.eventCreatePost(event, post))
                                    .toList();
            CompletableFuture<Void> allFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            allFuture.whenComplete((result, ex) -> {
                if (ex == null) {
                    acknowledgment.acknowledge();
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

    private Post getPost(PostCreateEvent postCreateEvent) {
        return Post.builder()
                .id(postCreateEvent.getId())
                .title(postCreateEvent.getTitle())
                .content(postCreateEvent.getContent())
                .authorId(postCreateEvent.getAuthorId())
                .createdAt(postCreateEvent.getCreatedAt())
                .ttl(Duration.ofHours(sizeProperties.ttl()).toMillis())
                .build();
    }
}
