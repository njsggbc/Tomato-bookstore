package cn.edu.nju.TomatoMall.models.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CheckoutResponse {
    private int cartItemId;
    private boolean isAvailable;
}
