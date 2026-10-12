// interceptors/AuthInterceptor.java
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthService authService;

    @Override
    public boolean preHandle(HttpServletRequest request,
                            HttpServletResponse response,
                            Object handler) {
        if (!authService.isAuthenticated(request)) {
            response.setStatus(401);
            return false; // Chặn, không đi tiếp
        }
        return true; // Thông qua
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                               HttpServletResponse response,
                               Object handler, Exception ex) {
        // Logic dọn dẹp
    }
}