package cn.darkjrong.storage.fdfs;

import cn.darkjrong.storage.utils.UUIDUtils;
import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.github.tobato.fastdfs.domain.fdfs.FileInfo;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.domain.fdfs.ThumbImageConfig;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Base64;

import static cn.darkjrong.storage.utils.FileUtils.*;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * FastDFS文件上传下载工具类
 * @author Rong.Jia
 * @date 2020/01/07 09:47
 */
public class FastDFSClient {

    private static final Logger logger = LoggerFactory.getLogger(FastDFSClient.class);

    @Autowired
    private FastFileStorageClient storageClient;

    @Autowired
    private ThumbImageConfig thumbImageConfig;

    @Autowired
    private FdfsWebServer fdfsWebServer;

    /**
     *  上传文件
     * @param file 文件对象
     * @param identifier 是否拼接全路径
     * @return String 文件路径
     * @throws IOException String
     */
    public String uploadFile(MultipartFile file, Boolean identifier) throws IOException {

        InputStream inputStream = file.getInputStream();
        StorePath storePath = storageClient.uploadFile(inputStream, file.getSize(),
                FilenameUtils.getExtension(file.getOriginalFilename()), null);
        IOUtils.closeQuietly(inputStream);
        return identifier ? getResAccessUrl(storePath) : storePath.getFullPath();
    }

    /**
     *  上传文件
     * @param bytes 文件数据
     * @param identifier 是否拼接全路径
     * @param format 文件格式（后缀）
     * @return String 文件路径
     */
    public String uploadFile(byte[] bytes, String format, Boolean identifier) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        StorePath storePath = storageClient.uploadFile(inputStream, bytes.length, format, null);
        IOUtils.closeQuietly(inputStream);
        return identifier ? getResAccessUrl(storePath) : storePath.getFullPath();
    }

    /**
     *  上传文件
     * @param file 文件对象
     * @param identifier 是否拼接全路径
     * @return String 文件路径
     * @throws IOException String
     */
    public String uploadFile(File file, Boolean identifier) throws IOException {

        FileInputStream fileInputStream = FileUtils.openInputStream(file);
        StorePath storePath = storageClient.uploadFile(fileInputStream, file.length(),
                FilenameUtils.getExtension(file.getName()), null);
        IOUtils.closeQuietly(fileInputStream);
        return  identifier ? getResAccessUrl(storePath) : storePath.getFullPath();
    }

    /**
     *  上传文件
     * @param base64 base64
     * @param identifier 是否拼接全路径
     * @return String 文件路径
     * @throws IOException String
     */
    public String uploadFile(String base64, Boolean identifier) throws IOException {

        base64 = base64.replaceAll(BASE64_REG, StringUtils.EMPTY);

        String filename = UUIDUtils.uuid() + System.currentTimeMillis() + JPEG_SUFFIX;

        // 判断文件夹是否存在
        String dirPath = TMP_DIR;
        cn.darkjrong.storage.utils.FileUtils.mkdirs(dirPath, Boolean.FALSE);

        String filePath = dirPath + File.separator + filename;

        File file = Files.write(Paths.get(filePath), Base64.getDecoder().decode(base64), StandardOpenOption.CREATE).toFile();

        FileInputStream fileInputStream = FileUtils.openInputStream(file);
        StorePath storePath = storageClient.uploadFile(fileInputStream, file.length(),
                FilenameUtils.getExtension(file.getName()), null);

        IOUtils.closeQuietly(fileInputStream);
        FileUtils.deleteQuietly(file);

        return  identifier ? getResAccessUrl(storePath) : storePath.getFullPath();
    }

    /**
     *  把字符串作为指定格式的文件上传
     * @param content 字符串
     * @param identifier 是否拼接全路径
     * @param fileExtension 文件格式
     * @return String 文件路径
     */
    public String uploadFile(String content, String fileExtension, Boolean identifier) {
        byte[] buff = content.getBytes(UTF_8);
        ByteArrayInputStream stream = new ByteArrayInputStream(buff);
        StorePath storePath = storageClient.uploadFile(stream, buff.length, fileExtension, null);
        IOUtils.closeQuietly(stream);
        return identifier ? getResAccessUrl(storePath) : storePath.getFullPath();
    }

    /**
     *  上传文件
     * @param file 文件对象
     * @param identifier 是否拼接全路径
     * @return String 文件路径
     * @throws IOException String
     */
    public String uploadImageAndCrtThumbImage(MultipartFile file, Boolean identifier) throws IOException {

        InputStream inputStream = file.getInputStream();
        StorePath storePath = storageClient.uploadImageAndCrtThumbImage(inputStream, file.getSize(),
                FilenameUtils.getExtension(file.getOriginalFilename()), null);
        IOUtils.closeQuietly(inputStream);
        return identifier ? getResAccessUrl(storePath) : storePath.getFullPath();
    }

    /**
     *  封装图片完整URL地址
     * @param storePath  文件路径
     * @return String 完成文件路径
     */
    private String getResAccessUrl(StorePath storePath) {

        return HTTP_PREFIX + fdfsWebServer.getWebServerUrl() + SLASH +  storePath.getFullPath();
    }
    /**
     *  封装图片完整URL地址
     * @param fullPath  文件路径
     * @return String 完成文件路径
     */
    public String getResAccessUrl(String fullPath) {

        return  HTTP_PREFIX + fdfsWebServer.getWebServerUrl() + SLASH +  fullPath;
    }

    /**
     *  根据图片路径获取缩略图路径（使用uploadImageAndCrtThumbImage方法上传图片）
     * @param filePath 图片路径
     * @return String 缩略图路径
     */
    public String getThumbImagePath(String filePath) {
        return thumbImageConfig.getThumbImagePath(filePath);
    }

    /**
     *  根据文件路径下载文件
     * @param filePath 文件路径
     * @return byte[] 文件字节数据
     */
    public byte[] downFile(String filePath) throws IOException {
        StorePath storePath = StorePath.parseFromUrl(filePath);
        return storageClient.downloadFile(storePath.getGroup(), storePath.getPath(), org.apache.commons.io.IOUtils::toByteArray);
    }

    /**
     *  根据文件地址删除文件
     * @param filePath 文件访问地址
     * @return true/false 成功/失败
     */
    public Boolean deleteFile(String filePath) {
        try {
            filePath = StringUtils.replace(filePath, HTTP_PREFIX + fdfsWebServer.getWebServerUrl() + SLASH, StringUtils.EMPTY);
            StorePath storePath = StorePath.parseFromUrl(filePath);
            storageClient.deleteFile(storePath.getGroup(), storePath.getPath());
            return Boolean.TRUE;
        }catch (Exception e) {
            logger.error("Delete file failed {}", e.getMessage());
        }
        return Boolean.FALSE;
    }

    /**
     *  查询文件信息
     * @param filePath 件访问地址
     * @return 文件信息
     */
    public FileInfo findFileInfo(String filePath){
        StorePath storePath = StorePath.parseFromUrl(filePath);
        return storageClient.queryFileInfo(storePath.getGroup(), storePath.getPath());
    }


}
