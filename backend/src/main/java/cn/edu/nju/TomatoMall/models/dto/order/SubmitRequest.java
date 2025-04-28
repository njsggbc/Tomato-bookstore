package cn.edu.nju.TomatoMall.models.dto.order;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class SubmitRequest {
    @NonNull
    private List<Integer> cartItemIds;
    @NonNull
    private String recipientName;
    @NonNull
    private String recipientPhone;
    @NonNull
    private String recipientAddress;
    private Map<Integer, String> storeRemarks;
}
