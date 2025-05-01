package cn.edu.nju.TomatoMall.models.dto.store;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NonNull
@NoArgsConstructor
public class StoreCreateRequest {
    String name;
    String address;
    String description;
    MultipartFile logo;
    List<MultipartFile> qualification;
}
