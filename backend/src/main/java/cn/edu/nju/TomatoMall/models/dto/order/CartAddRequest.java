package cn.edu.nju.TomatoMall.models.dto.order;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@NonNull
@NoArgsConstructor
public class CartAddRequest {
    private int productId;
    private int quantity;
}
