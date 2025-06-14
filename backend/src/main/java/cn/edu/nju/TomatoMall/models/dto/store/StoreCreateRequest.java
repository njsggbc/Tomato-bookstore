package cn.edu.nju.TomatoMall.models.dto.store;

import cn.edu.nju.TomatoMall.enums.PaymentMethod;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Map;

@Data
public class StoreCreateRequest {
    @NotBlank
    String name;
    @NotBlank
    String address;
    @NotBlank
    String description;
    @NotNull
    MultipartFile logo;
    @NotNull
    @Size(min=1)
    List<MultipartFile> qualification;
    @NotNull
    Map<PaymentMethod, String> merchantAccounts;
}
