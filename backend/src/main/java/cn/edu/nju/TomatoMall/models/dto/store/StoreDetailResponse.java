package cn.edu.nju.TomatoMall.models.dto.store;

import cn.edu.nju.TomatoMall.models.dto.product.ProductBriefResponse;
import cn.edu.nju.TomatoMall.models.dto.user.UserBriefResponse;
import cn.edu.nju.TomatoMall.models.po.Store;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class StoreDetailResponse {
    int id;
    String name;
    String description;
    String address;
    String logoUrl;
    String regTime;
    String status;
    Integer score;
    UserBriefResponse manager;
    List<UserBriefResponse> staffs;
    List<ProductBriefResponse> products;

    public StoreDetailResponse(Store store) {
        this.id = store.getId();
        this.name = store.getName();
        this.description = store.getDescription();
        this.address = store.getAddress();
        this.logoUrl = store.getLogoUrl();
        this.regTime = store.getCreateTime().toString();
        this.status = store.getStatus().toString();
        this.score = store.getScore();
        this.manager = new UserBriefResponse(store.getManager());
        this.staffs = store.getStaffs().stream().map(UserBriefResponse::new).collect(Collectors.toList());
        this.products = store.getProducts().stream().map(ProductBriefResponse::new).collect(Collectors.toList());
    }
}
