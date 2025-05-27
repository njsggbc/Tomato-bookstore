package cn.edu.nju.TomatoMall.repository;

import cn.edu.nju.TomatoMall.models.po.Employment;
import cn.edu.nju.TomatoMall.models.po.Store;
import cn.edu.nju.TomatoMall.models.po.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmploymentRepository extends JpaRepository<Employment, Integer> {
    // Find employment by store ID and employee ID
    @Query("SELECT e FROM Employment e WHERE e.store.id = :storeId AND e.employee.id = :employeeId")
    Optional<Employment> findByStoreIdAndEmployeeId(@Param("storeId") int storeId, @Param("employeeId") int employeeId);

    // Get all employees for a specific store
    @Query("SELECT e.employee FROM Employment e WHERE e.store.id = :storeId")
    List<User> getEmployeeByStoreId(@Param("storeId") int storeId);

    // Get all stores for a specific employee
    @Query("SELECT e.store FROM Employment e WHERE e.employee.id = :employeeId")
    List<Store> getStoreByEmployeeId(Integer employeeId);

    boolean existsByStoreIdAndEmployeeId(int storeId, int employeeId);
}