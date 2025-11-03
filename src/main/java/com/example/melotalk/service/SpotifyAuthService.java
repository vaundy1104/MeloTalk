package com.example.melotalk.service;

import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;



@Service
public class SpotifyAuthService {

private static final String TOKEN_URL = "https://accounts.spotify.com/api/token";
    
    @Value("${spotify.client.id:ab9fa145e08d474db4ca6ed7d0b78b91}")
    private String clientId;
    
    @Value("${spotify.client.secret:43bcdceddff94c5bab72cf1810829d87}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Client Credentials フロー（アプリケーション認証）
     * 検索などの公開APIにアクセスするため
     */
    public String getAccessToken() {
        String authHeader = "Basic " + Base64.getEncoder()
                .encodeToString((clientId + ":" + clientSecret).getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", authHeader);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(TOKEN_URL, request, String.class);

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            return root.get("access_token").asText();
        } catch (Exception e) {
            throw new RuntimeException("アクセストークンの解析に失敗しました: " + e.getMessage(), e);
        }
    }

    /**
     * Authorization Code フロー（ユーザー認証）
     * ユーザーの認証コードをアクセストークンに交換
     */
    public String exchangeCodeForToken(String code, String redirectUri) {
        String authHeader = "Basic " + Base64.getEncoder()
                .encodeToString((clientId + ":" + clientSecret).getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", authHeader);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(TOKEN_URL, request, String.class);

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            return root.get("access_token").asText();
        } catch (Exception e) {
            throw new RuntimeException("トークン交換に失敗しました: " + e.getMessage(), e);
        }
    }

    /**
     * 認証URL生成用のヘルパーメソッド
     */
    public String buildAuthorizationUrl(String redirectUri, String state, String scope) {
        StringBuilder authUrl = new StringBuilder("https://accounts.spotify.com/authorize");
        authUrl.append("?response_type=code");
        authUrl.append("&client_id=").append(clientId);
        authUrl.append("&scope=").append(scope.replace(" ", "%20"));
        authUrl.append("&redirect_uri=").append(redirectUri);
        authUrl.append("&state=").append(state);
        
        return authUrl.toString();
    }
}
