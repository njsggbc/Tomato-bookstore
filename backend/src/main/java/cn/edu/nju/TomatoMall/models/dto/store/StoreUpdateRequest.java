package cn.edu.nju.TomatoMall.models.dto.store;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class StoreUpdateRequest {
    String name;
    String address;
    String description;
    MultipartFile logo;
    List<MultipartFile> qualification;
}
