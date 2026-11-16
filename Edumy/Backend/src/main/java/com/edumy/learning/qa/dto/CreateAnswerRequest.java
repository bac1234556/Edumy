package com.edumy.learning.qa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateAnswerRequest {
    @NotBlank(message = "Nội dung câu trả lời không được để trống")
    @Size(max = 5000, message = "Nội dung tối đa 5000 ký tự")
    private String content;
}
