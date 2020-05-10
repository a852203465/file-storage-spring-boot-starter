package cn.darkjrong.spring.boot.autoconfigure.oss;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 阿里云oss 配置文件
 * @date 2019/10/22 09:06:22
 * @author rong.jia
 */
@ConfigurationProperties(prefix = "aliyun.oss")
public class OssProperties {

    private static final String ENABLED = "false";

    /**
     *  外网域名
     */
    private String endpoint;

    /**
     *  ak
     */
    private String accessKeyId;

    /**
     *   aks
     */
    private String accessKeySecret;

    /**
     *   内外地址
     */
    private String intranet;

    /**
     *  是否使用内网模式上传    1: open，close: 0
     */
    private Integer openIntranet;

    /**
     * 是否开启  OSS
     */
    private String enabled = ENABLED;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public String getIntranet() {
        return intranet;
    }

    public void setIntranet(String intranet) {
        this.intranet = intranet;
    }

    public Integer getOpenIntranet() {
        return openIntranet;
    }

    public void setOpenIntranet(Integer openIntranet) {
        this.openIntranet = openIntranet;
    }

    public String getEnabled() {
        return enabled;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }
}
