package toonpick.app.auth.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import toonpick.app.auth.jwt.JwtTokenProvider;
import toonpick.app.auth.service.TokenService;
import toonpick.app.service.MemberService;

import java.util.stream.Collectors;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;
    private final MemberService memberService;

    private static final Logger logger = LoggerFactory.getLogger(LoginSuccessHandler.class);

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        String username = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining());

        Long userId = memberService.getUserIdByUsername(username);
        String accessToken = jwtTokenProvider.createAccessToken(userId, username, role);
        String refreshToken = jwtTokenProvider.createRefreshToken(userId, username, role);

        tokenService.saveRefreshToken(username, refreshToken);
        response.setHeader("Authorization", "Bearer " + accessToken);
        response.addCookie(jwtTokenProvider.createCookie("refresh", refreshToken));
        response.setStatus(HttpStatus.OK.value());

        logger.info("USER LOGIN SUCCESS (username-{})", username);
    }
}
