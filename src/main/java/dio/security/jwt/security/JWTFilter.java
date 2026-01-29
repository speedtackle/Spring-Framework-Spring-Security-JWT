package dio.security.jwt.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class JWTFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        try {
            if (header != null && header.startsWith(JWTProperties.PREFIX + " ")) {

                JWTObject tokenObject = JWTCreator.parse(
                        header,
                        JWTProperties.PREFIX,
                        JWTProperties.KEY
                );

                List<SimpleGrantedAuthority> authorities =
                        tokenObject.getRoles()
                                .stream()
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList());

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                tokenObject.getSubject(),
                                null,
                                authorities
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
        } catch (MalformedJwtException | UnsupportedJwtException | SignatureException e) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
        }
    }
}
