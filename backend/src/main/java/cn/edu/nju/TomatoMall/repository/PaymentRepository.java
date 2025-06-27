package cn.edu.nju.TomatoMall.repository;

import cn.edu.nju.TomatoMall.enums.PaymentStatus;
import cn.edu.nju.TomatoMall.models.po.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    // Add any custom query methods if needed
    // For example:
    // List<Payment> findByOrderId(Long orderId);

    Optional<Payment> findById(int id);

    Optional<Payment> findByPaymentNo(String paymentNo);

    @Query("SELECT p FROM Payment p WHERE p.id = ?1 AND p.user.id = ?2")
    Optional<Payment> findByIdAndUserId(int id, int userId);

    Optional<Payment> findByPaymentNoAndUserId(String paymentNo, int userId);

    @Query("SELECT p FROM Payment p WHERE p.user.id = ?1 AND p.status = ?2")
    Page<Payment> findByUserIdAndStatus(int userId, PaymentStatus status, Pageable pageable);

    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findByStatusAndPaymentMethodIsNullAndCreateTimeBefore(PaymentStatus paymentStatus, LocalDateTime timeoutThreshold);

    List<Payment> findByStatusAndPaymentMethodIsNotNullAndPaymentRequestTimeBefore(PaymentStatus paymentStatus, LocalDateTime timeoutThreshold);

    @Query("SELECT p FROM Payment p WHERE :paymentId IS NULL OR p.id = :paymentId " +
           "AND (:paymentNo IS NULL OR p.paymentNo = :paymentNo) " +
           "AND p.user.id = :userId")
    Optional<Payment> findByIdOrPaymentNoAndUserId(Integer paymentId, String paymentNo, int userId);
}
