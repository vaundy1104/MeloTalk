package com.example.melotalk.entity;


import java.sql.Timestamp;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "songs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Song {

    @Id
    @Column(name = "song_id", nullable = false, length = 200)
    private String songId;  // DB 自動生成ID

//    @Column(name = "external_song_id", nullable = false, unique = true)
//    private String externalSongId; // SpotifyやYouTubeのID

    @Column(name = "song_title", nullable = false, length = 200)
    private String songTitle;

    @ManyToOne
    @JoinColumn(name = "artist_id", nullable = false)
    private Artist artistId; // Artistエンティティ

    @ManyToOne
    @JoinColumn(name = "album_id")
    private Album albumId;   // Albumエンティティ（任意）

    @Column(length = 50)
    private String genre;

    @Column(name = "release_date")
    private LocalDate releaseDate; // LocalDate に変更

    @Column(columnDefinition = "TEXT")
    private String lyrics;

    @Column(name = "spotify_url", length = 500)
    private String spotifyUrl;

    @Column(name = "youtube_url", length = 500)
    private String youtubeUrl;

    @Column(name = "created_at", updatable = false, insertable = false)
    private Timestamp createdAt;
}