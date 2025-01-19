package my_home.news_feed.service.post;

import my_home.news_feed.model.Post;

import java.util.concurrent.CompletableFuture;

public interface PostService {
    CompletableFuture<Void> eventCreatePost(Long userID, Post post);
}
