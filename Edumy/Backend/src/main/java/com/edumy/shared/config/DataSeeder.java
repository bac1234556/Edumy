package com.edumy.shared.config;

import com.edumy.authoring.model.Category;
import com.edumy.authoring.repository.CategoryRepository;
import com.edumy.learning.qa.entity.CourseQuestion;
import com.edumy.learning.qa.entity.QuestionAnswer;
import com.edumy.learning.qa.repository.CourseQuestionRepository;
import com.edumy.learning.qa.repository.QuestionAnswerRepository;
import com.edumy.identity.user.model.Role;
import com.edumy.identity.user.repository.RoleRepository;
import com.edumy.identity.user.model.User;
import com.edumy.identity.user.repository.UserRepository;
import com.edumy.identity.user.model.UserRole;
import com.edumy.identity.user.repository.UserRoleRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired private RoleRepository roleRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private UserRoleRepository userRoleRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private CourseQuestionRepository courseQuestionRepository;
    @Autowired private QuestionAnswerRepository questionAnswerRepository;

    @Override
    public void run(String... args) throws Exception {
        seedInitialUsersAndCategories();
        seedQaThreadsIfNeeded();
    }

    private void seedInitialUsersAndCategories() {
        if (userRepository.findByEmail("admin@edumy.com") != null) {
            return;
        }

        Role adminRole = new Role(); adminRole.setName("Admin");
        Role instructorRole = new Role(); instructorRole.setName("Instructor");
        Role studentRole = new Role(); studentRole.setName("Student");
        roleRepository.saveAll(Arrays.asList(adminRole, instructorRole, studentRole));

        User admin = new User();
        admin.setEmail("admin@edumy.com");
        admin.setPasswordHash(passwordEncoder.encode("Admin@123"));
        admin.setFullName("System Admin");
        admin.setIsActive(true);
        admin.setRole("Admin");
        userRepository.save(admin);

        UserRole ur = new UserRole();
        ur.setUserId(admin.getUserId());
        ur.setRoleId(adminRole.getId());
        userRoleRepository.save(ur);

        User instructor = new User();
        instructor.setEmail("instructor@edumy.com");
        instructor.setPasswordHash(passwordEncoder.encode("Admin@123"));
        instructor.setFullName("Giảng viên Edumy");
        instructor.setIsActive(true);
        instructor.setRole("Instructor");
        userRepository.save(instructor);

        UserRole urInst = new UserRole();
        urInst.setUserId(instructor.getUserId());
        urInst.setRoleId(instructorRole.getId());
        userRoleRepository.save(urInst);

        User student = new User();
        student.setEmail("student@edumy.com");
        student.setPasswordHash(passwordEncoder.encode("Admin@123"));
        student.setFullName("Học viên Edumy");
        student.setIsActive(true);
        student.setRole("Student");
        userRepository.save(student);

        UserRole urStud = new UserRole();
        urStud.setUserId(student.getUserId());
        urStud.setRoleId(studentRole.getId());
        userRoleRepository.save(urStud);

        Category devCat = new Category(); devCat.setName("Development");
        Category busCat = new Category(); busCat.setName("Business");
        Category designCat = new Category(); designCat.setName("Design");
        Category marketingCat = new Category(); marketingCat.setName("Marketing");
        Category itCat = new Category(); itCat.setName("IT & Software");
        categoryRepository.saveAll(Arrays.asList(devCat, busCat, designCat, marketingCat, itCat));

        System.out.println("Database seeded successfully.");
    }

    private void seedQaThreadsIfNeeded() {
        try {
            if (courseQuestionRepository.count() >= 5) {
                return;
            }
            User student = userRepository.findByEmail("student@edumy.com");
            User instructor = userRepository.findByEmail("instructor@edumy.com");
            Integer studentId = student != null ? student.getUserId() : 5;
            Integer instructorId = instructor != null ? instructor.getUserId() : 4;

            // 1. Course 2 (Java Spring Boot) - Answered & Resolved
            CourseQuestion q1 = courseQuestionRepository.save(CourseQuestion.builder()
                    .courseId(2)
                    .userId(studentId)
                    .title("Làm sao để tách 7 Bounded Contexts trong Spring Modulith mà không bị vòng lặp phụ thuộc?")
                    .content("Em đang thực hành cấu trúc dự án theo chuẩn Spring Modulith gồm authoring, catalog, commerce, identity, learning, notification, shared. Khi module commerce cần kiểm tra khóa học trong authoring thì nên gọi trực tiếp Repository hay qua Application Service / API interface ạ?")
                    .isResolved(true)
                    .visibilityStatus("VISIBLE")
                    .createdAt(LocalDateTime.now().minusDays(4))
                    .updatedAt(LocalDateTime.now().minusDays(3))
                    .build());

            questionAnswerRepository.save(QuestionAnswer.builder()
                    .questionId(q1.getId())
                    .authorId(instructorId)
                    .authorType("INSTRUCTOR")
                    .content("Chào em! Trong kiến trúc Spring Modulith, tuyệt đối không gọi trực tiếp Repository nội bộ của module khác. Em hãy tạo một Public API interface đặt trong package `authoring.api` (ví dụ `CourseCatalogPort` hoặc `CourseAccessApi`) hoặc giao tiếp qua Domain Events (`@ApplicationModuleListener` / `@TransactionalEventListener`). Khi đó `ModularityTest` sẽ kiểm tra và đảm bảo không có chu trình phụ thuộc.")
                    .visibilityStatus("VISIBLE")
                    .createdAt(LocalDateTime.now().minusDays(3))
                    .updatedAt(LocalDateTime.now().minusDays(3))
                    .build());

            // 2. Course 1 (MERN & Next.js) - Unanswered (Oldest in waiting queue for Instructor #4)
            courseQuestionRepository.save(CourseQuestion.builder()
                    .courseId(1)
                    .userId(studentId)
                    .title("Lỗi Hydration Mismatch khi render ngày tháng ở Server Component trong Next.js 14")
                    .content("Thưa thầy, ở bài giảng phần App Router khi em format thời gian bằng `toLocaleDateString('vi-VN')` trực tiếp trong component thì trình duyệt báo lỗi Text content does not match server-rendered HTML. Em nên xử lý như thế nào cho chuẩn ạ?")
                    .isResolved(false)
                    .visibilityStatus("VISIBLE")
                    .createdAt(LocalDateTime.now().minusDays(2).minusHours(5))
                    .updatedAt(LocalDateTime.now().minusDays(2).minusHours(5))
                    .build());

            // 3. Course 2 (Java Spring Boot) - Unanswered (Second in waiting queue for Instructor #4)
            courseQuestionRepository.save(CourseQuestion.builder()
                    .courseId(2)
                    .userId(studentId)
                    .title("Sự khác biệt giữa @TransactionalEventListener(AFTER_COMMIT) và gửi mail đồng bộ?")
                    .content("Khi tích hợp Mailpit để gửi email xác thực tài khoản và thông báo đơn hàng, tại sao chúng ta nên dùng `@Async` kết hợp `@TransactionalEventListener` thay vì gọi `mailSender.send()` ngay trong hàm đăng ký?")
                    .isResolved(false)
                    .visibilityStatus("VISIBLE")
                    .createdAt(LocalDateTime.now().minusDays(1).minusHours(3))
                    .updatedAt(LocalDateTime.now().minusDays(1).minusHours(3))
                    .build());

            // 4. Course 3 (ReactJS 18 & TypeScript) - Unanswered (Third in waiting queue for Instructor #4)
            courseQuestionRepository.save(CourseQuestion.builder()
                    .courseId(3)
                    .userId(studentId)
                    .title("Cách tối ưu re-render khi quản lý trạng thái Video Player và danh sách bài học")
                    .content("Em thấy khi cập nhật `currentVideoTime` mỗi giây trong trình phát video thì toàn bộ danh sách chương mục bên phải cũng bị render lại. Mình nên tách component hoặc dùng `useMemo` như thế nào cho tối ưu nhất ạ?")
                    .isResolved(false)
                    .visibilityStatus("VISIBLE")
                    .createdAt(LocalDateTime.now().minusHours(8))
                    .updatedAt(LocalDateTime.now().minusHours(8))
                    .build());

            // 5. Course 15 (Tài chính Doanh nghiệp - Instructor #2) - Unanswered in queue for Instructor #2
            courseQuestionRepository.save(CourseQuestion.builder()
                    .courseId(15)
                    .userId(studentId)
                    .title("Cách tính dòng tiền chiết khấu DCF và chỉ số WACC cho dự án khởi nghiệp công nghệ")
                    .content("Chào giảng viên, đối với một startup SaaS chưa có lợi nhuận dương trong 2 năm đầu thì khi áp dụng mô hình DCF ở Chương 3 mình nên ước tính tốc độ tăng trưởng dài hạn (Terminal Growth Rate) như thế nào cho hợp lý?")
                    .isResolved(false)
                    .visibilityStatus("VISIBLE")
                    .createdAt(LocalDateTime.now().minusDays(1))
                    .updatedAt(LocalDateTime.now().minusDays(1))
                    .build());

            // 6. Course 22 (Digital Marketing - Instructor #2) - Unanswered in queue for Instructor #2
            courseQuestionRepository.save(CourseQuestion.builder()
                    .courseId(22)
                    .userId(studentId)
                    .title("Cách thiết lập phễu AARRR Growth Hacking để tối ưu tỷ lệ chuyển đổi học viên")
                    .content("Thầy cho em hỏi ở bước Activation và Retention trong phễu AARRR, việc gửi email tự động nhắc nhở và giải đáp Q&A nhanh giúp tăng tỷ lệ giữ chân người dùng lên bao nhiêu phần trăm trong thực tế?")
                    .isResolved(false)
                    .visibilityStatus("VISIBLE")
                    .createdAt(LocalDateTime.now().minusHours(5))
                    .updatedAt(LocalDateTime.now().minusHours(5))
                    .build());

            System.out.println("==> Seeded sample Q&A threads and instructor waiting queue items.");
        } catch (Exception e) {
            System.err.println("Failed to seed Q&A data: " + e.getMessage());
        }
    }
}
