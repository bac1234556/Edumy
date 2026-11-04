package com.edumy.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizSubmitRequest {
    private Long quizId;
    
    // Hỗ trợ map questionId -> selectedAnswerId
    @Builder.Default
    private Map<Long, Long> answersMap = new HashMap<>();

    // Hoặc danh sách câu trả lời chi tiết
    @Builder.Default
    private List<SelectedAnswerItem> answers = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SelectedAnswerItem {
        private Long questionId;
        private Long selectedAnswerId;
    }

    /**
     * Chuyển đổi linh hoạt sang Map<Integer, Integer> cho việc xử lý
     */
    public Map<Integer, Integer> toAnswersMap() {
        Map<Integer, Integer> result = new HashMap<>();
        if (answersMap != null) {
            answersMap.forEach((k, v) -> {
                if (k != null && v != null) {
                    result.put(k.intValue(), v.intValue());
                }
            });
        }
        if (answers != null) {
            for (SelectedAnswerItem item : answers) {
                if (item.getQuestionId() != null && item.getSelectedAnswerId() != null) {
                    result.put(item.getQuestionId().intValue(), item.getSelectedAnswerId().intValue());
                }
            }
        }
        return result;
    }
}
