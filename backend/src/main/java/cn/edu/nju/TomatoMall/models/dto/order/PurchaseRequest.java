package cn.edu.nju.TomatoMall.models.dto.order;

import cn.edu.nju.TomatoMall.enums.PaymentMethod;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PurchaseRequest {
    @NonNull
    private int productId;

    @NonNull
    private int quantity;

    private String name;
    private String phone;
    private String address;
    private String remark;
}
