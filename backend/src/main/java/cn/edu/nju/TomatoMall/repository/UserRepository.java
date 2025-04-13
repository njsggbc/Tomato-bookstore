package cn.edu.nju.TomatoMall.repository;

import cn.edu.nju.TomatoMall.models.po.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    Optional<User> findByPhone(String phone);
    boolean existsByPhone(String phone);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}