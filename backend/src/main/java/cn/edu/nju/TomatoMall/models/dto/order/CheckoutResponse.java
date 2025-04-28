package cn.edu.nju.TomatoMall.models.dto.order;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@NonNull
public class CheckoutResponse {
    int cartItemId;
    int availableQuantity;
}
