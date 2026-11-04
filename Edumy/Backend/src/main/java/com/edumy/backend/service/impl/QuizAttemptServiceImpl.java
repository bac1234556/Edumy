package com.edumy.backend.service.impl;

import com.edumy.backend.dto.QuestionResultDto;
import com.edumy.backend.dto.QuizResultDto;
import com.edumy.backend.dto.QuizSubmissionDto;
import com.edumy.backend.dto.QuizSubmitRequest;
import com.edumy.backend.model.*;
import com.edumy.backend.repository.*;
import com.edumy.backend.service.QuizAttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class QuizAttemptServiceImpl implements QuizAttemptService {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private QuizAttemptAnswerRepository quizAttemptAnswerRepository;

    @Autowired
    private CourseSectionRepository courseSectionRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private LessonProgressRepository lessonProgressRepository;

    private Integer getCurrentUserId() {
        try {
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            return (name != null && !name.equalsIgnoreCase("anonymousUser")) ? Integer.parseInt(name) : null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QuizResultDto submitQuiz(Long quizId, QuizSubmitRequest request) {
        if (quizId == null) {
            throw new IllegalArgumentException("Quiz ID không được để trống.");
        }
        Integer userId = getCurrentUserId();
        if (userId == null) {
            throw new AccessDeniedException("Yêu cầu đăng nhập để nộp bài kiểm tra.");
        }

        Map<Integer, Integer> answersMap = request != null ? request.toAnswersMap() : new HashMap<>();
        return processQuizSubmission(quizId.intValue(), answersMap, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QuizResultDto submitQuiz(Integer quizId, QuizSubmissionDto submission, Integer currentUserId) {
        if (quizId == null) {
            throw new IllegalArgumentException("Quiz ID không được để trống.");
        }
        Integer userId = currentUserId != null ? currentUserId : getCurrentUserId();
        if (userId == null) {
            throw new AccessDeniedException("Yêu cầu đăng nhập để nộp bài kiểm tra.");
        }

        Map<Integer, Integer> answersMap = (submission != null && submission.getSelectedAnswers() != null)
                ? submission.getSelectedAnswers()
                : new HashMap<>();

        return processQuizSubmission(quizId, answersMap, userId);
    }

    private QuizResultDto processQuizSubmission(Integer quizId, Map<Integer, Integer> selectedAnswers, Integer userId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy bài trắc nghiệm với ID: " + quizId));

        // 1. Khởi tạo và lưu đối tượng QuizAttempt (quiz_id, user_id, attempt_date, score, is_passed)
        QuizAttempt attempt = QuizAttempt.builder()
                .quizId(quiz.getQuizId())
                .userId(userId)
                .startedAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .score(0)
                .isPassed(false)
                .selectedAnswers(new ArrayList<>())
                .build();

        QuizAttempt savedAttempt = quizAttemptRepository.save(attempt);

        int score = 0;
        int totalPoints = 0;
        if (quiz.getQuestions() != null) {
            totalPoints = quiz.getQuestions().stream().mapToInt(Question::getPoints).sum();
        }

        List<QuizAttemptAnswer> answersToSave = new ArrayList<>();
        List<QuestionResultDto> resultList = new ArrayList<>();

        // 2. Lặp qua danh sách câu trả lời của học viên trong request, map chính xác từng câu với bảng QuizAttemptAnswers
        if (quiz.getQuestions() != null) {
            for (Question question : quiz.getQuestions()) {
                Integer selectedAnswerId = selectedAnswers.get(question.getQuestionId());
                if (selectedAnswerId == null) selectedAnswerId = 0;

                Answer correctAnswer = null;
                if (question.getAnswers() != null) {
                    correctAnswer = question.getAnswers().stream()
                            .filter(a -> Boolean.TRUE.equals(a.getIsCorrect()))
                            .findFirst()
                            .orElse(null);
                }

                boolean isCorrect = correctAnswer != null && correctAnswer.getAnswerId().equals(selectedAnswerId);
                if (isCorrect) {
                    score += question.getPoints();
                }

                // Ghi nhận chi tiết câu trả lời vào QuizAttemptAnswers
                if (selectedAnswerId > 0) {
                    QuizAttemptAnswer qaa = QuizAttemptAnswer.builder()
                            .quizAttemptId(savedAttempt.getQuizAttemptId())
                            .questionId(question.getQuestionId())
                            .answerId(selectedAnswerId)
                            .isCorrect(isCorrect)
                            .build();
                    answersToSave.add(qaa);
                }

                QuestionResultDto qr = new QuestionResultDto();
                qr.setQuestionId(question.getQuestionId());
                qr.setSelectedAnswerId(selectedAnswerId);
                qr.setCorrectAnswerId(correctAnswer != null ? correctAnswer.getAnswerId() : 0);
                qr.setIsCorrect(isCorrect);
                qr.setExplanation(question.getExplanation());
                resultList.add(qr);
            }
        }

        // 3. Dùng quizAttemptAnswerRepository.saveAll(answersList) để lưu hàng loạt
        if (!answersToSave.isEmpty()) {
            quizAttemptAnswerRepository.saveAll(answersToSave);
        }

        // Đánh giá tỷ lệ điểm và chuẩn đạt
        int passingScore = quiz.getPassingScore() != null ? quiz.getPassingScore() : 80;
        double percentage = totalPoints > 0 ? ((double) score / totalPoints) * 100.0 : 0.0;
        boolean isPassed = (score >= passingScore) || (percentage >= passingScore);

        savedAttempt.setScore(score);
        savedAttempt.setIsPassed(isPassed);
        quizAttemptRepository.save(savedAttempt);

        // 4. Nếu vượt qua điểm chuẩn, kiểm tra tiến độ và kích hoạt cấp chứng chỉ trong bảng Certificates
        if (isPassed) {
            triggerCertificateIfCompleted(quiz, userId);
        }

        QuizResultDto resultDto = new QuizResultDto();
        resultDto.setQuizAttemptId(savedAttempt.getQuizAttemptId());
        resultDto.setScore(score);
        resultDto.setTotalPoints(totalPoints);
        resultDto.setPassed(isPassed);
        resultDto.setResults(resultList);

        return resultDto;
    }

    /**
     * Kích hoạt cấp chứng chỉ khi học viên hoàn thành đúng 100% tất cả bài học và bài kiểm tra
     */
    private void triggerCertificateIfCompleted(Quiz quiz, Integer userId) {
        Integer sectionId = quiz.getCourseSectionId();
        CourseSection section = quiz.getCourseSection();
        if (section == null && sectionId != null) {
            section = courseSectionRepository.findById(sectionId).orElse(null);
        }
        if (section == null || section.getCourseId() == null) {
            return;
        }

        Integer courseId = section.getCourseId();

        // Kiểm tra tiến độ tổng thể thực tế
        long totalLessons = lessonRepository.countLessonsByCourseId(courseId);
        long completedLessons = lessonProgressRepository.countCompletedLessonsByUserAndCourse(userId, courseId);
        long totalQuizzes = quizRepository.countQuizzesByCourseId(courseId);
        java.util.List<Integer> quizIds = quizRepository.findQuizIdsByCourseId(courseId);
        long completedQuizzes = (quizIds != null && !quizIds.isEmpty()) 
                ? quizAttemptRepository.countByUserIdAndIsPassedTrueAndQuizIdIn(userId, quizIds) 
                : 0;

        long totalItems = totalLessons + totalQuizzes;
        long completedItems = completedLessons + completedQuizzes;
        int progressPercentage = totalItems > 0 
                ? (int) Math.min(100, Math.round((double) completedItems / totalItems * 100)) 
                : 100;

        // Cập nhật tiến độ hoàn thành khóa học trong Enrollments
        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(userId, courseId);
        if (enrollment != null) {
            enrollment.setProgressPercentage(progressPercentage);
            if (progressPercentage >= 100) {
                enrollment.setStatus("COMPLETED");
                if (enrollment.getCompletedAt() == null) {
                    enrollment.setCompletedAt(LocalDateTime.now());
                }
            }
            enrollmentRepository.save(enrollment);
        }

        // CHỈ cấp chứng chỉ khi học viên ĐÃ HOÀN THÀNH 100% TẤT CẢ BÀI HỌC VÀ BÀI KIỂM TRA
        if (totalItems > 0 && completedItems >= totalItems) {
            boolean alreadyHasCertificate = certificateRepository.existsByUserIdAndCourseId(userId, courseId);
            if (!alreadyHasCertificate) {
                String certCode = "EDUMY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
                Certificate cert = Certificate.builder()
                        .userId(userId)
                        .courseId(courseId)
                        .certificateUrl(certCode)
                        .issuedAt(LocalDateTime.now())
                        .build();
                certificateRepository.save(cert);
            }
        }
    }
}
