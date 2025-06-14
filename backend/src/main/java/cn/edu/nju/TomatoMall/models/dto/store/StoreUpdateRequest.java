package cn.edu.nju.TomatoMall.models.dto.store;

import cn.edu.nju.TomatoMall.enums.PaymentMethod;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Data
public class StoreUpdateRequest {
    String name;
    String address;
    String description;
    MultipartFile logo;
    List<MultipartFile> qualification;
    Map<PaymentMethod, String> merchantAccounts;
}
