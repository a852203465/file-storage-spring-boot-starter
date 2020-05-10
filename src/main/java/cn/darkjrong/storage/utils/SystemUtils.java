package cn.darkjrong.storage.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 *  系统信息工具类
 * @author Rong.Jia
 * @date 2019/02/26 15:44:22
 */
public class SystemUtils {

    private static final Logger logger = LoggerFactory.getLogger(SystemUtils.class);

    /**
     * 操作系统的名称
     */
    public static final String SYSTEM_ENVIRONMENT = "os.name";

    /**
     * linux 系统
     */
    public static final String LINUX_SYSTEM = "Linux";

    /**
     *  判断运行环境是linux还是windows
     * @author Rong.Jia
     * @date 2019/01/23 9:19
     * @return false/true windows/linux
     */
    public static Boolean judgeSystem(){

        Properties prop = System.getProperties();
        String os = prop.getProperty(SYSTEM_ENVIRONMENT);
        if (StringUtils.isNoneBlank(os) && LINUX_SYSTEM.equalsIgnoreCase(os)) {
            return true;
        } else {
            return false;
        }
    }

}
