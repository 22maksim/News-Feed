package my_home.news_feed.feign_client;

import my_home.news_feed.model.dto.UserAuthorDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;
import java.util.List;

@FeignClient(name = "user-service", url = "${user-service.host}:${user-service.port}")
public interface UserClientService {

    @GetMapping("api/v1/users/get/{userId}")
    HashMap<String, Long> getAuthor(@PathVariable Long userId);

    @GetMapping("api/v1/users/hash-map")
    HashMap<String, Long> getAuthors(@RequestBody List<Long> userIds); // получаем ключ это имя+фамилия через -
}
