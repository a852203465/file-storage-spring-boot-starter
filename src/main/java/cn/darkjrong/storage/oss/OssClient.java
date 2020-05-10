package cn.darkjrong.storage.oss;

import cn.darkjrong.storage.oss.domain.ObjectMetaInfo;
import cn.darkjrong.storage.utils.UUIDUtils;
import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.common.comm.ResponseMessage;
import com.aliyun.oss.common.utils.IOUtils;
import com.aliyun.oss.event.ProgressEvent;
import com.aliyun.oss.event.ProgressEventType;
import com.aliyun.oss.event.ProgressListener;
import com.aliyun.oss.model.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import static cn.darkjrong.storage.utils.FileUtils.*;

/**
 * oss 工具类
 *
 * @author Rong.Jia
 * @date 2019/10/21 18:34
 */
public class OssClient {

    private static final Logger log = LoggerFactory.getLogger(OssClient.class);

    private String endpoint;
    private String intranet;

    /**
     *  1: open，close: 0
     */
    private Integer openIntranet;

    @Autowired
    private OSS oss;

    public OssClient(String endpoint, String intranet, Integer openIntranet) {

        endpoint = StringUtils.replace(endpoint, HTTP_PREFIX, StringUtils.EMPTY);
        endpoint = StringUtils.replace(endpoint, HTTPS_PREFIX, StringUtils.EMPTY);

        this.endpoint = endpoint;
        this.openIntranet = openIntranet;

        intranet = StringUtils.replace(intranet, HTTP_PREFIX, StringUtils.EMPTY);
        intranet = StringUtils.replace(intranet, HTTPS_PREFIX, StringUtils.EMPTY);

        this.intranet = intranet;
    }

    /**
     * 创建存储空间
     *
     * @param bucketName 存储空间名
     * @return true/false
     * @date 2019/10/21 18:37:22
     */
    public Boolean createBucket(String bucketName) {

        Boolean flag = Boolean.FALSE;

        try {
            boolean bucketExist = oss.doesBucketExist(bucketName);
            if (!bucketExist) {
                oss.createBucket(bucketName);
                flag = Boolean.TRUE;
            }else {
                flag = bucketExist;
            }
        } catch (Exception e) {
            log.error("createBucket {}", e.getMessage());
        }

        return flag;
    }

    /**
     * 列举指定前缀的存储空间
     *
     * @param bucketPrefix 存储空间 前缀
     */
    public List<Bucket> listBucketByPrefix(String bucketPrefix) {
        ListBucketsRequest listBucketsRequest = new ListBucketsRequest();

        // 列举指定前缀的存储空间。
        if (!StringUtils.isEmpty(bucketPrefix)) {
            listBucketsRequest.setPrefix(bucketPrefix);
        }

        try {
            return oss.listBuckets(listBucketsRequest).getBucketList();
        } catch (Exception e) {

            log.error("listBucketByPrefix {}", e.getMessage());
            return null;
        }
    }

    /**
     * 列举存储空间
     */
    public List<Bucket> listBucket() {

        try {
            return oss.listBuckets();
        } catch (Exception e) {

            log.error("listBucket {}", e.getMessage());
            return null;
        }
    }

    /**
     * 列举指定marker之后的存储空间
     *
     * @param bucketMarker 存储空间名称
     */
    public List<Bucket> listBucketByMarker(String bucketMarker) {
        ListBucketsRequest listBucketsRequest = new ListBucketsRequest();

        // 列举指定marker之后的存储空间
        if (!StringUtils.isEmpty(bucketMarker)) {
            listBucketsRequest.setMarker(bucketMarker);
        }

        try {
            return oss.listBuckets(listBucketsRequest).getBucketList();
        } catch (Exception e) {

            log.error("listBucketByMarker {}", e.getMessage());
            return null;
        }
    }

    /**
     * 列举指定个数的存储空间
     *
     * @param maxKeys 指定个数
     */
    public List<Bucket> listBucketByMaxKeys(int maxKeys) {

        ListBucketsRequest listBucketsRequest = new ListBucketsRequest();

        // 列举指定marker之后的存储空间
        listBucketsRequest.setMaxKeys(maxKeys);

        try {
            return oss.listBuckets(listBucketsRequest).getBucketList();
        } catch (Exception e) {

            log.error("listBucketByMaxKeys {}", e.getMessage());
            return null;
        }
    }

    /**
     * 判断存储空间是否存在
     *
     * @param bucketName 存储空间
     */
    public Boolean doesBucketExist(String bucketName) {

        Boolean flag = Boolean.FALSE;

        try {
            flag = oss.doesBucketExist(bucketName);
        } catch (Exception e) {
            log.error("doesBucketExist {}", e.getMessage());
        }

        return flag;
    }

    /**
     * 获取存储空间信息
     *
     * @param bucketName
     */
    public cn.darkjrong.storage.oss.domain.BucketInfo getBucketInfo(String bucketName) {

        //判断空间是否存在
        boolean exists = oss.doesBucketExist(bucketName);
        if (!exists) {
            return null;
        }

        try {
            cn.darkjrong.storage.oss.domain.BucketInfo bucketInfo = new cn.darkjrong.storage.oss.domain.BucketInfo();

            BucketInfo info = oss.getBucketInfo(bucketName);

            // 获取地域。
            bucketInfo.setLocation(info.getBucket().getLocation());

            // 获取创建日期。
            bucketInfo.setCreateTime(new SimpleDateFormat("yyyy-MM-dd HH:ss").format(info.getBucket().getCreationDate()));

            // 获取拥有者信息。
            bucketInfo.setOwner(info.getBucket().getOwner());

            // 获取权限信息。
            AccessControlList acl = oss.getBucketAcl(bucketName);
            bucketInfo.setAuthority(acl.toString());

            return bucketInfo;
        } catch (Exception e) {
            log.error("getBucketInfo {}", e.getMessage());
        }

        return null;
    }

    /**
     * 删除存储空间
     *
     * @param bucketName
     */
    public Boolean deleteBucket(String bucketName) {

        Boolean flag = Boolean.FALSE;

        try {

            //判断空间是否存在
            boolean exists = oss.doesBucketExist(bucketName);
            if (exists) {
                // 删除存储空间。
                oss.deleteBucket(bucketName);
            }
            flag = exists;
        } catch (Exception e) {
            log.error("deleteBucket {}", e.getMessage());
        }

        return flag;
    }

    /**
     * 设置存储空间的访问权限
     *
     * @param bucketAcl  访问权限
     * @param bucketName 存储空间
     */
    public Boolean setBucketAcl(String bucketName, CannedAccessControlList bucketAcl) {

        Boolean flag = Boolean.FALSE;

        try {
            oss.setBucketAcl(bucketName, bucketAcl);
            flag = Boolean.TRUE;
        } catch (Exception e) {
            log.error("setBucketAcl {}", e.getMessage());
        }

        return flag;
    }

    /**
     * 获取存储空间的访问权限
     *
     * @param bucketName 存储空间
     */
    public String getBucketAcl(String bucketName) {

        if (StringUtils.isEmpty(bucketName)) {
            return null;
        }

        try {
            AccessControlList bucketAcl = oss.getBucketAcl(bucketName);
            return bucketAcl.toString();
        } catch (Exception e) {

            log.error("getBucketAcl {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取存储空间的地域
     *
     * @param bucketName 存储空间
     */
    public String getBucketLocation(String bucketName) {

        if (StringUtils.isEmpty(bucketName)) {
            return null;
        }

        try {
            return oss.getBucketLocation(bucketName);
        } catch (Exception e) {

            log.error("getBucketLocation {}", e.getMessage());
            return null;
        }
    }

    /**
     *  判断文件是否存在
     * @param objectName 文件
     * @param bucketName 储存 空间
     * @return Boolean true/false
     */
    public Boolean doesObjectExist(String bucketName, String objectName){

        try {
            return oss.doesObjectExist(bucketName, objectName);
        }catch (Exception e) {
            log.error("doesObjectExist {}", e.getMessage());
        }

        return Boolean.FALSE;
    }

    /**
     * 上传文件
     *
     * @param file       待上传文件
     * @param bucketName 存储空间
     * @param dir 目录
     * @return 文件上传完整路径
     * @throws IOException 文件上传异常
     */
    public String upload(MultipartFile file, String bucketName, String dir) throws IOException {

        String putName =dir + SLASH + UUIDUtils.uuid() + System.currentTimeMillis() + JPEG_SUFFIX;

        // 上传文件流。
        InputStream inputStream = file.getInputStream();
        PutObjectResult putObject = oss.putObject(bucketName, putName, inputStream);

        ResponseMessage response = putObject.getResponse();

        if (!ObjectUtils.isEmpty(response) && !StringUtils.isEmpty(response.getErrorResponseAsString())) {
            return null;
        }

        IOUtils.safeClose(inputStream);
        return putName;
    }

    /**
     * 合并文件(主要用于文本对象)
     *
     * @param bucketName
     * @param contentType
     * @param content
     * @return
     */
    public String mergeContent(String bucketName, String contentType, InputStream... content) {

        ObjectMetadata meta = new ObjectMetadata();

        // 指定上传的内容类型。
        meta.setContentType(contentType);

        //设置文件名
        String fileName = UUIDUtils.uuid() + System.currentTimeMillis();

        AppendObjectRequest appendObjectRequest = new AppendObjectRequest(bucketName, fileName, content[0], meta);

        AppendObjectResult objectResult = null;
        try {

            for (int i = 0; i < content.length; i++) {
                if (i == 0) {
                    appendObjectRequest.setPosition(0L);
                    objectResult = oss.appendObject(appendObjectRequest);
                } else {
                    appendObjectRequest.setPosition(objectResult.getNextPosition());
                    appendObjectRequest.setInputStream(content[i]);
                    oss.appendObject(appendObjectRequest);
                }
            }

            return fileName;
        } catch (OSSException e) {
            log.error("mergeContent {}", e.getMessage());
            return null;
        }
    }

    /**
     * 分片上传
     *
     * @param bucketName
     * @param file
     * @return
     * @throws IOException
     */
    public String shardUpload(String bucketName, MultipartFile file) throws IOException {

        String putName = file.getOriginalFilename() + "_" + UUIDUtils.uuid() + System.currentTimeMillis();

        //初始化一个分片上传事件。
        InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucketName, putName);
        InitiateMultipartUploadResult result = oss.initiateMultipartUpload(request);

        // 返回uploadId，它是分片上传事件的唯一标识，您可以根据这个ID来发起相关的操作，如取消分片上传、查询分片上传等。
        String uploadId = result.getUploadId();

        //上传分片

        // partETags是PartETag的集合。PartETag由分片的ETag和分片号组成。
        List<PartETag> partETags = new ArrayList<>();

        // 计算文件有多少个分片。
        final long partSize = 1024 * 1024L;
        long fileLength = file.getSize();
        int partCount = (int) (fileLength / partSize);

        if (fileLength % partSize != 0) {
            partCount++;
        }

        // 遍历分片上传。
        for (int i = 0; i < partCount; i++) {

            long startPos = i * partSize;
            long curPartSize = (i + 1 == partCount) ? (fileLength - startPos) : partSize;

            InputStream instep = file.getInputStream();

            // 跳过已经上传的分片。
            instep.skip(startPos);

            UploadPartRequest uploadPartRequest = new UploadPartRequest();
            uploadPartRequest.setBucketName(bucketName);
            uploadPartRequest.setKey(putName);
            uploadPartRequest.setUploadId(uploadId);
            uploadPartRequest.setInputStream(instep);

            // 设置分片大小。除了最后一个分片没有大小限制，其他的分片最小为100KB。
            uploadPartRequest.setPartSize(curPartSize);

            // 设置分片号。每一个上传的分片都有一个分片号，取值范围是1~10000，如果超出这个范围，OSS将返回InvalidArgument的错误码。
            uploadPartRequest.setPartNumber(i + 1);

            // 每个分片不需要按顺序上传，甚至可以在不同客户端上传，OSS会按照分片号排序组成完整的文件。
            UploadPartResult uploadPartResult = oss.uploadPart(uploadPartRequest);

            // 每次上传分片之后，OSS的返回结果会包含一个PartETag。PartETag将被保存到partETags中。
            partETags.add(uploadPartResult.getPartETag());
        }

        //完成分片上传
        // 排序。partETags必须按分片号升序排列。
        partETags.sort(Comparator.comparingInt(PartETag::getPartNumber));

        // 在执行该操作时，需要提供所有有效的partETags。OSS收到提交的partETags后，会逐一验证每个分片的有效性。当所有的数据分片验证通过后，OSS将把这些分片组合成一个完整的文件。
        CompleteMultipartUploadRequest completeMultipartUploadRequest = new CompleteMultipartUploadRequest(bucketName, putName, uploadId, partETags);
        oss.completeMultipartUpload(completeMultipartUploadRequest);

        return putName;
    }

    /**
     * 带进度条上传
     *
     * @param bucketName 空间名
     * @param file 待上传文件
     * @return 上传完整路径
     */
    public String uploadWithProgress(String bucketName, MultipartFile file) throws IOException {

        String putName = file.getOriginalFilename() + "_" +UUIDUtils.uuid() + System.currentTimeMillis();

        InputStream inputStream = null;
        try {

            inputStream = file.getInputStream();
            oss.putObject(new PutObjectRequest(bucketName, putName, inputStream).withProgressListener(new PutObjectProgressListener()));
            return putName;
        } catch (OSSException e) {
            log.error("uploadWithProgress {}", e.getMessage());
            return null;
        }finally {
            IOUtils.safeClose(inputStream);
        }

    }

    /**
     *  上传文件
     * @param bucketName 存储空间
     * @param file 待上传文件
     * @return 文件存储路径
     * @throws FileNotFoundException
     */
    public String upload(String bucketName, String dir, File file) throws FileNotFoundException {

        if (StringUtils.isEmpty(bucketName)) {
            return null;
        }

        if (!file.exists()) {
            throw new FileNotFoundException();
        }

        try {

            String objectName = dir + SLASH + UUIDUtils.uuid() + System.currentTimeMillis() + JPEG_SUFFIX;
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, file);
            oss.putObject(putObjectRequest);

            return objectName;
        }catch (Exception e){
            log.error("upload {}", e.getMessage());
            return null;
        }

    }

    /**
     * 获得url链接
     * @param objectName 对象名称
     * @param bucketName 存储空间
     * @return
     */
    public String getUrl(String bucketName, String objectName) {

        return getUrl(bucketName, objectName, 7200L * 1000);
    }

    /**
     * 获得url链接
     * @param objectName 对象名称
     * @param bucketName 存储空间
     * @param expirationTime 超时时间 单位毫秒
     * @return
     */
    public String getUrl(String bucketName, String objectName, Long expirationTime) {

        Date expiration = new Date(System.currentTimeMillis() + expirationTime);

        GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest(bucketName, objectName, HttpMethod.GET);

        req.setExpiration(expiration);

        try {
            String signedUrl = oss.generatePresignedUrl(req).toString();

            if (null != openIntranet && openIntranet == 1) {

                signedUrl = StringUtils.replace(signedUrl, intranet, endpoint);
            }

            return signedUrl;
        }catch (Exception e){
            log.error("getUrl {}", e.getMessage());
        }

        return null;
    }

    /**
     *  上传文件
     * @param bucketName 存储空间
     * @param inputStream 输入流
     * @param fileName 文件名
     * @return 文件存储路径
     * @throws FileNotFoundException
     */
    public String upload(String bucketName, String fileName, String dir, InputStream inputStream) {

        if (StringUtils.isEmpty(bucketName)) {
            return null;
        }

        try {

            String objectName = dir + "/" + fileName;
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, inputStream);
            oss.putObject(putObjectRequest);

            return objectName;
        }catch (Exception e){
            log.error("upload {}", e.getMessage());
            return null;
        }finally {
            IOUtils.safeClose(inputStream);
        }

    }

    /**
     *  下载文件
     * @param bucketName 存储空间名
     * @param objectName 对象名
     * @return
     */
    public byte[] download(String bucketName, String objectName){

        if (StringUtils.isEmpty(bucketName) || StringUtils.isEmpty(objectName)) {
            return null;
        }

        try {
            OSSObject ossObject = oss.getObject(bucketName, objectName);

           return IOUtils.readStreamAsByteArray(new BufferedInputStream(ossObject.getObjectContent()));
        }catch (Exception e){
            log.error("download {}", e.getMessage());
            return null;
        }
    }

    /**
     *  下载文件
     * @param bucketName 存储空间名
     * @param objectName 对象名
     * @param localFileName 本地文件名
     * @return
     */
    public File download(String bucketName, String objectName, String localFileName){

        if (StringUtils.isEmpty(bucketName) || StringUtils.isEmpty(objectName) || StringUtils.isEmpty(localFileName)) {
            return null;
        }

        File file = new File(localFileName);

        try {
            oss.getObject(new GetObjectRequest(bucketName, objectName), file);
            return file;
        }catch (Exception e){
            log.error("download {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取文件信息
     *
     * @param bucketName 存储空间
     * @param objectName 对象名
     * @return
     */
    public Map<String, Object> getFileInfo(String bucketName, String objectName) {

        try {
            // 获取文件元信息。
            ObjectMetadata metadata = oss.getObjectMetadata(bucketName, objectName);
            return metadata.getRawMetadata();
        }catch (Exception e){
            log.error("getFileInfo {}", e.getMessage());
        }

        return null;
    }

    /**
     * 获取指定前缀的文件
     * @param bucketName
     * @param filePrefix
     * @return
     */
    public List<OSSObjectSummary> listFile(String bucketName, String filePrefix) {

        try {
            // 列举包含指定前缀的文件。默认列举100个文件。
            ObjectListing objectListing = oss.listObjects(new ListObjectsRequest(bucketName).withPrefix(filePrefix));
            return objectListing.getObjectSummaries();
        }catch (Exception e){
            log.error("listFile {}", e.getMessage());
        }
       return null;
    }

    /**
     * 删除文件
     *
     * @param bucketName 存储空间
     * @param objectName 对象名
     * @return
     */
    public Boolean delete(String bucketName, String objectName) {

        Boolean flag = Boolean.FALSE;

        try {
            // 删除文件。
            oss.deleteObject(bucketName, objectName);
            flag = Boolean.TRUE;
        } catch (OSSException e) {
            log.error("delete {}", e.getMessage());
        }

        return flag;
    }

    /**
     * 上传进程监控（进度条）
     */
    public static class PutObjectProgressListener implements ProgressListener {

        private long bytesWritten = 0;
        private long totalBytes = -1;
        private boolean succeed = false;

        @Override
        public void progressChanged(ProgressEvent progressEvent) {
            long bytes = progressEvent.getBytes();
            ProgressEventType eventType = progressEvent.getEventType();
            switch (eventType) {
                case TRANSFER_STARTED_EVENT:
                    log.info("Start to upload......");
                    break;
                case REQUEST_CONTENT_LENGTH_EVENT:
                    this.totalBytes = bytes;
                    log.info(this.totalBytes + " bytes in total will be uploaded to OSS");
                    break;
                case REQUEST_BYTE_TRANSFER_EVENT:
                    this.bytesWritten += bytes;
                    if (this.totalBytes != -1) {
                        int percent = (int) (this.bytesWritten * 100.0 / this.totalBytes);
                        log.info(bytes + " bytes have been written at this time, upload progress: " + percent + "%(" + this.bytesWritten + "/" + this.totalBytes + ")");
                    } else {
                        log.info(bytes + " bytes have been written at this time, upload ratio: unknown" + "(" + this.bytesWritten + "/...)");
                    }
                    break;
                case TRANSFER_COMPLETED_EVENT:
                    this.succeed = true;
                    log.info("Succeed to upload, " + this.bytesWritten + " bytes have been transferred in total");
                    break;
                case TRANSFER_FAILED_EVENT:
                    log.info("Failed to upload, " + this.bytesWritten + " bytes have been transferred");
                    break;
                default:
                    break;
            }
        }

        /**
         * 获取当前进程百分比
         *
         * @return
         */
        public int getProgress() {
            return (int) (this.bytesWritten * 100.0 / this.totalBytes);

        }

        public boolean isSucceed() {
            return succeed;
        }


        /**
         * 文件下载监控（进度条）
         */
        public static class GetObjectProgressListener implements ProgressListener {
            private long bytesRead = 0;
            private long totalBytes = -1;
            private boolean succeed = false;

            @Override
            public void progressChanged(ProgressEvent progressEvent) {
                long bytes = progressEvent.getBytes();
                ProgressEventType eventType = progressEvent.getEventType();
                switch (eventType) {
                    case TRANSFER_STARTED_EVENT:
                        System.out.println("Start to download......");
                        break;
                    case RESPONSE_CONTENT_LENGTH_EVENT:
                        this.totalBytes = bytes;
                        System.out.println(this.totalBytes + " bytes in total will be downloaded to a local file");
                        break;
                    case RESPONSE_BYTE_TRANSFER_EVENT:
                        this.bytesRead += bytes;
                        if (this.totalBytes != -1) {
                            int percent = (int) (this.bytesRead * 100.0 / this.totalBytes);
                            System.out.println(bytes + " bytes have been read at this time, download progress: " +
                                    percent + "%(" + this.bytesRead + "/" + this.totalBytes + ")");
                        } else {
                            System.out.println(bytes + " bytes have been read at this time, download ratio: unknown" +
                                    "(" + this.bytesRead + "/...)");
                        }
                        break;
                    case TRANSFER_COMPLETED_EVENT:
                        this.succeed = true;
                        System.out.println("Succeed to download, " + this.bytesRead + " bytes have been transferred in total");
                        break;
                    case TRANSFER_FAILED_EVENT:
                        System.out.println("Failed to download, " + this.bytesRead + " bytes have been transferred");
                        break;
                    default:
                        break;
                }
            }

            /**
             * 获取当前进程百分比
             *
             * @return
             */
            public int getProgress() {
                return (int) (this.bytesRead * 100.0 / this.totalBytes);

            }

            public boolean isSucceed() {
                return succeed;
            }
        }
    }

    /**
     *  获取文件全部元信息
     * @param  objectName 文件名
     * @param  bucketName 储存空间
     * @return 元信息
     */
    public ObjectMetaInfo getObjectMetadata(String bucketName, String objectName){

        ObjectMetadata objectMetadata = null;

        try {
            objectMetadata = oss.getObjectMetadata(bucketName, objectName);
        }catch (Exception e){
            log.error("File information retrieval failed {}", e.getMessage());
        }

        if (ObjectUtils.isEmpty(objectMetadata)) {
            return null;
        }

        ObjectMetaInfo objectMetaInfo = new ObjectMetaInfo();
        objectMetaInfo.setRequestId(objectMetadata.getRequestId());
        objectMetaInfo.setLastModified(objectMetadata.getLastModified().getTime());
        objectMetaInfo.setContentType(objectMetadata.getContentType());
        objectMetaInfo.setServerCrc(objectMetadata.getServerCRC());
        objectMetaInfo.setMd5(objectMetadata.getContentMD5());
        objectMetaInfo.setStorageClass(objectMetadata.getObjectStorageClass().toString());
        objectMetaInfo.setTag(objectMetadata.getETag());
        objectMetaInfo.setLength(objectMetadata.getContentLength());

        return objectMetaInfo;
    }

    /**
     *  获取文件部分元信息
     * @param  objectName 文件名
     * @param  bucketName 储存空间
     * @return 元信息
     */
    public ObjectMetaInfo getSimplifiedObjectMeta(String bucketName, String objectName){

        SimplifiedObjectMeta  simplifiedObjectMeta = null;

        try {
            simplifiedObjectMeta = oss.getSimplifiedObjectMeta(bucketName, objectName);
        }catch (Exception e){
            log.error("File information retrieval failed {}", e.getMessage());
        }

        if (ObjectUtils.isEmpty(simplifiedObjectMeta)) {
            return null;
        }

        ObjectMetaInfo objectMetaInfo = new ObjectMetaInfo();
        objectMetaInfo.setLastModified(simplifiedObjectMeta.getLastModified().getTime());
        objectMetaInfo.setTag(simplifiedObjectMeta.getETag());
        objectMetaInfo.setLength(simplifiedObjectMeta.getSize());

        return objectMetaInfo;
    }

}
