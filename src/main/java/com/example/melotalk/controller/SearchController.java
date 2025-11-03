package com.example.melotalk.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.melotalk.service.SpotifySearchService;

@Controller
public class SearchController {

	private final SpotifySearchService spotifySearchService;

	public SearchController(SpotifySearchService spotifySearchService) {
		this.spotifySearchService = spotifySearchService;
	}

	// 検索画面表示
	@GetMapping("/search")
	public String search() {
		return "search"; // search.htmlを返す
	}

	// SearchController.java の getSearchSuggestions メソッドを改善

	@GetMapping("/api/search/suggestions")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> getSearchSuggestions(
			@RequestParam String query) {

		if (query == null || query.trim().length() < 2) {
			return ResponseEntity.ok(createEmptyResponse());
		}

		try {
			// 並列で楽曲とアーティストを検索
			List<Map<String, String>> tracks = spotifySearchService.searchTracks(query, 5);
			List<Map<String, String>> artistsRaw = spotifySearchService.searchArtistsSimple(query, 5);

			Map<String, Object> response = new HashMap<>();

			// 楽曲リスト
			List<Map<String, String>> songs = new ArrayList<>();
			for (Map<String, String> track : tracks) {
				Map<String, String> song = new HashMap<>();
				song.put("songId", track.get("songId"));
				song.put("songTitle", track.get("name"));
				song.put("artistName", track.get("artist"));
				songs.add(song);
			}
			response.put("songs", songs);

			// アーティストリスト
			response.put("artists", artistsRaw);

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			System.err.println("検索候補取得エラー: " + e.getMessage());
			return ResponseEntity.ok(createEmptyResponse());
		}
	}

	// 楽曲検索API
	@GetMapping("/api/spotify/search-tracks")
	@ResponseBody
	public ResponseEntity<List<Map<String, String>>> searchTracks(
			@RequestParam String keyword,
			@RequestParam(defaultValue = "10") int limit) {

		try {
			List<Map<String, String>> results = spotifySearchService.searchTracks(keyword, limit);
			return ResponseEntity.ok(results);
		} catch (Exception e) {
			return ResponseEntity.badRequest().build();
		}
	}

	// アーティスト詳細検索API
	@GetMapping("/api/spotify/artist-detail")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> searchArtistDetail(@RequestParam String keyword) {
		try {
			Map<String, Object> result = spotifySearchService.searchArtistDetail(keyword);
			return ResponseEntity.ok(result);
		} catch (Exception e) {
			return ResponseEntity.badRequest().build();
		}
	}
	
	@GetMapping("/api/spotify/artist-detail-by-id")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> getArtistDetailById(@RequestParam String artistId) {
	    try {
	        Map<String, Object> result = spotifySearchService.getArtistDetailById(artistId);
	        return ResponseEntity.ok(result);
	    } catch (Exception e) {
	        System.err.println("アーティストID検索エラー: " + e.getMessage());
	        return ResponseEntity.badRequest().build();
	    }
	}

	// アルバム詳細取得API
	@GetMapping("/api/spotify/album-detail")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> getAlbumDetail(@RequestParam String albumId) {
		try {
			Map<String, Object> result = spotifySearchService.getAlbumDetail(albumId);
			return ResponseEntity.ok(result);
		} catch (Exception e) {
			return ResponseEntity.badRequest().build();
		}
	}
	
	// 空のレスポンスを作成
    private Map<String, Object> createEmptyResponse() {
        Map<String, Object> response = new HashMap<>();
        response.put("songs", new ArrayList<>());
        response.put("artists", new ArrayList<>());
        return response;
    }

}
