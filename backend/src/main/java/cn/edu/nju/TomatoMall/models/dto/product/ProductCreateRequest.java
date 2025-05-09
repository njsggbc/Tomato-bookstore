package cn.edu.nju.TomatoMall.models.dto.product;

import lombok.Data;
import lombok.NonNull;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class ProductCreateRequest {
    @NotBlank
    String title;
    @NotBlank
    String description;
    @NotNull
    @Size(min = 1)
    List<MultipartFile> images;
    @NotNull
    BigDecimal price;
    @NotNull
    Integer storeId;
    @NotNull
    @Size(min = 1)
    Map<String, String> specifications;
}
