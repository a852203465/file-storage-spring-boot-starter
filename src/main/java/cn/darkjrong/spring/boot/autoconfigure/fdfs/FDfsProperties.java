package cn.darkjrong.spring.boot.autoconfigure.fdfs;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Fast DFS  配置类
 * @author Rong.Jia
 * @date 2019/10/17 00:23
 */
@ConfigurationProperties(prefix = "fdfs")
public class FDfsProperties {

    private static final String ENABLED = "false";

    /**
     * 是否开启 Fast DFS
     */
    private String enabled = ENABLED;

    public String getEnabled() {
        return enabled;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }
}
