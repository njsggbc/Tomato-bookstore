package cn.edu.nju.TomatoMall.models.dto.advertisements;
import cn.edu.nju.TomatoMall.models.po.Advertisement;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Setter
@Getter
@NoArgsConstructor
public class AdvertisementBriefResponse {

    private Integer id;
    private String title;
    private String content;
    private String imageUrl;
    private Integer productId;

    // 构造方法
    public AdvertisementBriefResponse(Advertisement advertisement) {
        this.title = advertisement.getTitle();
        this.content = advertisement.getContent();
        this.imageUrl = advertisement.getImageUrls().get(0);
        this.productId = advertisement.getProductId();
    }


}
