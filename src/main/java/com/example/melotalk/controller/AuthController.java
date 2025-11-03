package com.example.melotalk.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import com.example.melotalk.service.SpotifyAuthService;

import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {
	
	@Autowired
    private SpotifyAuthService spotifyAuthService;

    @Value("${spotify.client.id}")
    private String clientId;
    
    @Value("${spotify.client.secret}")
    private String clientSecret;
    
    @Value("${spotify.redirect.uri}")
    private String redirectUri;

    @GetMapping("/login")
    public String login() {
        return "login"; // login.htmlを表示
    }
    
    // Spotify OAuth用は別のエンドポイントにする
    @GetMapping("/spotify/auth")
    public RedirectView spotifyAuth(HttpSession session) {
        String state = UUID.randomUUID().toString();
        session.setAttribute("spotify_state", state);
        
        String scope = "user-read-private user-read-email";
        
        StringBuilder authUrl = new StringBuilder("https://accounts.spotify.com/authorize");
        authUrl.append("?response_type=code");
        authUrl.append("&client_id=").append(clientId);
        authUrl.append("&scope=").append(scope.replace(" ", "%20"));
        authUrl.append("&redirect_uri=").append(redirectUri);
        authUrl.append("&state=").append(state);
        
        return new RedirectView(authUrl.toString());
    }
    
    

    @GetMapping("/callback")
    public String callback(@RequestParam String code, HttpSession session) {
        String redirectUri = "http://127.0.0.1:8080/callback";
        String accessToken = spotifyAuthService.exchangeCodeForToken(code, redirectUri);
        // セッションにトークンを保存するなどの処理
        return "redirect:/";
    }
}
