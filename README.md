# file-storage-spring-boot-starter
文件存储封装(Fast DFS, Aliyun OSS), 并制作成Spring-boot starter

#使用方式
自己下载install引入使用

```
<dependency>
    <groupId>cn.darkjrong</groupId>
    <artifactId>file-storage-spring-boot-starter</artifactId>
    <version>1.0</version>
</dependency>
```

Aliyun OSS 配置参数(application.properties)  yml配置，必须配置enabled: true，否则默认false不起作用

```properties
    aliyun.oss.endpoint=
    aliyun.oss.accessKeyId=
    aliyun.oss.enabled=
    aliyun.oss.accessKeySecret=
    aliyun.oss.intranet=
    aliyun.oss.openIntranet=
```

是否使用内外模式上传 
> openIntranet :     1: open，close: 0

Fast DFS 配置参数(application.properties)  yml配置，必须配置enabled: true，否则默认false不起作用
```properties
    ## tracker地址，多个可fdfs.trackerList[0]、fdfs.trackerList[1]等方式配置
    fdfs.trackerList=192.168.10.128:22122
    ## 文件访问地址
    fdfs.web-server-url=192.168.10.128:9099
    ## 连接超时时间
    fdfs.connect-timeout=5000
    ## 读取inputsream阻塞时间
    fdfs.so-timeout=3000
    ## 连接池最大数量 
    fdfs.pool.max-total=200
    ## 每个tracker地址的最大连接数
    fdfs.pool.max-total-per-key=20
    ## 连接耗尽时等待获取连接的最大毫秒数
    fdfs.pool.max-wait-millis=25000
    ## 缩略图相关配置
    fdfs.thumbImage.height=150
    fdfs.thumbImage.width=150
    fdfs.enabled=true
```

代码使用
```java
    @Autowired
    private OssClient ossClient;
```

```java
    @Autowired
    private FastDFSClient fastDFSClient;
```


