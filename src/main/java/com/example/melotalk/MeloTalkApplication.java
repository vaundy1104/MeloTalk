package com.example.melotalk;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.example.melotalk.service.SpotifySearchService;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.melotalk.repository")
@EntityScan(basePackages = "com.example.melotalk.entity")

public class MeloTalkApplication implements CommandLineRunner {
	
	@Autowired
	private SpotifySearchService spotifySearchService;

	public static void main(String[] args) {
		SpringApplication.run(MeloTalkApplication.class, args);
	}
	
	@Override
    public void run(String... args) throws Exception {
        // 曲検索（例: YOASOBI を 5 件）
//        spotifySearchService.searchTracks("YOASOBI", 5);
//        spotifySearchService.searchAlbums("THE BOOK", 3);
    }

}
