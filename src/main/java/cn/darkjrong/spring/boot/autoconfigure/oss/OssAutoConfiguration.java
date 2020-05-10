package cn.darkjrong.spring.boot.autoconfigure.oss;

import cn.darkjrong.storage.oss.OssClient;
import com.aliyun.oss.OSS;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云oss 自动配置
 * @date 2019/10/22 09:06:22
 * @author rong.jia
 */
@Configuration
@ConditionalOnClass({OssProperties.class})
@EnableConfigurationProperties(OssProperties.class)
@ConditionalOnProperty(prefix = "aliyun.oss", name = "enabled", havingValue = "true")
public class OssAutoConfiguration {

    private final OssProperties properties;

    public OssAutoConfiguration(final OssProperties properties) {
        this.properties = properties;
    }

    @Bean
    public OssClientFactoryBean ossClientFactoryBean() {
        final OssClientFactoryBean factoryBean = new OssClientFactoryBean();
        factoryBean.setProperties(this.properties);
        return factoryBean;
    }

    @Bean
    public OssClient ossClient() {

        return new OssClient(properties.getEndpoint(), properties.getIntranet(), properties.getOpenIntranet());
    }

}
