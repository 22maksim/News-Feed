package my_home.news_feed.feign_client;

import my_home.news_feed.model.dto.PostDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;

@FeignClient(name = "my-post-service", url = "${post-service.host}:${post-service.port}")
public interface PostClientService {

    @GetMapping("api/v1/posts/feed-posts")
    HashMap<Long, Instant> getPosts();
}
