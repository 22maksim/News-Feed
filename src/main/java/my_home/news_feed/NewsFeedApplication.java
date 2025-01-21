package my_home.news_feed;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@EnableConfigurationProperties
@ConfigurationPropertiesScan
@SpringBootApplication
@EnableFeignClients(basePackages = "my_home.news_feed.feign_client")
public class NewsFeedApplication {

	public static void main(String[] args) {
		SpringApplication.run(NewsFeedApplication.class, args);
	}

}
