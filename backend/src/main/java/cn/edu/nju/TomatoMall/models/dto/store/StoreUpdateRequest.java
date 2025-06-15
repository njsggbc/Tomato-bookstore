package cn.edu.nju.TomatoMall.models.dto.store;

import cn.edu.nju.TomatoMall.enums.PaymentMethod;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class StoreUpdateRequest {
    private String name;
    private String address;
    private String description;
    private MultipartFile logo;
    private List<MultipartFile> qualification;
    private Map<String, String> merchantAccounts;

    public Map<PaymentMethod, String> getMerchantAccounts() {
        return merchantAccounts.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> PaymentMethod.valueOf(entry.getKey()),
                        Map.Entry::getValue
                ));
    }
}
