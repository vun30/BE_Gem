package online.gemfpt.BE.service;

import online.gemfpt.BE.Repository.ProductsRepository;
import online.gemfpt.BE.entity.CustomerPoint;
import online.gemfpt.BE.entity.Order;
import online.gemfpt.BE.entity.Discount;
import online.gemfpt.BE.Repository.CustomerPointRepository;
import online.gemfpt.BE.Repository.OrderRepository;
import online.gemfpt.BE.Repository.DiscountRepository;
import online.gemfpt.BE.entity.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private DiscountRepository discountRepository;

    @Autowired
    private CustomerPointRepository customerPointRepository;

    @Autowired
    private ProductsRepository productsRepository;

    public Order saveOrder(Order order, List<OrderItem> orderItems, Long discountId) {
        if (discountId != null) {
            Discount discount = discountRepository.findById(discountId).orElse(null);
            if (discount != null && discount.isStatus()) {
                double discountAmount = order.getTotalAmount() * (discount.getDiscountRate() / 100);
                order.setDiscountAmount(discountAmount);
                order.setTotalAmount(order.getTotalAmount() - discountAmount);
                order.setDiscount(discount);
            }
        }
        Order savedOrder = orderRepository.save(order);

        // Lưu các sản phẩm trong đơn hàng
        for (OrderItem item : orderItems) {
            item.setOrder(savedOrder);
            productsRepository.save(item.getProduct());
        }

        // Tính toán và cộng điểm cho khách hàng
        int pointsEarned = (int) (savedOrder.getTotalAmount() / 1000); // Giả sử mỗi 1000 VND được 1 điểm
        CustomerPoint customerPoint = new CustomerPoint();
        customerPoint.setCustomer(savedOrder.getCustomer());
        customerPoint.setPoints(pointsEarned);
        customerPoint.setCreateDate(LocalDateTime.now());
        customerPointRepository.save(customerPoint);

        return savedOrder;
    }

    public Order getOrderById(long id) {
        return orderRepository.findById(id).orElse(null);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public void deleteOrder(long id) {
        orderRepository.deleteById(id);
    }
}
