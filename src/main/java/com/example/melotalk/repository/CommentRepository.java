package com.example.melotalk.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.melotalk.entity.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {

	// 楽曲IDでルートコメント検索 → List<Comment>（複数件）
    List<Comment> findBySong_SongIdAndParentIsNull(String songId);
    
    // 親コメントIDで返信検索 → List<Comment>（複数件）
    Optional<Comment> findByParent_CommentId(Integer parentCommentId);
    
    // コメントIDで単一検索 → Optional<Comment>（0or1件）
    Optional<Comment> findByCommentId(Integer commentId);

	//List<Comment> findBySong_SpotifyTrackIdOrderByCreatedAtDesc(String spotifyTrackId);
}