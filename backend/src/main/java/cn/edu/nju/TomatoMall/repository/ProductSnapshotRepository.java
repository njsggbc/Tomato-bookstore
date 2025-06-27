package cn.edu.nju.TomatoMall.repository;

import cn.edu.nju.TomatoMall.models.po.ProductSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductSnapshotRepository extends JpaRepository<ProductSnapshot, Integer> {
    ProductSnapshot findById(int id);
}
