package torquehub.torquehub.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import torquehub.torquehub.configuration.utils.VoteRateLimiterInterceptor;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final VoteRateLimiterInterceptor voteRateLimiterInterceptor;

    public WebMvcConfig(VoteRateLimiterInterceptor voteRateLimiterInterceptor) {
        this.voteRateLimiterInterceptor = voteRateLimiterInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Adjust this pattern to cover all your vote-related paths
        registry.addInterceptor(voteRateLimiterInterceptor).addPathPatterns(
                "/questions/*/upvote", "/questions/*/downvote",
                "/answers/*/upvote", "/answers/*/downvote",
                "/bookmarks/*", "/bookmarks/answer",
                "/follows/questions/*", "/follows/answers/*");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true);

    }
}