package cn.edu.nju.TomatoMall.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import cn.edu.nju.TomatoMall.exception.TomatoMallException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URI;
import java.util.UUID;

@Component
@Getter
@Setter
@NoArgsConstructor
@ConfigurationProperties("aliyun.oss")
public class FileUtil {
    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    public String upload(int userId, MultipartFile file) {
        String objectName = userId + "/" + UUID.randomUUID().toString() + '.' + file.getOriginalFilename().split("\\.")[1];
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, file.getInputStream());
            ossClient.putObject(putObjectRequest);
        } catch (Exception e) {
            throw TomatoMallException.fileUploadFail();
        } finally {
            ossClient.shutdown();
        }
        return "https://" + bucketName + "." + endpoint + "/" + objectName;
    }

    public InputStream get(String url) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            URI uri = new URI(url);
            String objectName = uri.getPath().substring(1);
            return ossClient.getObject(bucketName, objectName).getObjectContent();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ossClient.shutdown();
        }
        return null;
    }

    public void delete(String url) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            URI uri = new URI(url);
            String objectName = uri.getPath().substring(1);
            ossClient.deleteObject(bucketName, objectName);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ossClient.shutdown();
        }
    }
}
