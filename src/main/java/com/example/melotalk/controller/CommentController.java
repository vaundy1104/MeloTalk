package com.example.melotalk.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.melotalk.dto.CommentRequest;
import com.example.melotalk.dto.CommentResponse;
import com.example.melotalk.entity.Album;
import com.example.melotalk.entity.Artist;
import com.example.melotalk.entity.Comment;
import com.example.melotalk.entity.Song;
import com.example.melotalk.entity.User;
import com.example.melotalk.repository.AlbumRepository;
import com.example.melotalk.repository.ArtistRepository;
import com.example.melotalk.repository.CommentRepository;
import com.example.melotalk.repository.SongRepository;
import com.example.melotalk.repository.UserRepository;
import com.example.melotalk.security.UserDetailsImpl;

@RestController
@RequestMapping("/api/comments")
public class CommentController {
	private final CommentRepository commentRepository;
	private final UserRepository userRepository;
	private final SongRepository songRepository;
	private final AlbumRepository albumRepository;
	private final ArtistRepository artistRepository;

	public CommentController(CommentRepository commentRepository, UserRepository userRepository,
			SongRepository songRepository,
			AlbumRepository albumRepository,
			ArtistRepository artistRepository) {
		this.commentRepository = commentRepository;
		this.userRepository = userRepository;
		this.songRepository = songRepository;
		this.albumRepository = albumRepository;
		this.artistRepository = artistRepository;
	}

	// コメント取得
	@GetMapping("/{songId}")
	public List<Comment> getComments(@PathVariable String songId) {
		return commentRepository.findBySong_SongIdAndParentIsNull(songId);
	}

	// コメント投稿
	@PostMapping
	public ResponseEntity<Comment> postComment(
			@RequestBody CommentRequest request,
			@AuthenticationPrincipal UserDetailsImpl userDetail) {

		if (userDetail == null) {
			return ResponseEntity.status(401).build(); // 未ログインなら401返す
		}

		// ログイン中のユーザーを取得
		User user = userDetail.getUser();

		// 曲ID必須チェック
		if (request.getSongId() == null || request.getSongId().isEmpty()) {
			throw new IllegalArgumentException("songId is required");
		}

		// 曲を取得 or 新規登録
		Song song = songRepository.findById(request.getSongId())
				.orElseGet(() -> {
					Song newSong = new Song();
					newSong.setSongId(request.getSongId());
					newSong.setSongTitle(request.getSongTitle());

					if (request.getArtistId() != null && !request.getArtistId().isEmpty()) {
						Artist artist = artistRepository.findById(request.getArtistId())
								.orElseGet(() -> {
									Artist newArtist = new Artist();
									newArtist.setArtistId(request.getArtistId());
									newArtist.setArtistName(request.getArtistName());
									try {
										return artistRepository.saveAndFlush(newArtist);
									} catch (Exception e) {
										// 他トランザクションが同時に登録していた場合は、再取得
										return artistRepository.findById(request.getArtistId()).orElseThrow();
									}
								});

						newSong.setArtistId(artist);
					}

					// アルバムは任意なのでnullでも問題なし
					if (request.getAlbumId() != null) {
						Album album = albumRepository.findById(request.getAlbumId()).orElse(null);
						newSong.setAlbumId(album);
					}

					newSong.setGenre(request.getGenre());
					newSong.setReleaseDate(request.getReleaseDate());
					newSong.setSpotifyUrl(request.getSpotifyUrl());
					newSong.setYoutubeUrl(request.getYoutubeUrl());

					return songRepository.save(newSong);
				});

		// コメント登録
		Comment comment = new Comment();
		comment.setUser(user); // ← request.getUserId() は使わない！
		comment.setSong(song);
		comment.setCommentText(request.getCommentText());

		Comment saved = commentRepository.save(comment);
		return ResponseEntity.ok(saved);
	}

	// 返信投稿
		@PostMapping("/reply/{parentId}")
		public ResponseEntity<CommentResponse> postReply(
	            @PathVariable Integer parentId,
	            @RequestBody CommentRequest request,
	            @AuthenticationPrincipal UserDetailsImpl userDetail) {
	        
	        if (userDetail == null) {
	            return ResponseEntity.status(401).build();
	        }
	        
	        User user = userDetail.getUser();
	        System.out.println("=== 返信投稿デバッグ ===");
	        System.out.println("親コメントID: " + parentId);
	        System.out.println("コメント本文: " + request.getCommentText());
	        
	        // 親コメントを取得
	        Optional<Comment> parentOptional = commentRepository.findById(parentId);
	        System.out.println("親コメント存在: " + parentOptional.isPresent());
	        
	        if (!parentOptional.isPresent()) {
	            // データベースに存在しないコメントへの返信
	            System.err.println("親コメントID " + parentId + " が見つかりません");
	            
	            // すべてのコメントIDをログ出力
	            List<Comment> allComments = commentRepository.findAll();
	            System.out.println("存在するコメントID一覧:");
	            allComments.forEach(c -> System.out.println("  - ID: " + c.getCommentId()));
	            
	            return ResponseEntity.status(404).body(null);
	        }
	        
	        Comment parentComment = parentOptional.get();
	        
	        // 返信コメントを作成
	        Comment reply = new Comment();
	        reply.setUser(user);
	        reply.setSong(parentComment.getSong());
	        reply.setParent(parentComment);
	        reply.setCommentText(request.getCommentText());
	        
	        
	        Comment saved = commentRepository.save(reply);
	        
	        // DTOに変換して返す
	        CommentResponse response = CommentResponse.from(saved);
	        
	        return ResponseEntity.ok(response);
	    }
		
		// コメント編集
	    @PutMapping("/{commentId}")
	    public ResponseEntity<CommentResponse> updateComment(
	            @PathVariable Integer commentId,
	            @RequestBody CommentRequest request,
	            @AuthenticationPrincipal UserDetailsImpl userDetail) {
	        
	        if (userDetail == null) {
	            return ResponseEntity.status(401).build();
	        }
	        
	        User user = userDetail.getUser();
	        
	        // コメントを取得
	        Optional<Comment> commentOptional = commentRepository.findByCommentId(commentId);
	        if (!commentOptional.isPresent()) {
	            return ResponseEntity.status(404).body(null);
	        }
	        
	        Comment comment = commentOptional.get();
	        
	        // 本人確認
	        if (!comment.getUser().getUserId().equals(user.getUserId())) {
	            return ResponseEntity.status(403).body(null); // 権限なし
	        }
	        
	        // コメント内容を更新
	        comment.setCommentText(request.getCommentText());
	        Comment updated = commentRepository.save(comment);
	        
	        CommentResponse response = CommentResponse.from(updated);
	        return ResponseEntity.ok(response);
	    }
	    
	    // コメント削除
	    @DeleteMapping("/{commentId}")
	    public ResponseEntity<Void> deleteComment(
	            @PathVariable Integer commentId,
	            @AuthenticationPrincipal UserDetailsImpl userDetail) {
	        
	        if (userDetail == null) {
	            return ResponseEntity.status(401).build();
	        }
	        
	        User user = userDetail.getUser();
	        
	        // コメントを取得
	        Optional<Comment> commentOptional = commentRepository.findByCommentId(commentId);
	        if (!commentOptional.isPresent()) {
	            return ResponseEntity.status(404).build();
	        }
	        
	        Comment comment = commentOptional.get();
	        
	        // 本人確認
	        if (!comment.getUser().getUserId().equals(user.getUserId())) {
	            return ResponseEntity.status(403).build(); // 権限なし
	        }
	        
	        // コメント削除
	        commentRepository.delete(comment);
	        return ResponseEntity.ok().build();
	    }
}
