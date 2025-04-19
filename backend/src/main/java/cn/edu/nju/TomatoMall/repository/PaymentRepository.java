package cn.edu.nju.TomatoMall.repository;

import cn.edu.nju.TomatoMall.enums.PaymentMethod;
import cn.edu.nju.TomatoMall.enums.PaymentStatus;
import cn.edu.nju.TomatoMall.models.po.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    // Add any custom query methods if needed
    // For example:
    // List<Payment> findByOrderId(Long orderId);

    @Query("SELECT p FROM Payment p WHERE p.user.id = ?1 AND p.status = ?2")
    List<Payment> findByUserIdAndStatus(int userId, PaymentStatus status);

    List<Payment> findByStatus(PaymentStatus status);

    @Query("SELECT p.paymentMethod FROM Payment p WHERE p.id = ?1")
    PaymentMethod getPaymentMethodById(String id);
}
