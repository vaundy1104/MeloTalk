package com.example.melotalk.dto;

import java.time.LocalDateTime;

import com.example.melotalk.entity.Comment;

import lombok.Data;

@Data
public class CommentResponse {
    private Integer commentId;
    private String commentText;
    private UserInfo user;
    private LocalDateTime createdAt;
    
    @Data
    public static class UserInfo {
        private Integer userId;
        private String userName;
    }
    
    public static CommentResponse from(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setCommentId(comment.getCommentId());
        response.setCommentText(comment.getCommentText());
        response.setCreatedAt(comment.getCreatedAt());
        
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(comment.getUser().getUserId());
        userInfo.setUserName(comment.getUser().getUserName());
        response.setUser(userInfo);
        
        return response;
    }
}
