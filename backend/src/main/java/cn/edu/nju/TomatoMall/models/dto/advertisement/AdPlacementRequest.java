package cn.edu.nju.TomatoMall.models.dto.advertisement;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class AdPlacementRequest {
    @NotNull
    private Integer adId;
    @NotNull
    private Integer adSpaceId;
    @NotNull
    @Size(min = 1)
    private List<Integer> adSlotIds;
}
