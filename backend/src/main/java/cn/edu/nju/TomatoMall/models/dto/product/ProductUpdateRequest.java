package cn.edu.nju.TomatoMall.models.dto.product;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ProductUpdateRequest {
    String title;
    String description;
    List<MultipartFile> images;
    Double price;
    Integer stock;
    Map<String, String> specifications;
}
