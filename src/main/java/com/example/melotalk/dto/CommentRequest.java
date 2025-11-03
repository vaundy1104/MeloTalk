package com.example.melotalk.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentRequest {
    private Integer userId;       // ユーザーID
    private String songId;       // 曲ID
    private String songTitle;  
    private String artistId;
    private String artistName;
    private String albumId;
    private String genre;    
    private LocalDate releaseDate;
    private String spotifyUrl;
    private String youtubeUrl;
    private String commentText;
}


