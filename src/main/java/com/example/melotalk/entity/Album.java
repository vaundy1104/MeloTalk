package com.example.melotalk.entity;

import java.sql.Date;
import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "albums")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Album {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "album_id")
	private String albumId;
	
	@Column(nullable = false, unique = true, name = "album_title")
	private String albumTitle;
	
	@ManyToOne
	@JoinColumn(name = "artist_id")
	private Artist artistId;
	
	@Column(name = "release_date")
	private Date releaseDate;
	
	@Column(name = "album_cover_url", length = 500)
	private String albumCoverUrl;
	
	@Column(name = "created_at")
	private Timestamp createdAt;
}
