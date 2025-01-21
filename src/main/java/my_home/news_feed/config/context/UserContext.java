package my_home.news_feed.config.context;

import org.springframework.stereotype.Component;

@Component
public class UserContext {
    ThreadLocal<Long> userIdHolder = new ThreadLocal<>();

    public void setUserId(Long userId) {
        userIdHolder.set(userId);
    }

    public Long getUserId() {
        Long userId = userIdHolder.get();
        if (userId == null) {
            throw new IllegalArgumentException("User ID is missing. Please make sure 'x-user-id' header is included in the request.");
        }
        return userId;
    }

    public void clear() {
        userIdHolder.remove();
    }
}
