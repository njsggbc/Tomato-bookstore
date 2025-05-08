package cn.edu.nju.TomatoMall.models.dto.order;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Map;

@Data
public class SubmitRequest {
    @NotNull
    @Size(min=1)
    private List<Integer> cartItemIds;

    private String recipientName;
    private String recipientPhone;
    private String recipientAddress;
    private Map<Integer, String> storeRemarks;
}
