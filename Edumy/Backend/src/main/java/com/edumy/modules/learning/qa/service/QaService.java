package com.edumy.modules.learning.qa.service;

import com.edumy.common.exception.BusinessException;
import com.edumy.common.exception.ErrorCode;
import com.edumy.modules.course.model.Course;
import com.edumy.modules.course.repository.CourseRepository;
import com.edumy.modules.learning.qa.dto.*;
import com.edumy.modules.learning.qa.entity.CourseQuestion;
import com.edumy.modules.learning.qa.entity.QuestionAnswer;
import com.edumy.modules.learning.qa.repository.CourseQuestionRepository;
import com.edumy.modules.learning.qa.repository.QuestionAnswerRepository;
import com.edumy.modules.user.model.User;
import com.edumy.modules.user.repository.UserRepository;
import com.edumy.notification.service.MailService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class QaService {

    private final CourseQuestionRepository questionRepository;
    private final QuestionAnswerRepository answerRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final MailService mailService;

    public QaService(CourseQuestionRepository questionRepository,
                     QuestionAnswerRepository answerRepository,
                     CourseRepository courseRepository,
                     UserRepository userRepository,
                     MailService mailService) {
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.mailService = mailService;
    }

    /**
     * Student view: questions in a course/lesson, newest first.
     */
    @Transactional(readOnly = true)
    public Page<QuestionSummaryResponse> listForCourse(Integer courseId, Integer lessonId, Pageable pageable) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy khóa học."));

        Page<CourseQuestion> page = questionRepository.findForStudentCourse(courseId, lessonId, pageable);
        Map<Integer, User> userMap = loadUsers(page.getContent().stream().map(CourseQuestion::getUserId).collect(Collectors.toSet()));

        return page.map(q -> toSummary(q, course.getTitle(), userMap.get(q.getUserId())));
    }

    /**
     * Thread view: question and all visible answers.
     */
    @Transactional(readOnly = true)
    public QuestionThreadResponse getThread(Long questionId) {
        CourseQuestion question = questionRepository.findById(questionId)
                .filter(q -> !"DELETED".equalsIgnoreCase(q.getVisibilityStatus()))
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy câu hỏi hoặc câu hỏi đã bị xóa."));

        Course course = courseRepository.findById(question.getCourseId()).orElse(null);
        String courseTitle = course != null ? course.getTitle() : "Khóa học";

        List<QuestionAnswer> answers = answerRepository.findByQuestionIdAndVisibilityStatusOrderByCreatedAtAsc(questionId, "VISIBLE");

        Set<Integer> userIds = new HashSet<>();
        userIds.add(question.getUserId());
        answers.forEach(a -> userIds.add(a.getAuthorId()));
        Map<Integer, User> userMap = loadUsers(userIds);

        User asker = userMap.get(question.getUserId());

        List<AnswerResponse> answerResponses = answers.stream()
                .map(a -> {
                    User author = userMap.get(a.getAuthorId());
                    return AnswerResponse.builder()
                            .id(a.getId())
                            .authorId(a.getAuthorId())
                            .authorName(author != null ? author.getFullName() : "Người dùng")
                            .authorAvatar(author != null ? author.getAvatarUrl() : null)
                            .authorType(a.getAuthorType())
                            .content(a.getContent())
                            .createdAt(a.getCreatedAt())
                            .build();
                }).collect(Collectors.toList());

        return QuestionThreadResponse.builder()
                .id(question.getId())
                .courseId(question.getCourseId())
                .courseTitle(courseTitle)
                .lessonId(question.getLessonId())
                .title(question.getTitle())
                .content(question.getContent())
                .askerId(question.getUserId())
                .askerName(asker != null ? asker.getFullName() : "Học viên")
                .askerAvatar(asker != null ? asker.getAvatarUrl() : null)
                .isResolved(question.getIsResolved())
                .createdAt(question.getCreatedAt())
                .answers(answerResponses)
                .build();
    }

    /**
     * Post a new question.
     */
    public QuestionSummaryResponse askQuestion(Integer userId, Integer courseId, CreateQuestionRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy khóa học."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy người dùng."));

        CourseQuestion question = CourseQuestion.builder()
                .courseId(courseId)
                .lessonId(request.getLessonId())
                .userId(userId)
                .title(request.getTitle().trim())
                .content(request.getContent().trim())
                .isResolved(false)
                .visibilityStatus("VISIBLE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        CourseQuestion saved = questionRepository.save(question);
        return toSummary(saved, course.getTitle(), user);
    }

    /**
     * Post a reply to a question.
     */
    public AnswerResponse postAnswer(Integer userId, String userRole, Long questionId, CreateAnswerRequest request) {
        CourseQuestion question = questionRepository.findById(questionId)
                .filter(q -> !"DELETED".equalsIgnoreCase(q.getVisibilityStatus()))
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy câu hỏi hoặc câu hỏi đã bị xóa."));

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy người dùng."));

        Course course = courseRepository.findById(question.getCourseId()).orElse(null);

        boolean isInstructor = "Instructor".equalsIgnoreCase(userRole) ||
                "Admin".equalsIgnoreCase(userRole) ||
                (course != null && course.getInstructorId() != null && course.getInstructorId().equals(userId));

        String authorType = isInstructor ? "INSTRUCTOR" : "STUDENT";

        QuestionAnswer answer = QuestionAnswer.builder()
                .questionId(questionId)
                .authorId(userId)
                .authorType(authorType)
                .content(request.getContent().trim())
                .visibilityStatus("VISIBLE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        QuestionAnswer saved = answerRepository.save(answer);

        // If instructor answers, notify the student asker via email
        if (isInstructor && !userId.equals(question.getUserId())) {
            User student = userRepository.findById(question.getUserId()).orElse(null);
            if (student != null && student.getEmail() != null && course != null) {
                mailService.sendQuestionAnswered(
                        student.getEmail(),
                        student.getFullName(),
                        author.getFullName(),
                        question.getTitle(),
                        course.getTitle(),
                        course.getSlug(),
                        question.getId()
                );
            }
        }

        return AnswerResponse.builder()
                .id(saved.getId())
                .authorId(userId)
                .authorName(author.getFullName())
                .authorAvatar(author.getAvatarUrl())
                .authorType(authorType)
                .content(saved.getContent())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    /**
     * Toggle resolved mark on question.
     */
    public QuestionSummaryResponse toggleResolve(Integer userId, Long questionId) {
        CourseQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy câu hỏi."));

        question.setIsResolved(!Boolean.TRUE.equals(question.getIsResolved()));
        question.setUpdatedAt(LocalDateTime.now());
        questionRepository.save(question);

        Course course = courseRepository.findById(question.getCourseId()).orElse(null);
        User user = userRepository.findById(question.getUserId()).orElse(null);
        return toSummary(question, course != null ? course.getTitle() : "Khóa học", user);
    }

    /**
     * UC-I22: Instructor waiting queue — oldest first (cũ nhất trước).
     * Filter by instructor's courses, default unansweredOnly = true.
     */
    @Transactional(readOnly = true)
    public Page<QuestionSummaryResponse> listForInstructor(Integer instructorId, Integer courseIdFilter,
                                                           boolean unansweredOnly, Pageable pageable) {
        List<Course> instructedCourses = courseRepository.findByInstructorId(instructorId);
        if (instructedCourses.isEmpty()) {
            return Page.empty(pageable);
        }

        Map<Integer, String> courseTitleMap = instructedCourses.stream()
                .collect(Collectors.toMap(Course::getCourseId, Course::getTitle));

        Set<Integer> courseIds = new HashSet<>(courseTitleMap.keySet());
        if (courseIdFilter != null) {
            if (!courseIds.contains(courseIdFilter)) {
                return Page.empty(pageable);
            }
            courseIds = Set.of(courseIdFilter);
        }

        org.springframework.data.domain.Pageable oldestFirst = org.springframework.data.domain.PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                org.springframework.data.domain.Sort.by("createdAt").ascending()
        );
        Page<CourseQuestion> page = questionRepository.findForInstructorOldestQueue(courseIds, unansweredOnly, oldestFirst);
        Map<Integer, User> userMap = loadUsers(page.getContent().stream().map(CourseQuestion::getUserId).collect(Collectors.toSet()));

        return page.map(q -> toSummary(q, courseTitleMap.getOrDefault(q.getCourseId(), "Khóa học"), userMap.get(q.getUserId())));
    }

    private QuestionSummaryResponse toSummary(CourseQuestion q, String courseTitle, User asker) {
        long answersCount = answerRepository.countByQuestionIdAndVisibilityStatus(q.getId(), "VISIBLE");
        boolean hasInstructorAnswer = answerRepository.existsByQuestionIdAndAuthorTypeAndVisibilityStatus(q.getId(), "INSTRUCTOR", "VISIBLE");

        return QuestionSummaryResponse.builder()
                .id(q.getId())
                .courseId(q.getCourseId())
                .courseTitle(courseTitle)
                .lessonId(q.getLessonId())
                .title(q.getTitle())
                .content(q.getContent())
                .askerId(q.getUserId())
                .askerName(asker != null ? asker.getFullName() : "Học viên")
                .askerAvatar(asker != null ? asker.getAvatarUrl() : null)
                .isResolved(q.getIsResolved())
                .answerCount(answersCount)
                .hasInstructorAnswer(hasInstructorAnswer)
                .createdAt(q.getCreatedAt())
                .build();
    }

    private Map<Integer, User> loadUsers(Set<Integer> userIds) {
        if (userIds.isEmpty()) return Map.of();
        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, u -> u));
    }
}
