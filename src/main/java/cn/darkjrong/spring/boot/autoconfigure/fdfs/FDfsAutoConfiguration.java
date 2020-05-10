package cn.darkjrong.spring.boot.autoconfigure.fdfs;

import cn.darkjrong.storage.fdfs.FastDFSClient;
import com.github.tobato.fastdfs.FdfsClientConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Fast DFS 加载配置
 * @author Rong.Jia
 * @date 2019/10/17 00:27
 */
@Configuration
@Import(FdfsClientConfig.class)
@ConditionalOnClass({FDfsProperties.class})
@EnableConfigurationProperties({FDfsProperties.class})
@ConditionalOnProperty(prefix = "fdfs", name = "enabled", havingValue = "true")
public class FDfsAutoConfiguration {

    @Bean
    public FDfsFactoryBean fDfsFactoryBean() {
        return new FDfsFactoryBean();
    }

    @Bean
    public FastDFSClient fastDFSClient() {
        return new FastDFSClient();
    }


}
