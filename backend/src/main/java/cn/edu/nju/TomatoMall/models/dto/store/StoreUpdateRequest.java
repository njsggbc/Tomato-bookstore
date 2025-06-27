package cn.edu.nju.TomatoMall.models.dto.store;

import cn.edu.nju.TomatoMall.enums.PaymentMethod;
import cn.edu.nju.TomatoMall.util.JsonMapConverter;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class StoreUpdateRequest {
    private String name;
    private String description;
    private MultipartFile logo;
    private String address;
    private String merchantAccounts; // 序列化JSON字符串
    private List<MultipartFile> qualifications;

    public Map<PaymentMethod, String> getMerchantAccounts() {
        if (merchantAccounts == null || merchantAccounts.isEmpty()) {
            return null;
        }
        return JsonMapConverter.jsonToStringMap(merchantAccounts).entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> PaymentMethod.valueOf(entry.getKey()),
                        Map.Entry::getValue
                ));
    }
}
