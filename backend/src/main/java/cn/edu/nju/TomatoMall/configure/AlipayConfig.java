package cn.edu.nju.TomatoMall.configure;

import com.alipay.api.AlipayClient;
import com.alipay.api.CertAlipayRequest;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.*;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "alipay")
@Data
@Slf4j
public class AlipayConfig {
    private String appId;
    private String gateway;
    private String notifyUrl;
    private String returnUrl;
    private String signType = "RSA2";
    private String charset = "UTF-8";
    private String appCertPath;
    private String alipayCertPath;
    private String alipayRootCertPath;
    private String privateKeyPath;
    private String encryptKey;

    // 缓存证书文件路径，避免重复创建临时文件
    private String cachedAppCertPath;
    private String cachedAlipayCertPath;
    private String cachedAlipayRootCertPath;
    private String cachedPrivateKey;
    private String cachedAlipayPublicKey;

    @Autowired
    private ResourceLoader resourceLoader;

    @Bean
    public AlipayClient alipayClient() {
        try {
            // 读取私钥文件内容
            String privateKey = getPrivateKey();

            // 创建证书模式请求对象
            CertAlipayRequest certAlipayRequest = new CertAlipayRequest();
            certAlipayRequest.setServerUrl(gateway);
            certAlipayRequest.setAppId(appId);
            certAlipayRequest.setPrivateKey(privateKey);
            certAlipayRequest.setFormat("json");
            certAlipayRequest.setCharset(charset);
            certAlipayRequest.setSignType(signType);

            // 设置证书路径
            certAlipayRequest.setCertPath(getAbsoluteAppCertPath());
            certAlipayRequest.setAlipayPublicCertPath(getAbsoluteAlipayCertPath());
            certAlipayRequest.setRootCertPath(getAbsoluteAlipayRootCertPath());

            // 设置加密参数
            if (encryptKey != null && !encryptKey.isEmpty()) {
                certAlipayRequest.setEncryptType("AES");
                certAlipayRequest.setEncryptor(encryptKey);
            }

            log.info("支付宝客户端初始化完成，使用证书模式");
            log.debug("应用证书路径: {}", certAlipayRequest.getCertPath());
            log.debug("支付宝公钥证书路径: {}", certAlipayRequest.getAlipayPublicCertPath());
            log.debug("支付宝根证书路径: {}", certAlipayRequest.getRootCertPath());

            // 创建AlipayClient
            return new DefaultAlipayClient(certAlipayRequest);
        } catch (Exception e) {
            log.error("支付宝客户端初始化失败", e);
            throw new RuntimeException("支付宝客户端初始化失败", e);
        }
    }

    /**
     * 验证支付宝异步通知签名
     * @param params 通知参数
     * @return 验证结果
     */
    public boolean verifySignature(Map<String, String> params) {
        try {
            // 记录关键参数用于调试
            log.debug("验证签名参数: sign={}, sign_type={}, timestamp={}",
                    params.get("sign"), params.get("sign_type"), params.get("timestamp"));

            // 使用证书验证签名
            String alipayPublicCertPath = getAbsoluteAlipayCertPath();
            log.debug("使用支付宝公钥证书路径: {}", alipayPublicCertPath);

            boolean verifyResult = AlipaySignature.rsaCertCheckV1(
                    params,
                    alipayPublicCertPath,
                    charset,
                    signType
            );

            log.debug("签名验证结果: {}", verifyResult);
            return verifyResult;
        } catch (Exception e) {
            log.error("签名验证失败", e);
            throw new RuntimeException("签名验证失败: " + e.getMessage(), e);
        }
    }

    // 获取私钥
    private String getPrivateKey() throws IOException {
        if (cachedPrivateKey == null) {
            cachedPrivateKey = readFile(privateKeyPath);
            log.debug("读取私钥成功，长度: {}", cachedPrivateKey.length());
        }
        return cachedPrivateKey;
    }

    // 获取应用证书的绝对路径
    private String getAbsoluteAppCertPath() throws IOException {
        if (cachedAppCertPath == null) {
            cachedAppCertPath = createAccessibleCertPath(appCertPath, "app_cert_");
        }
        return cachedAppCertPath;
    }

    // 获取支付宝公钥证书的绝对路径
    private String getAbsoluteAlipayCertPath() throws IOException {
        if (cachedAlipayCertPath == null) {
            cachedAlipayCertPath = createAccessibleCertPath(alipayCertPath, "alipay_cert_");
        }
        return cachedAlipayCertPath;
    }

    // 获取支付宝根证书的绝对路径
    private String getAbsoluteAlipayRootCertPath() throws IOException {
        if (cachedAlipayRootCertPath == null) {
            cachedAlipayRootCertPath = createAccessibleCertPath(alipayRootCertPath, "alipay_root_cert_");
        }
        return cachedAlipayRootCertPath;
    }

    // 创建可访问的证书路径
    private String createAccessibleCertPath(String resourcePath, String prefix) throws IOException {
        Resource resource = resourceLoader.getResource(resourcePath);
        log.debug("加载资源: {}, 是否存在: {}", resourcePath, resource.exists());

        // 如果是文件系统中的资源且可直接访问
        if (resource.isFile() && resource.getFile().exists()) {
            String path = resource.getFile().getAbsolutePath();
            log.debug("使用直接文件路径: {}", path);
            return path;
        }

        // 否则创建临时文件
        File tempFile = File.createTempFile(prefix, ".crt");
        tempFile.deleteOnExit();

        try (InputStream in = resource.getInputStream();
             FileOutputStream out = new FileOutputStream(tempFile)) {
            IOUtils.copy(in, out);
        }

        String path = tempFile.getAbsolutePath();
        log.debug("创建临时证书文件: {}", path);
        return path;
    }

    // 读取支付宝公钥（备用方法）
    public String getAlipayPublicKey() {
        if (cachedAlipayPublicKey == null) {
            try {
                Resource certResource = resourceLoader.getResource(alipayCertPath);
                try (InputStream inputStream = certResource.getInputStream()) {
                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
                    X509Certificate cert = (X509Certificate) cf.generateCertificate(inputStream);
                    PublicKey publicKey = cert.getPublicKey();
                    cachedAlipayPublicKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());
                    log.debug("提取的支付宝公钥: {}", cachedAlipayPublicKey);
                }
            } catch (Exception e) {
                log.error("读取支付宝公钥失败", e);
                throw new RuntimeException("读取支付宝公钥失败", e);
            }
        }
        return cachedAlipayPublicKey;
    }

    // 读取文件内容的辅助方法
    private String readFile(String path) throws IOException {
        Resource resource = resourceLoader.getResource(path);
        try (InputStream inputStream = resource.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString().trim();
        }
    }
}
