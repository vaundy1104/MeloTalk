package com.example.melotalk.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.melotalk.entity.Comment;
import com.example.melotalk.repository.CommentRepository;
import com.example.melotalk.repository.SongRepository;
import com.example.melotalk.security.UserDetailsImpl;

@Controller
public class SongController {

	private final CommentRepository commentRepository;
	private final SongRepository songRepository;

	public SongController(CommentRepository commentRepository, SongRepository songRepositor) {
		this.commentRepository = commentRepository;
		this.songRepository = songRepositor;
	}

	@GetMapping("/song")
	public String showSongPage(Model model,
			@RequestParam String songId,
		    @RequestParam(required = false) String title,
		    @RequestParam(required = false) String artist,
		    @RequestParam(required = false) String artistId, // 追加
		    @AuthenticationPrincipal UserDetailsImpl userDetail) {
		
		String userId = null;
	    if (userDetail != null) {
	        userId = String.valueOf(userDetail.getUser().getUserId()); // または getUserName() など
	    }


		List<Comment> comments = commentRepository.findBySong_SongIdAndParentIsNull(songId);

		System.out.println(artist);
		model.addAttribute("songId", songId);
		model.addAttribute("userId", userId);
		model.addAttribute("songTitle", title);
		model.addAttribute("artistName", artist);
		model.addAttribute("artistId", artistId);
		model.addAttribute("comments", comments);
		System.out.println("songId = " + songId);
		System.out.println("userId = " + userId);
		System.out.println("artistId = " + artistId);
		System.out.println("artistName = " + artist);

		return "song";
	}
}
