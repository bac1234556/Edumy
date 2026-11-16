package com.edumy.modules.learning.qa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateQuestionRequest {
    @NotBlank(message = "Tiêu đề câu hỏi không được để trống")
    @Size(max = 200, message = "Tiêu đề tối đa 200 ký tự")
    private String title;

    @NotBlank(message = "Nội dung câu hỏi không được để trống")
    @Size(max = 5000, message = "Nội dung tối đa 5000 ký tự")
    private String content;

    private Integer lessonId;
}
