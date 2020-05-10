package cn.darkjrong.storage.oss.domain;

import com.aliyun.oss.model.Owner;

import java.io.Serializable;
import java.util.Objects;

/**
 * 存储空间信息
 * @author Rong.Jia
 * @date 2019/10/21 19:04
 */
public class BucketInfo implements Serializable {

    /**
     *  地域
     */
    private String location;

    /**
     *  添加时间
     */
    private String createTime;

    /**
     *  拥有者信息
     */
    private Owner owner;

    /**
     *  权限信息
     */
    private String authority;

    public BucketInfo() {
    }

    public BucketInfo(String location, String createTime, Owner owner, String authority) {
        this.location = location;
        this.createTime = createTime;
        this.owner = owner;
        this.authority = authority;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BucketInfo that = (BucketInfo) o;
        return Objects.equals(location, that.location) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(owner, that.owner) &&
                Objects.equals(authority, that.authority);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, createTime, owner, authority);
    }

    @Override
    public String toString() {
        return "BucketInfo{" +
                "location='" + location + '\'' +
                ", createTime='" + createTime + '\'' +
                ", owner=" + owner +
                ", authority='" + authority + '\'' +
                '}';
    }
}
