package com.example.melotalk.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.melotalk.entity.Comment;
import com.example.melotalk.repository.CommentRepository;
import com.example.melotalk.repository.SongRepository;

@Service
public class CommentService {
	
	private final CommentRepository commentRepository;
	private final SongRepository songRepository;
	
	public CommentService(CommentRepository commentRepository, SongRepository songRepository) {
		this.commentRepository = commentRepository;
		this.songRepository = songRepository;
	}
	
	// 親コメントのみ取得
	public List<Comment> findBySongIdAndParentIsNull(String songId) {
		return commentRepository.findBySong_SongIdAndParentIsNull(songId);
	}

	// 子コメントを取得したい場合は別メソッド
	public Optional<Comment> findByParentId(Integer parentId) {
		return commentRepository.findById(parentId); 
	}
	
//	// 曲のコメントを作成日時順に取得
//	public List<Comment> findBySongSpotifyTrackId(String spotifyTrackId) {
//		return commentRepository.findBySong_SpotifyTrackIdOrderByCreatedAtDesc(spotifyTrackId);
//	}
}
