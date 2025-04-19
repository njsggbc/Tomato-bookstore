package cn.edu.nju.TomatoMall.models.dto.order;

import cn.edu.nju.TomatoMall.enums.PaymentMethod;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CheckOutRequest {
    @NonNull
    List<Integer> cartItemIds;

    String name;
    String phone;
    String address;
    String remark;
}
