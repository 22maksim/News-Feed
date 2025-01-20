package my_home.news_feed.service.kafka.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my_home.news_feed.model.Comment;
import my_home.news_feed.model.event.PostCommentEvent;
import my_home.news_feed.service.post.PostService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaCommentPostConsumer {
    private final ObjectMapper mapper;
    private final PostService postServiceImpl;

    @KafkaListener(topics = {"${topic.kafka.comments}"}, concurrency = "3", groupId = "comments")
    public void onMessage(String message, Acknowledgment ack) {
        PostCommentEvent event = mapper.convertValue(message, PostCommentEvent.class);
        Comment comment = getComment(event);

        CompletableFuture<Void> future = postServiceImpl.eventCommentForPost(comment);
        future.whenComplete((r, e) -> {
            if (e != null) {
                log.error(e.getMessage(), e);
            } else {
                ack.acknowledge();
            }
        });
    }

    private Comment getComment(PostCommentEvent event) {
        return Comment.builder()
                .postId(event.getPostId())
                .author_id(event.getAuthor_id())
                .comment(event.getComment())
                .createdAt(event.getCreatedAt())
                .build();
    }
}
