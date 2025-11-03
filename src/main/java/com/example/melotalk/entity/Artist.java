package com.example.melotalk.entity;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "artists")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Artist {

	@Id
	@Column(name = "artist_id")
	private String artistId;
	
	@Column(name = "artist_name")
	private String artistName;
	
	@Column(name = "created_at")
	private Timestamp createdAt;
}
