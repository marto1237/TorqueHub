package torquehub.torquehub.configuration.jwt.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import torquehub.torquehub.configuration.jwt.token.AccessToken;
import torquehub.torquehub.configuration.jwt.token.AccessTokenDecoder;
import torquehub.torquehub.configuration.jwt.token.exeption.InvalidAccessTokenException;
import torquehub.torquehub.configuration.jwt.token.impl.BlacklistService;

import java.io.IOException;
import java.util.List;

@Component
public class AuthenticationRequestFilter extends OncePerRequestFilter {


    private final AccessTokenDecoder accessTokenDecoder;
    private final BlacklistService blacklistService;

    public AuthenticationRequestFilter(AccessTokenDecoder accessTokenDecoder,
                                       BlacklistService blacklistService) {
        this.accessTokenDecoder = accessTokenDecoder;
        this.blacklistService = blacklistService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String requestTokenHeader = request.getHeader("Authorization");
        if (requestTokenHeader == null || !requestTokenHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessTokenString = requestTokenHeader.substring(7);

        if (blacklistService.isTokenBlacklisted(accessTokenString)) {
            sendAuthenticationError(response);
            return;
        }

        try {
            AccessToken accessToken = accessTokenDecoder.decode(accessTokenString);

            if (accessToken == null) {
                logger.warn("Access token is invalid or could not be decoded.");
                sendAuthenticationError(response);
                return;
            }
            setupSpringSecurityContext(accessToken);
            filterChain.doFilter(request, response);
        } catch (InvalidAccessTokenException e) {
            logger.error("Error validating access token", e);
            sendAuthenticationError(response);
        }
    }

    private void sendAuthenticationError(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.flushBuffer();
    }

    private void setupSpringSecurityContext(AccessToken accessToken) {

        if (accessToken == null) {
            throw new IllegalArgumentException("Access token cannot be null when setting up security context");
        }
        UserDetails userDetails = new User(accessToken.getUsername(), "",
                List.of(new SimpleGrantedAuthority(accessToken.getRole())));

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
}
