package cn.edu.nju.TomatoMall.models.dto.store;

import cn.edu.nju.TomatoMall.enums.PaymentMethod;
import cn.edu.nju.TomatoMall.util.JsonMapConverter;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class StoreCreateRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String description;
    @NotNull
    MultipartFile logo;
    @NotBlank
    private String address;
    @NotBlank
    private String merchantAccounts; // 序列化JSON字符串
    @NotNull
    @Size(min = 1)
    private List<MultipartFile> qualifications;

    public Map<PaymentMethod, String> getMerchantAccounts() {
        return JsonMapConverter.jsonToStringMap(merchantAccounts).entrySet()
                .stream()
                .collect(Collectors.toMap(entry ->
                                PaymentMethod.valueOf(entry.getKey()), Map.Entry::getValue
        ));
    }
}
