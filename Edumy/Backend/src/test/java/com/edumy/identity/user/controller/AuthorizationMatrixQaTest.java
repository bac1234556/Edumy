package com.edumy.identity.user.controller;

import com.edumy.authoring.controller.SpecUseCaseController;
import com.edumy.authoring.repository.CourseRepository;
import com.edumy.authoring.repository.CourseSectionRepository;
import com.edumy.authoring.repository.LessonRepository;
import com.edumy.commerce.order.repository.OrderRepository;
import com.edumy.identity.user.repository.UserRepository;
import com.edumy.learning.enrollment.repository.EnrollmentRepository;
import com.edumy.learning.enrollment.repository.LessonProgressRepository;
import com.edumy.learning.review.repository.ReviewRepository;
import com.edumy.notification.service.MailService;
import com.edumy.payout.controller.AdminWithdrawalController;
import com.edumy.shared.exception.BusinessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AuthorizationMatrixQaTest {

    @Mock private UserRepository userRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private CourseSectionRepository courseSectionRepository;
    @Mock private LessonRepository lessonRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private LessonProgressRepository lessonProgressRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private MailService mailService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("AC-37: Admin cannot self-lock via SpecUseCaseController.lockUser")
    void adminCannotSelfLock() {
        var auth = new UsernamePasswordAuthenticationToken(
                "1", null, List.of(new SimpleGrantedAuthority("ROLE_Admin"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        SpecUseCaseController ctrl = new SpecUseCaseController(
                userRepository, courseRepository, courseSectionRepository, lessonRepository,
                enrollmentRepository, lessonProgressRepository, orderRepository, reviewRepository, mailService
        );

        BusinessException ex = assertThrows(BusinessException.class, () -> ctrl.lockUser(1));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    @DisplayName("DEF-W-005 / AC-37: Admin cannot lock another Admin")
    void adminCannotLockAnotherAdmin() {
        var auth = new UsernamePasswordAuthenticationToken(
                "1", null, List.of(new SimpleGrantedAuthority("ROLE_Admin"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        var otherAdmin = com.edumy.identity.user.model.User.builder()
                .userId(2).email("admin2@edumy.test").role("Admin").isActive(true).build();
        org.mockito.Mockito.when(userRepository.findById(2)).thenReturn(java.util.Optional.of(otherAdmin));

        SpecUseCaseController ctrl = new SpecUseCaseController(
                userRepository, courseRepository, courseSectionRepository, lessonRepository,
                enrollmentRepository, lessonProgressRepository, orderRepository, reviewRepository, mailService
        );

        BusinessException ex = assertThrows(BusinessException.class, () -> ctrl.lockUser(2));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }

    @Test
    @DisplayName("NFR-01 RBAC: Critical Admin controllers must enforce @PreAuthorize hasRole('Admin')")
    void adminControllersRequireAdminRole() {
        PreAuthorize adminCtrlAuth = AdminController.class.getAnnotation(PreAuthorize.class);
        assertNotNull(adminCtrlAuth, "AdminController must declare @PreAuthorize");
        assertTrue(adminCtrlAuth.value().contains("Admin"));

        PreAuthorize adminWdAuth = AdminWithdrawalController.class.getAnnotation(PreAuthorize.class);
        assertNotNull(adminWdAuth, "AdminWithdrawalController must declare @PreAuthorize");
        assertTrue(adminWdAuth.value().contains("Admin"));
    }
}
