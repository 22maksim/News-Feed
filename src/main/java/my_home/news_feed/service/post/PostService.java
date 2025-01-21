package my_home.news_feed.service.post;

import my_home.news_feed.model.Comment;
import my_home.news_feed.model.Post;
import my_home.news_feed.model.event.PostLikeEvent;

import java.util.concurrent.CompletableFuture;

public interface PostService {
    CompletableFuture<Void> eventCreatePost(Long userID, Post post);

    CompletableFuture<Void> eventCommentForPost(Comment comment);

    CompletableFuture<Void> eventLikePost(PostLikeEvent event);
}
