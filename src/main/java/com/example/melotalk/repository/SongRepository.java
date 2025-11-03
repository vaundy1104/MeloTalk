package com.example.melotalk.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.melotalk.entity.Song;

@Repository
public interface SongRepository extends JpaRepository<Song, String> {
	List<Song> findTop5BySongTitleContainingIgnoreCase(String songTitle);
}
