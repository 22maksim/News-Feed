package my_home.news_feed.service.kafka.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my_home.news_feed.mapper.CommentMapper;
import my_home.news_feed.model.Comment;
import my_home.news_feed.model.event.PostCommentEvent;
import my_home.news_feed.service.post.PostEventsService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaCommentPostConsumer {
    private final ObjectMapper mapper;
    private final PostEventsService postEventsServiceImpl;
    private final CommentMapper commentMapper;

    @KafkaListener(topics = {"${topic.kafka.comments}"}, concurrency = "3", groupId = "comments")
    public void onMessage(String message, Acknowledgment ack) {
        if (message == null) {
            log.error("Message is null");
            ack.acknowledge();
            return;
        }

        Comment comment = commentMapper.toComment(getEvent(message));

        try {
            CompletableFuture<Void> future = postEventsServiceImpl.eventCommentForPost(comment);
            future.whenComplete((r, e) -> {
                if (e != null) {
                    log.error(e.getMessage(), e);
                } else {
                    ack.acknowledge();
                }
            });
        } catch (JsonProcessingException e) {
            log.error("Comment don't save, json error, Message kafka: {}", message, e);
        }
    }

    private PostCommentEvent getEvent(String message) {
        PostCommentEvent event = null;
        try {
            event = mapper.convertValue(message, PostCommentEvent.class);
        } catch (IllegalArgumentException e) {
            log.error("Invalid event-comment. kafka message {}", message);
        }
        if (event == null) {
            log.error("Event comment is null");
        }
        return event;
    }
}
