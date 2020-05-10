package cn.darkjrong.storage.utils;

import java.util.UUID;

/**
 * uuid 工具类
 * @author Rong.Jia
 * @date 2020/01/03 17:24
 */
public class UUIDUtils {

    /**
     * 生成UUID
     * @date 2019/02/14 08:40:22
     * @author Rong.Jia
     * @return String UUID
     */
    public static String uuid(){

        String uuid = UUID.randomUUID().toString();

        //去掉“-”符号
        return uuid.replaceAll("-", "");
    }

}
