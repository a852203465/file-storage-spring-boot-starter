package cn.darkjrong.spring.boot.autoconfigure.fdfs;

import cn.darkjrong.storage.fdfs.FastDFSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Fast DFS 工厂类
 * @author Rong.Jia
 * @date 2019/10/17 00:31
 */
public class FDfsFactoryBean implements FactoryBean<FastDFSClient>, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(FDfsFactoryBean.class);

    private FastDFSClient fastDFSClient;

    @Override
    public FastDFSClient getObject() throws Exception {
        return this.fastDFSClient;
    }

    @Override
    public Class<?> getObjectType() {
        return FastDFSClient.class;
    }

    @Override
    public boolean isSingleton() {
        return Boolean.TRUE;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.fastDFSClient = new FastDFSClient();
    }


}
