package torquehub.torquehub.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PasswordEncoderConfig {

    @Bean PasswordEncoderConfig passwordEncoder() {
        return new PasswordEncoderConfig();
    }
}
