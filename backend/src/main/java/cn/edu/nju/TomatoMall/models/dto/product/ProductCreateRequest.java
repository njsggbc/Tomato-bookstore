package cn.edu.nju.TomatoMall.models.dto.product;

import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NonNull
public class ProductCreateRequest {
    String title;
    String description;
    List<MultipartFile> images;
    BigDecimal price;
    int stock;
    int storeId;
    Map<String, String> specifications;
}
