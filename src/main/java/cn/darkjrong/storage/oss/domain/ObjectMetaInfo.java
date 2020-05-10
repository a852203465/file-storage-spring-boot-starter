package cn.darkjrong.storage.oss.domain;

import java.io.Serializable;
import java.util.Objects;

/**
 * 文件的元信息
 * @author Rong.Jia
 * @date 2019/10/31 14:49
 */
public class ObjectMetaInfo implements Serializable {

    private static final long serialVersionUID = 3537683307404044923L;

    private String requestId;

    private Long lastModified;

    private String encryption;

    private String md5;

    private String storageClass;

    private String tag;

    private Long serverCrc;

    private Long length;

    private String contentType;

    public ObjectMetaInfo() {
    }

    public ObjectMetaInfo(String requestId, Long lastModified, String encryption, String md5, String storageClass, String tag, Long serverCrc, Long length, String contentType) {
        this.requestId = requestId;
        this.lastModified = lastModified;
        this.encryption = encryption;
        this.md5 = md5;
        this.storageClass = storageClass;
        this.tag = tag;
        this.serverCrc = serverCrc;
        this.length = length;
        this.contentType = contentType;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Long getLastModified() {
        return lastModified;
    }

    public void setLastModified(Long lastModified) {
        this.lastModified = lastModified;
    }

    public String getEncryption() {
        return encryption;
    }

    public void setEncryption(String encryption) {
        this.encryption = encryption;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getStorageClass() {
        return storageClass;
    }

    public void setStorageClass(String storageClass) {
        this.storageClass = storageClass;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Long getServerCrc() {
        return serverCrc;
    }

    public void setServerCrc(Long serverCrc) {
        this.serverCrc = serverCrc;
    }

    public Long getLength() {
        return length;
    }

    public void setLength(Long length) {
        this.length = length;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObjectMetaInfo that = (ObjectMetaInfo) o;
        return Objects.equals(requestId, that.requestId) &&
                Objects.equals(lastModified, that.lastModified) &&
                Objects.equals(encryption, that.encryption) &&
                Objects.equals(md5, that.md5) &&
                Objects.equals(storageClass, that.storageClass) &&
                Objects.equals(tag, that.tag) &&
                Objects.equals(serverCrc, that.serverCrc) &&
                Objects.equals(length, that.length) &&
                Objects.equals(contentType, that.contentType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId, lastModified, encryption, md5, storageClass, tag, serverCrc, length, contentType);
    }

    @Override
    public String toString() {
        return "ObjectMetaInfo{" +
                "requestId='" + requestId + '\'' +
                ", lastModified=" + lastModified +
                ", encryption='" + encryption + '\'' +
                ", md5='" + md5 + '\'' +
                ", storageClass='" + storageClass + '\'' +
                ", tag='" + tag + '\'' +
                ", serverCrc=" + serverCrc +
                ", length=" + length +
                ", contentType='" + contentType + '\'' +
                '}';
    }
}
