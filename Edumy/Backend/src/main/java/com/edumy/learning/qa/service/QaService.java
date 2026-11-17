package com.edumy.learning.qa.service;

import com.edumy.shared.exception.BusinessException;
import com.edumy.shared.exception.ErrorCode;
import com.edumy.authoring.model.Course;
import com.edumy.authoring.repository.CourseRepository;
import com.edumy.learning.qa.dto.*;
import com.edumy.learning.qa.entity.CourseQuestion;
import com.edumy.learning.qa.entity.QuestionAnswer;
import com.edumy.learning.qa.repository.CourseQuestionRepository;
import com.edumy.learning.qa.repository.QuestionAnswerRepository;
import com.edumy.identity.user.model.User;
import com.edumy.identity.user.repository.UserRepository;
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
     * UC-S18: Student view: questions in a course/lesson, newest first, with optional filters
     * (lessonId, mineOnly, unansweredOnly, keyword q).
     */
    @Transactional(readOnly = true)
    public Page<QuestionSummaryResponse> listForCourse(Integer courseId,
                                                       Integer lessonId,
                                                       Integer currentUserId,
                                                       boolean mineOnly,
                                                       boolean unansweredOnly,
                                                       String keyword,
                                                       Pageable pageable) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy khóa học."));

        Integer authorFilter = (mineOnly && currentUserId != null) ? currentUserId : null;
        String cleanKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;

        Page<CourseQuestion> page = questionRepository.findForStudentCourse(
                courseId, lessonId, authorFilter, unansweredOnly, cleanKeyword, pageable);
        Map<Integer, User> userMap = loadUsers(page.getContent().stream().map(CourseQuestion::getUserId).collect(Collectors.toSet()));

        return page.map(q -> toSummary(q, course.getTitle(), userMap.get(q.getUserId())));
    }

    /**
     * Overload for backwards compatibility.
     */
    @Transactional(readOnly = true)
    public Page<QuestionSummaryResponse> listForCourse(Integer courseId, Integer lessonId, Pageable pageable) {
        return listForCourse(courseId, lessonId, null, false, false, null, pageable);
    }

    /**
     * UC-S18: Thread view: question and all visible answers.
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

        List<AnswerResponse> answerDtos = answers.stream().map(a -> {
            User author = userMap.get(a.getAuthorId());
            return AnswerResponse.builder()
                    .id(a.getId())
                    .authorId(a.getAuthorId())
                    .authorName(author != null ? author.getFullName() : "Người dùng")
                    .authorAvatar(author != null ? author.getAvatarUrl() : null)
                    .authorType(a.getAuthorType())
                    .content(a.getContent())
                    .visibilityStatus(a.getVisibilityStatus())
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
                .visibilityStatus(question.getVisibilityStatus())
                .createdAt(question.getCreatedAt())
                .answers(answerDtos)
                .build();
    }

    /**
     * UC-S17: Ask a new question in a course/lesson.
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
     * UC-S19: Edit own question.
     */
    public QuestionThreadResponse updateQuestion(Integer userId, String userRole, Long questionId, String title, String content) {
        CourseQuestion question = questionRepository.findById(questionId)
                .filter(q -> !"DELETED".equalsIgnoreCase(q.getVisibilityStatus()))
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy câu hỏi."));

        if (!Objects.equals(question.getUserId(), userId) && !"Admin".equalsIgnoreCase(userRole)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Bạn chỉ có thể chỉnh sửa câu hỏi của chính mình.");
        }
        if ("HIDDEN".equalsIgnoreCase(question.getVisibilityStatus())) {
            throw new BusinessException(ErrorCode.CONFLICT, "Câu hỏi đã bị ẩn bởi Quản trị viên, không thể chỉnh sửa.");
        }

        if (title != null && !title.trim().isEmpty()) {
            question.setTitle(title.trim());
        }
        if (content != null && !content.trim().isEmpty()) {
            question.setContent(content.trim());
        }
        question.setUpdatedAt(LocalDateTime.now());
        questionRepository.save(question);
        return getThread(questionId);
    }

    /**
     * UC-S19: Delete own question (soft-delete to DELETED).
     */
    public void deleteQuestion(Integer userId, String userRole, Long questionId) {
        CourseQuestion question = questionRepository.findById(questionId)
                .filter(q -> !"DELETED".equalsIgnoreCase(q.getVisibilityStatus()))
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy câu hỏi."));

        if (!Objects.equals(question.getUserId(), userId) && !"Admin".equalsIgnoreCase(userRole)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Bạn chỉ có thể xóa câu hỏi của chính mình.");
        }

        question.setVisibilityStatus("DELETED");
        question.setUpdatedAt(LocalDateTime.now());
        questionRepository.save(question);
    }

    /**
     * UC-I20: Post a reply to a question.
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
                .visibilityStatus(saved.getVisibilityStatus())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    /**
     * UC-I21 / UC-S19: Edit own answer.
     */
    public AnswerResponse updateAnswer(Integer userId, String userRole, Long answerId, String content) {
        QuestionAnswer answer = answerRepository.findById(answerId)
                .filter(a -> !"DELETED".equalsIgnoreCase(a.getVisibilityStatus()))
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy câu trả lời."));

        if (!Objects.equals(answer.getAuthorId(), userId) && !"Admin".equalsIgnoreCase(userRole)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Bạn chỉ có thể chỉnh sửa phản hồi của chính mình.");
        }
        if ("HIDDEN".equalsIgnoreCase(answer.getVisibilityStatus())) {
            throw new BusinessException(ErrorCode.CONFLICT, "Phản hồi đã bị ẩn bởi Quản trị viên, không thể chỉnh sửa.");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Nội dung phản hồi không được để trống.");
        }

        answer.setContent(content.trim());
        answer.setUpdatedAt(LocalDateTime.now());
        QuestionAnswer saved = answerRepository.save(answer);
        User author = userRepository.findById(saved.getAuthorId()).orElse(null);

        return AnswerResponse.builder()
                .id(saved.getId())
                .authorId(saved.getAuthorId())
                .authorName(author != null ? author.getFullName() : "Người dùng")
                .authorAvatar(author != null ? author.getAvatarUrl() : null)
                .authorType(saved.getAuthorType())
                .content(saved.getContent())
                .visibilityStatus(saved.getVisibilityStatus())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    /**
     * UC-I21 / UC-S19: Delete own answer (soft-delete to DELETED).
     */
    public void deleteAnswer(Integer userId, String userRole, Long answerId) {
        QuestionAnswer answer = answerRepository.findById(answerId)
                .filter(a -> !"DELETED".equalsIgnoreCase(a.getVisibilityStatus()))
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy câu trả lời."));

        if (!Objects.equals(answer.getAuthorId(), userId) && !"Admin".equalsIgnoreCase(userRole)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Bạn chỉ có thể xóa phản hồi của chính mình.");
        }

        answer.setVisibilityStatus("DELETED");
        answer.setUpdatedAt(LocalDateTime.now());
        answerRepository.save(answer);
    }

    /**
     * Toggle or set resolved mark on question.
     */
    public QuestionSummaryResponse toggleResolve(Integer userId, Long questionId) {
        return setResolved(userId, questionId, null);
    }

    public QuestionSummaryResponse setResolved(Integer userId, Long questionId, Boolean resolved) {
        CourseQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy câu hỏi."));

        boolean target = resolved != null ? resolved : !Boolean.TRUE.equals(question.getIsResolved());
        question.setIsResolved(target);
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

    /**
     * UC-A19: List all questions across all courses for Admin Moderation (including HIDDEN).
     */
    @Transactional(readOnly = true)
    public List<QuestionSummaryResponse> listAllForAdmin() {
        List<CourseQuestion> all = questionRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(q -> !"DELETED".equalsIgnoreCase(q.getVisibilityStatus()))
                .toList();

        Set<Integer> userIds = all.stream().map(CourseQuestion::getUserId).collect(Collectors.toSet());
        Set<Integer> courseIds = all.stream().map(CourseQuestion::getCourseId).collect(Collectors.toSet());

        Map<Integer, User> userMap = loadUsers(userIds);
        Map<Integer, String> courseTitleMap = courseIds.isEmpty() ? Map.of() :
                courseRepository.findAllById(courseIds).stream()
                        .collect(Collectors.toMap(Course::getCourseId, Course::getTitle));

        return all.stream()
                .map(q -> toSummary(q, courseTitleMap.getOrDefault(q.getCourseId(), "Khóa học #" + q.getCourseId()), userMap.get(q.getUserId())))
                .collect(Collectors.toList());
    }

    /**
     * UC-A19: Admin hides or restores a question.
     */
    public QuestionSummaryResponse moderateQuestion(Long questionId, String newVisibility) {
        CourseQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy câu hỏi."));
        question.setVisibilityStatus(newVisibility);
        question.setUpdatedAt(LocalDateTime.now());
        questionRepository.save(question);

        Course course = courseRepository.findById(question.getCourseId()).orElse(null);
        User user = userRepository.findById(question.getUserId()).orElse(null);
        return toSummary(question, course != null ? course.getTitle() : "Khóa học", user);
    }

    /**
     * UC-A19: Admin hides or restores an answer.
     */
    public AnswerResponse moderateAnswer(Long answerId, String newVisibility) {
        QuestionAnswer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy câu trả lời."));
        answer.setVisibilityStatus(newVisibility);
        answer.setUpdatedAt(LocalDateTime.now());
        answerRepository.save(answer);

        User author = userRepository.findById(answer.getAuthorId()).orElse(null);
        return AnswerResponse.builder()
                .id(answer.getId())
                .authorId(answer.getAuthorId())
                .authorName(author != null ? author.getFullName() : "Người dùng")
                .authorAvatar(author != null ? author.getAvatarUrl() : null)
                .authorType(answer.getAuthorType())
                .content(answer.getContent())
                .visibilityStatus(answer.getVisibilityStatus())
                .createdAt(answer.getCreatedAt())
                .build();
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
                .visibilityStatus(q.getVisibilityStatus())
                .createdAt(q.getCreatedAt())
                .build();
    }

    private Map<Integer, User> loadUsers(Set<Integer> userIds) {
        if (userIds.isEmpty()) return Map.of();
        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, u -> u));
    }
}
