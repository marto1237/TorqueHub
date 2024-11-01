package torquehub.torquehub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class TorqueHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(TorqueHubApplication.class, args);
    }

}
