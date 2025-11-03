package com.example.melotalk.form;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommentEditForm {

	@NotBlank(message = "コメントを入力してください。")
	@Length(max = 200, message = "コメントは200文字以内で入力してください。")
	private String text;
}
