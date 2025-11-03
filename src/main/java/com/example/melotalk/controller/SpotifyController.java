package com.example.melotalk.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.melotalk.service.SpotifyAuthService;

@RestController
@RequestMapping("/spotify")
public class SpotifyController {

    private final SpotifyAuthService spotifyAuthService;

    public SpotifyController(SpotifyAuthService spotifyAuthService) {
        this.spotifyAuthService = spotifyAuthService;
    }

    // 曲検索APIの窓口
    @GetMapping("/songs-test")
    public ResponseEntity<String> searchSongs(@RequestParam String q) {
        String accessToken = spotifyAuthService.getAccessToken();

        // TODO: Spotify の検索APIにアクセスする処理を Service に作って呼ぶ
        // 例: spotifyAuthService.searchTracks(q, accessToken);

        return ResponseEntity.ok("検索キーワード: " + q + " （トークン: " + accessToken + "）");
    }
}
