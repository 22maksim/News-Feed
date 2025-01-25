package my_home.news_feed.service.post;

import com.fasterxml.jackson.core.JsonProcessingException;
import my_home.news_feed.model.Comment;
import my_home.news_feed.model.Post;
import my_home.news_feed.model.event.PostLikeEvent;
import my_home.news_feed.model.event.PostViewsEvent;

import java.util.concurrent.CompletableFuture;

public interface PostEventsService {
    CompletableFuture<Void> eventCreatePost(Long userID, Post post);

    CompletableFuture<Void> eventCommentForPost(Comment comment) throws JsonProcessingException;

    CompletableFuture<Void> eventLikePost(PostLikeEvent event);

    CompletableFuture<Void> eventViewPost(PostViewsEvent event);
}
