package com.example.melotalk.main;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.melotalk.service.SpotifyAuthService;

@SpringBootApplication
public class SpotifyApp implements CommandLineRunner {

    @Autowired
    private SpotifyAuthService spotifyAuthService;

    public static void main(String[] args) {
        SpringApplication.run(SpotifyApp.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // アクセストークンを取得
        String response = spotifyAuthService.getAccessToken();
        System.out.println("Spotify Access Token Response: " + response);
    }
}
