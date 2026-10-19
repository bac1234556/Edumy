package com.edumy.backend.service.impl;

import com.edumy.backend.dto.CartResponseDto;
import com.edumy.backend.model.*;
import com.edumy.backend.repository.*;
import com.edumy.backend.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    private Cart getOrCreateCart(Integer userId) {
        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) {
            cart = new Cart();
            cart.setUserId(userId);
            cart = cartRepository.save(cart);
        }
        return cart;
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponseDto getCart(Integer userId) {
        if (userId == null) {
            return null;
        }

        Cart cart = getOrCreateCart(userId);
        List<CartItem> cartItems = cart.getCartItems() != null ? cart.getCartItems() : Collections.emptyList();

        // Lấy danh sách các khóa học học viên đã sở hữu (Enrollments ACTIVE hoặc Completed)
        List<Enrollment> userEnrollments = enrollmentRepository.findByUserId(userId);
        Set<Integer> activeEnrolledCourseIds = userEnrollments.stream()
                .filter(e -> e.getStatus() == null || "ACTIVE".equalsIgnoreCase(e.getStatus()) || "Completed".equalsIgnoreCase(e.getStatus()))
                .map(Enrollment::getCourseId)
                .collect(Collectors.toSet());

        BigDecimal totalPrice = BigDecimal.ZERO;
        BigDecimal payableTotal = BigDecimal.ZERO;
        int payableItemsCount = 0;

        List<CartResponseDto.CartItemDto> itemDtos = new ArrayList<>();

        for (CartItem ci : cartItems) {
            Course course = ci.getCourse();
            BigDecimal price = (course != null && course.getPrice() != null) ? course.getPrice() : BigDecimal.ZERO;
            totalPrice = totalPrice.add(price);

            boolean isEnrolled = activeEnrolledCourseIds.contains(ci.getCourseId());
            if (!isEnrolled) {
                payableTotal = payableTotal.add(price);
                payableItemsCount++;
            }

            CartResponseDto.CourseSummaryDto courseSummary = null;
            if (course != null) {
                String instructorName = course.getInstructor() != null ? course.getInstructor().getFullName() : null;
                courseSummary = CartResponseDto.CourseSummaryDto.builder()
                        .title(course.getTitle())
                        .price(course.getPrice())
                        .originalPrice(course.getPrice())
                        .thumbnailUrl(course.getThumbnailUrl())
                        .instructorName(instructorName)
                        .averageRating(course.getAverageRating() != null ? course.getAverageRating() : 5.0)
                        .reviewsCount(course.getStudentCount() != null ? course.getStudentCount() : 0)
                        .build();
            }

            CartResponseDto.CartItemDto itemDto = CartResponseDto.CartItemDto.builder()
                    .id(ci.getId())
                    .courseId(ci.getCourseId())
                    .addedAt(ci.getAddedAt() != null ? ci.getAddedAt().toString() : null)
                    .isEnrolled(isEnrolled)
                    .isPaid(isEnrolled)
                    .course(courseSummary)
                    .build();

            itemDtos.add(itemDto);
        }

        boolean allPaid = !itemDtos.isEmpty() && payableItemsCount == 0;

        return CartResponseDto.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .totalItems(itemDtos.size())
                .totalPrice(totalPrice)
                .totalAmount(totalPrice)
                .payableTotal(payableTotal)
                .payableItemsCount(payableItemsCount)
                .allPaid(allPaid)
                .items(itemDtos)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> addToCart(Integer userId, Integer courseId) {
        Map<String, String> res = new HashMap<>();

        Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null || !"Published".equals(course.getStatus())) {
            throw new NoSuchElementException("Khóa học không tồn tại hoặc chưa xuất bản.");
        }

        boolean alreadyEnrolled = enrollmentRepository.existsByUserIdAndCourseId(userId, courseId);
        if (alreadyEnrolled) {
            throw new IllegalStateException("Bạn đã đăng ký và sở hữu khóa học này rồi.");
        }

        Cart cart = getOrCreateCart(userId);

        if (cart.getCartItems() != null && cart.getCartItems().stream().anyMatch(ci -> ci.getCourseId().equals(courseId))) {
            res.put("message", "Khóa học đã có sẵn trong giỏ hàng.");
            return res;
        }

        CartItem cartItem = new CartItem();
        cartItem.setCartId(cart.getId());
        cartItem.setCourseId(courseId);
        cartItem.setAddedAt(LocalDateTime.now());

        cartItemRepository.save(cartItem);

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        res.put("message", "Đã thêm khóa học vào giỏ hàng thành công.");
        return res;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> removeFromCart(Integer userId, Integer courseId) {
        Cart cart = getOrCreateCart(userId);
        
        // 1. Delete from database via explicit transactional query (supporting both courseId and cartItemId)
        cartItemRepository.deleteByCartIdAndCourseOrItemId(cart.getId(), courseId);

        // 2. Remove matching item from loaded collection to avoid Hibernate cascade re-save or orphanRemoval conflict
        if (cart.getCartItems() != null) {
            cart.getCartItems().removeIf(ci -> 
                (ci.getCourseId() != null && ci.getCourseId().equals(courseId)) ||
                (ci.getId() != null && ci.getId().equals(courseId))
            );
        }

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        Map<String, String> res = new HashMap<>();
        res.put("message", "Đã xóa khóa học khỏi giỏ hàng thành công.");
        return res;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> removeCartItem(Integer userId, Integer itemId) {
        return removeFromCart(userId, itemId);
    }
}
