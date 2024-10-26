package torquehub.torquehub.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.StaticHeadersWriter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())  // Disable CSRF protection for testing

                .headers(headers -> headers
                        .addHeaderWriter(new StaticHeadersWriter("X-Content-Type-Options", "nosniff"))  // Prevent MIME type sniffing
                        .addHeaderWriter(new StaticHeadersWriter("Strict-Transport-Security", "max-age=31536000; includeSubDomains"))  // Enable HSTS for 1 year
                )
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/admin/**").hasAuthority("ADMIN") // Protect specific endpoints by role
                        .requestMatchers("/moderator/**").hasAuthority("MODERATOR")
                        .requestMatchers("/organizer/**").hasAuthority("ORGANIZER")
                        .requestMatchers("/user/**").hasAuthority("USER")
                        .anyRequest().permitAll()  // Allow unrestricted access to all endpoints  // Require authentication for any other requests
                );
        return http.build();
    }
}
