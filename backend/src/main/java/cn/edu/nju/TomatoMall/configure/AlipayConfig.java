package cn.edu.nju.TomatoMall.configure;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.CertAlipayRequest;
import com.alipay.api.DefaultAlipayClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties(prefix = "alipay")
@Data
public class AlipayConfig {
    private String appId;
    private String privateKey;      // 应用私钥（无需BEGIN/END标记）
    private String alipayPublicKey; // 支付宝公钥
    private String gateway;
    private String notifyUrl;
    private String returnUrl;
    private String signType = "RSA2";
    private String charset = "UTF-8";

    @Bean
    public AlipayClient alipayClient() {
        // 使用CertAlipayRequest处理密钥（避免格式问题）
        CertAlipayRequest request = new CertAlipayRequest();
        request.setServerUrl(gateway);
        request.setAppId(appId);
        request.setPrivateKey(privateKey);
        request.setFormat("json");
        request.setCharset(charset);
        request.setSignType(signType);
        request.setCertPath(null); // 沙箱无需证书
        request.setAlipayPublicCertContent(alipayPublicKey);

        try {
            return new DefaultAlipayClient(request);
        } catch (AlipayApiException e) {
            throw new RuntimeException("支付宝客户端初始化失败", e);
        }
    }
}