package com.example.melotalk.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.melotalk.repository.SongRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SpotifySearchService {

	private static final String SEARCH_URL = "https://api.spotify.com/v1/search";

	@Autowired
	private SpotifyAuthService spotifyAuthService;
	
	@Autowired
	private SongRepository songRepository;

	private final RestTemplate restTemplate = new RestTemplate();
	private final ObjectMapper objectMapper = new ObjectMapper();

	public List<Map<String, String>> searchTracks(String keyword, int limit) {
		List<Map<String, String>> results = new ArrayList<>(); // 結果格納用

		try {
			// アクセストークンを取得
			String accessToken = spotifyAuthService.getAccessToken();

			// URL作成
			String url = UriComponentsBuilder.fromHttpUrl(SEARCH_URL)
					.queryParam("q", keyword)
					.queryParam("type", "track")
					.queryParam("limit", limit)
					.toUriString();

			HttpHeaders headers = new HttpHeaders();
			headers.setBearerAuth(accessToken);

			HttpEntity<Void> request = new HttpEntity<>(headers);

			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
			
			// ★ Spotifyから返ってきた生JSONを確認
			System.out.println("=== Spotify API Raw Response ===");
			System.out.println(response.getBody());
			System.out.println("================================");

			JsonNode root = objectMapper.readTree(response.getBody());
			JsonNode tracks = root.path("tracks").path("items");
			System.out.println(tracks);

			for (JsonNode track : tracks) {
				Map<String, String> trackInfo = new HashMap<>();
				trackInfo.put("songId", track.path("id").asText());
				trackInfo.put("name", track.path("name").asText());
				trackInfo.put("artist", track.path("artists").get(0).path("name").asText());
				trackInfo.put("artistId", track.path("artists").get(0).path("id").asText());
				trackInfo.put("album", track.path("album").path("name").asText());
				trackInfo.put("releaseDate", track.path("album").path("release_date").asText());

				results.add(trackInfo); // リストに追加
				
				
			}
			

		} catch (Exception e) {
			throw new RuntimeException("Spotify検索中にエラーが発生しました: " + e.getMessage(), e);
		}

		return results; // 最後に必ず返す
	}

	public void searchArtists(String keyword, int limit) {
		try {
			String accessToken = spotifyAuthService.getAccessToken();

			String url = UriComponentsBuilder.fromHttpUrl(SEARCH_URL)
					.queryParam("q", keyword)
					.queryParam("type", "artist")
					.queryParam("limit", limit)
					.toUriString();

			HttpHeaders headers = new HttpHeaders();
			headers.setBearerAuth(accessToken);

			HttpEntity<Void> request = new HttpEntity<>(headers);

			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

			JsonNode root = objectMapper.readTree(response.getBody());
			JsonNode artists = root.path("artists").path("items");

			for (JsonNode artist : artists) {
				String name = artist.path("name").asText();
				int followers = artist.path("followers").path("total").asInt();
				String genre = artist.path("genres").toString();
				System.out.println("アーティスト: " + name + " / フォロワー: " + followers + " / ジャンル: " + genre);
			}

		} catch (Exception e) {
			throw new RuntimeException("Spotifyアーティスト検索でエラー: " + e.getMessage(), e);
		}
	}

	public void searchAlbums(String keyword, int limit) {
		try {
			String accessToken = spotifyAuthService.getAccessToken();

			String url = UriComponentsBuilder.fromHttpUrl(SEARCH_URL)
					.queryParam("q", keyword)
					.queryParam("type", "album")
					.queryParam("limit", limit)
					.toUriString();

			HttpHeaders headers = new HttpHeaders();
			headers.setBearerAuth(accessToken);

			HttpEntity<Void> request = new HttpEntity<>(headers);

			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

			JsonNode root = objectMapper.readTree(response.getBody());
			JsonNode albums = root.path("albums").path("items");

			for (JsonNode album : albums) {
				String albumId = album.path("id").asText();
				String albumName = album.path("name").asText();
				String releaseDate = album.path("release_date").asText();
				String artistName = album.path("artists").get(0).path("name").asText();

				System.out.println("\nアルバム: " + albumName + " / アーティスト: " + artistName + " / 発売日: " + releaseDate);
				System.out.println("収録曲:");

				// アルバムに収録されている曲を取得
				fetchAlbumTracks(accessToken, albumId);
			}

		} catch (Exception e) {
			throw new RuntimeException("Spotifyアルバム検索でエラー: " + e.getMessage(), e);
		}
	}

	private void fetchAlbumTracks(String accessToken, String albumId) {
		String url = "https://api.spotify.com/v1/albums/" + albumId + "/tracks";

		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(accessToken);
		HttpEntity<Void> request = new HttpEntity<>(headers);

		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

		try {
			JsonNode root = objectMapper.readTree(response.getBody());
			JsonNode tracks = root.path("items");
			for (JsonNode track : tracks) {
				System.out.println(" - " + track.path("name").asText());
			}
		} catch (Exception e) {
			throw new RuntimeException("アルバムの曲取得でエラー: " + e.getMessage(), e);
		}
	}

	public Map<String, Object> searchArtistDetail(String keyword) {
		Map<String, Object> result = new HashMap<>();

		try {
			String accessToken = spotifyAuthService.getAccessToken();

			// 1. アーティスト検索
			String searchUrl = UriComponentsBuilder.fromHttpUrl(SEARCH_URL)
					.queryParam("q", "artist:" + keyword)
					.queryParam("type", "artist")
					.queryParam("limit", 1)
					.toUriString();

			HttpHeaders headers = new HttpHeaders();
			headers.setBearerAuth(accessToken);
			HttpEntity<Void> request = new HttpEntity<>(headers);

			ResponseEntity<String> searchResponse = restTemplate.exchange(searchUrl, HttpMethod.GET, request,
					String.class);

			JsonNode artistNode = objectMapper.readTree(searchResponse.getBody())
					.path("artists").path("items").get(0);

			String artistId = artistNode.path("id").asText();
			Map<String, Object> artistInfo = new HashMap<>();
			artistInfo.put("name", artistNode.path("name").asText());
			artistInfo.put("followers", artistNode.path("followers").path("total").asInt());
			artistInfo.put("genres", artistNode.path("genres"));
			artistInfo.put("image", artistNode.path("images").get(0).path("url").asText());
			artistInfo.put("id", artistNode.path("id").asText());

			result.put("artist", artistInfo);

			// 2. 人気曲取得
			String topTracksUrl = "https://api.spotify.com/v1/artists/" + artistId + "/top-tracks?market=JP";
			ResponseEntity<String> topTracksResponse = restTemplate.exchange(topTracksUrl, HttpMethod.GET, request,
					String.class);

			JsonNode tracksNode = objectMapper.readTree(topTracksResponse.getBody()).path("tracks");
			System.out.println(tracksNode);
			
			List<Map<String, String>> topTracks = new ArrayList<>();
			for (int i = 0; i < Math.min(5, tracksNode.size()); i++) {
				JsonNode track = tracksNode.get(i);
				Map<String, String> trackInfo = new HashMap<>();
				trackInfo.put("name", track.path("name").asText());
				trackInfo.put("songId", track.path("id").asText()); // Spotify トラック ID
				trackInfo.put("albumId", track.path("album").path("id").asText());
				trackInfo.put("previewUrl", track.path("preview_url").asText());
				trackInfo.put("album", track.path("album").path("name").asText());
				trackInfo.put("releaseDate", track.path("album").path("release_date").asText());
				trackInfo.put("image", track.path("album").path("images").get(0).path("url").asText());
				
				topTracks.add(trackInfo);
			}
			result.put("topTracks", topTracks);

			// 3. アルバム取得
			// 3. アルバム取得（ページング対応）
			List<Map<String, String>> albums = new ArrayList<>();
			int limit = 50;
			int offset = 0;

			while (true) {
				String albumsUrl = "https://api.spotify.com/v1/artists/" + artistId
						+ "/albums?include_groups=album&limit=" + limit + "&offset=" + offset;

				ResponseEntity<String> albumsResponse = restTemplate.exchange(albumsUrl, HttpMethod.GET, request,
						String.class);

				JsonNode albumsNode = objectMapper.readTree(albumsResponse.getBody()).path("items");

				if (albumsNode.isEmpty()) {
					break; // これ以上アルバムがない場合終了
				}

				for (JsonNode album : albumsNode) {
					Map<String, String> albumInfo = new HashMap<>();
					albumInfo.put("name", album.path("name").asText());
					albumInfo.put("id", album.path("id").asText());
					albumInfo.put("releaseDate", album.path("release_date").asText());
					albumInfo.put("image", album.path("images").get(0).path("url").asText());
					albums.add(albumInfo);
				}

				offset += limit; // 次のページを取得
			}

			result.put("albums", albums);

		} catch (Exception e) {
			throw new RuntimeException("Spotifyアーティスト詳細検索でエラー: " + e.getMessage(), e);
		}

		return result;
	}

	public Map<String, Object> getAlbumDetail(String albumId) {
		Map<String, Object> result = new HashMap<>();

		try {
			String accessToken = spotifyAuthService.getAccessToken();

			String url = "https://api.spotify.com/v1/albums/" + albumId;

			HttpHeaders headers = new HttpHeaders();
			headers.setBearerAuth(accessToken);
			HttpEntity<Void> request = new HttpEntity<>(headers);

			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

			JsonNode root = objectMapper.readTree(response.getBody());

			// アルバム情報
			Map<String, Object> albumInfo = new HashMap<>();
			albumInfo.put("id", root.path("id").asText());
			albumInfo.put("name", root.path("name").asText());
			albumInfo.put("releaseDate", root.path("release_date").asText());
			albumInfo.put("image", root.path("images").get(0).path("url").asText());
			albumInfo.put("artist", root.path("artists").get(0).path("name").asText());

			result.put("album", albumInfo);
			System.out.println(albumInfo);

			// トラック一覧
			List<Map<String, Object>> tracks = new ArrayList<>();
			for (JsonNode track : root.path("tracks").path("items")) {
				Map<String, Object> trackInfo = new HashMap<>();
				trackInfo.put("name", track.path("name").asText());
				trackInfo.put("artist", track.path("artists").get(0).path("name").asText());
				trackInfo.put("duration_ms", track.path("duration_ms").asInt());
				tracks.add(trackInfo);
			}
			result.put("tracks", tracks);

		} catch (Exception e) {
			throw new RuntimeException("Spotifyアルバム詳細取得でエラー: " + e.getMessage(), e);
		}

		return result;
	}
	
	public List<Map<String, String>> searchArtistsSimple(String keyword, int limit) {
	    List<Map<String, String>> results = new ArrayList<>();

	    try {
	        String accessToken = spotifyAuthService.getAccessToken();

	        String url = UriComponentsBuilder.fromHttpUrl(SEARCH_URL)
	                .queryParam("q", keyword)
	                .queryParam("type", "artist")
	                .queryParam("limit", limit)
	                .toUriString();

	        HttpHeaders headers = new HttpHeaders();
	        headers.setBearerAuth(accessToken);

	        HttpEntity<Void> request = new HttpEntity<>(headers);

	        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

	        JsonNode root = objectMapper.readTree(response.getBody());
	        JsonNode artists = root.path("artists").path("items");

	        for (JsonNode artist : artists) {
	            Map<String, String> artistInfo = new HashMap<>();
	            artistInfo.put("artistId", artist.path("id").asText());
	            artistInfo.put("artistName", artist.path("name").asText());
	            
	            // 画像URLを取得（存在する場合）
	            JsonNode images = artist.path("images");
	            if (images.size() > 0) {
	                artistInfo.put("imageUrl", images.get(0).path("url").asText());
	            } else {
	                artistInfo.put("imageUrl", ""); // デフォルト画像のURLなどを設定
	            }
	            
	            results.add(artistInfo);
	        }

	    } catch (Exception e) {
	        throw new RuntimeException("Spotifyアーティスト検索でエラー: " + e.getMessage(), e);
	    }

	    return results;
	}
	
	public Map<String, Object> getArtistDetailById(String artistId) {
	    String accessToken = spotifyAuthService.getAccessToken();
	    
	    try {
	        // 1. アーティスト基本情報を取得
	        String artistUrl = "https://api.spotify.com/v1/artists/" + artistId;
	        HttpHeaders headers = new HttpHeaders();
	        headers.setBearerAuth(accessToken);
	        HttpEntity<String> entity = new HttpEntity<>(headers);
	        
	        ResponseEntity<Map> artistResponse = restTemplate.exchange(
	            artistUrl,
	            HttpMethod.GET,
	            entity,
	            Map.class
	        );
	        
	        Map<String, Object> artistData = artistResponse.getBody();
	        
	        // アーティスト情報の整形
	        Map<String, Object> artist = new HashMap<>();
	        artist.put("id", artistData.get("id"));
	        artist.put("name", artistData.get("name"));
	        
	        Map<String, Object> followers = (Map<String, Object>) artistData.get("followers");
	        artist.put("followers", followers.get("total"));
	        artist.put("genres", artistData.get("genres"));
	        
	        List<Map<String, Object>> images = (List<Map<String, Object>>) artistData.get("images");
	        if (images != null && !images.isEmpty()) {
	            artist.put("image", images.get(0).get("url"));
	        } else {
	            artist.put("image", "");
	        }
	        
	        // 2. トップトラックを取得
	        String topTracksUrl = "https://api.spotify.com/v1/artists/" + artistId + "/top-tracks?market=JP";
	        ResponseEntity<Map> tracksResponse = restTemplate.exchange(
	            topTracksUrl,
	            HttpMethod.GET,
	            entity,
	            Map.class
	        );
	        
	        List<Map<String, Object>> tracks = (List<Map<String, Object>>) tracksResponse.getBody().get("tracks");
	        List<Map<String, Object>> topTracks = new ArrayList<>();
	        
	        for (Map<String, Object> track : tracks) {
	            Map<String, Object> trackInfo = new HashMap<>();
	            trackInfo.put("songId", track.get("id"));
	            trackInfo.put("name", track.get("name"));
	            
	            Map<String, Object> album = (Map<String, Object>) track.get("album");
	            trackInfo.put("album", album.get("name"));
	            trackInfo.put("albumId", album.get("id"));
	            trackInfo.put("releaseDate", album.get("release_date"));
	            
	            List<Map<String, Object>> albumImages = (List<Map<String, Object>>) album.get("images");
	            if (albumImages != null && !albumImages.isEmpty()) {
	                trackInfo.put("image", albumImages.get(0).get("url"));
	            } else {
	                trackInfo.put("image", "");
	            }
	            
	            topTracks.add(trackInfo);
	        }
	        
	        // 3. アルバムを取得
	        String albumsUrl = "https://api.spotify.com/v1/artists/" + artistId + "/albums?market=JP&limit=20&include_groups=album,single";
	        ResponseEntity<Map> albumsResponse = restTemplate.exchange(
	            albumsUrl,
	            HttpMethod.GET,
	            entity,
	            Map.class
	        );
	        
	        List<Map<String, Object>> albumItems = (List<Map<String, Object>>) albumsResponse.getBody().get("items");
	        List<Map<String, Object>> albums = new ArrayList<>();
	        
	        for (Map<String, Object> album : albumItems) {
	            Map<String, Object> albumInfo = new HashMap<>();
	            albumInfo.put("id", album.get("id"));
	            albumInfo.put("name", album.get("name"));
	            albumInfo.put("releaseDate", album.get("release_date"));
	            
	            List<Map<String, Object>> albumImages = (List<Map<String, Object>>) album.get("images");
	            if (albumImages != null && !albumImages.isEmpty()) {
	                albumInfo.put("image", albumImages.get(0).get("url"));
	            } else {
	                albumInfo.put("image", "");
	            }
	            
	            albums.add(albumInfo);
	        }
	        
	        // 4. 結果をまとめる
	        Map<String, Object> result = new HashMap<>();
	        result.put("artist", artist);
	        result.put("topTracks", topTracks);
	        result.put("albums", albums);
	        
	        return result;
	        
	    } catch (Exception e) {
	        System.err.println("アーティストID検索エラー: " + e.getMessage());
	        e.printStackTrace();
	        throw new RuntimeException("アーティスト情報の取得に失敗しました", e);
	    }
	}

}
