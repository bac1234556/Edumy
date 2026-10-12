// services/OrderService.java
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepo;

    @Loggable           // AOP tự động log
    @Transactional      // Spring tự động quản lý transaction
    @Monitor("đặt hàng") // AOP tự động thu thập chỉ số hiệu suất
    public OrderResponse placeOrder(CreateOrderRequest request) {
        // CHỈ logic nghiệp vụ ở đây
        var order = Order.create(request);
        return orderRepo.save(order);
    }
}