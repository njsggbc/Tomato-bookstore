package cn.edu.nju.TomatoMall.models.dto.store;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class StoreUpdateRequest {
    String name;
    String address;
    String description;
    MultipartFile logo;
    List<MultipartFile> qualification;
}
