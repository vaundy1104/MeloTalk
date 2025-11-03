package com.example.melotalk.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchSuggestionResponse {
    
    private List<SongSuggestion> songs = new ArrayList<>();
    private List<ArtistSuggestion> artists = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SongSuggestion {
        private String songId;
        private String songTitle;
        private String artistName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ArtistSuggestion {
        private String artistId;
        private String artistName;
    }
}
