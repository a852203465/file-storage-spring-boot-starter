package cn.darkjrong.storage.utils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * 文件工具类
 *
 * @author Rong.Jia
 * @date 2020/01/06 14:51
 */
public class FileUtils {

    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    public static final String JPEG_SUFFIX = ".jpeg";
    public static final String DOT = ".";
    public static final String COLON = ":";
    public static final String SLASH = "/";
    public static final String BACKSLASH = "\\";
    public static final String DOUBLE_DOT = "..";
    public static final String BASE64_REG = "[\\s*\t\n\r]";
    public static final String TMP_DIR = System.getProperty("user.dir") + "/data/tmp";
    public static final String HTTP_PREFIX = "http://";
    public static final String HTTPS_PREFIX = "https://";

    /**
     * 获取标准的绝对路径
     *
     * @param file 文件
     * @return 绝对路径
     */
    public static String getAbsolutePath(File file) {

        if (file == null) {
            return null;
        }

        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            return file.getAbsolutePath();
        }
    }

    /**
     * 给定路径已经是绝对路径FILE_TYPE_MAP
     * 此方法并没有针对路径做标准化，建议先执行{@link #normalize(String)}方法标准化路径后判断
     *
     * @param path 需要检查的Path
     * @return 是否已经是绝对路径
     */
    public static boolean isAbsolutePath(String path) {

        if (StringUtils.isBlank(path)) {
            return false;
        }

        if (SLASH.charAt(0) == path.charAt(0) || path.matches("^[a-zA-Z]:[/\\\\].*")) {
            // 给定的路径已经是绝对路径了
            return true;
        }
        return false;
    }

    /**
     * 判断是否为目录，如果file为null，则返回false
     *
     * @param file 文件
     * @return 如果为目录true
     */
    public static boolean isDirectory(File file) {
        return (file != null) && file.isDirectory();
    }

    /**
     * 判断是否为文件，如果file为null，则返回false
     *
     * @param file 文件
     * @return 如果为文件true
     */
    public static boolean isFile(File file) {
        return (file != null) && file.isFile();
    }

    /**
     * 检查两个文件是否是同一个文件
     * 所谓文件相同，是指File对象是否指向同一个文件或文件夹
     *
     * @param file1 文件1
     * @param file2 文件2
     * @return 是否相同
     * @throws IOException IO异常
     */
    public static boolean equals(File file1, File file2) throws IOException {

        if (!file1.exists() || !file2.exists()) {

            // 两个文件都不存在判断其路径是否相同
            if (!file1.exists() && !file2.exists() && pathEquals(file1, file2)) {
                return true;
            }

            // 对于一个存在一个不存在的情况，一定不相同
            return false;
        }
        return Files.isSameFile(file1.toPath(), file2.toPath());
    }

    /**
     * 比较两个文件内容是否相同
     * 首先比较长度，长度一致再比较内容
     *
     * @param file1 文件1
     * @param file2 文件2
     * @return 两个文件内容一致返回true，否则false
     * @throws IOException IO异常
     */
    public static boolean contentEquals(File file1, File file2) throws IOException {

        boolean file1Exists = file1.exists();

        if (file1Exists != file2.exists()) {
            return false;
        }

        if (!file1Exists) {

            // 两个文件都不存在，返回true
            return true;
        }

        if (file1.isDirectory() || file2.isDirectory()) {

            // 不比较目录
            throw new IOException("Can't compare directories, only files");
        }

        if (file1.length() != file2.length()) {
            // 文件长度不同
            return false;
        }

        if (equals(file1, file2)) {
            // 同一个文件
            return true;
        }

        InputStream input1 = null;
        InputStream input2 = null;
        try {
            input1 = getInputStream(file1);
            input2 = getInputStream(file2);
            return IOUtils.contentEquals(input1, input2);

        } finally {
            IOUtils.closeQuietly(input1);
            IOUtils.closeQuietly(input2);
        }
    }

    /**
     * 文件路径是否相同
     * 取两个文件的绝对路径比较，在Windows下忽略大小写，在Linux下不忽略。
     *
     * @param file1 文件1
     * @param file2 文件2
     * @return 文件路径是否相同
     */
    public static boolean pathEquals(File file1, File file2) {

        if (!SystemUtils.judgeSystem()) {
            // Windows环境
            try {
                if (StringUtils.equalsIgnoreCase(file1.getCanonicalPath(), file2.getCanonicalPath())) {
                    return true;
                }
            } catch (Exception e) {
                if (StringUtils.equalsIgnoreCase(file1.getAbsolutePath(), file2.getAbsolutePath())) {
                    return true;
                }
            }
        } else {
            // 类Unix环境
            try {
                if (StringUtils.equals(file1.getCanonicalPath(), file2.getCanonicalPath())) {
                    return true;
                }
            } catch (Exception e) {
                if (StringUtils.equals(file1.getAbsolutePath(), file2.getAbsolutePath())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断文件是否被改动
     * 如果文件对象为 null 或者文件不存在，被视为改动
     *
     * @param file           文件对象
     * @param lastModifyTime 上次的改动时间
     * @return 是否被改动
     */
    public static boolean isModifed(File file, long lastModifyTime) {
        if (null == file || !file.exists()) {
            return true;
        }
        return file.lastModified() != lastModifyTime;
    }

    /**
     * 修复路径FILE_TYPE_MAP
     * 如果原路径尾部有分隔符，则保留为标准分隔符（/），否则不保留
     *
     * @param path 原路径
     * @return 修复后的路径
     */
    public static String normalize(String path) {
        if (path == null) {
            return null;
        }

        // 兼容Spring风格的ClassPath路径，去除前缀，不区分大小写
        String pathToUse = StringUtils.removeStartIgnoreCase(path, "classpath:");
        // 去除file:前缀
        pathToUse = StringUtils.removeStartIgnoreCase(pathToUse, "file:");
        // 统一使用斜杠
        pathToUse = pathToUse.replaceAll("[/\\\\]{1,}", "/").trim();

        int prefixIndex = pathToUse.indexOf(COLON);
        String prefix = "";
        if (prefixIndex > -1) {
            // 可能Windows风格路径
            prefix = pathToUse.substring(0, prefixIndex + 1);
            if (StringUtils.startsWith(prefix, SLASH)) {
                // 去除类似于/C:这类路径开头的斜杠
                prefix = prefix.substring(1);
            }
            if (!prefix.contains("/")) {
                pathToUse = pathToUse.substring(prefixIndex + 1);
            } else {
                // 如果前缀中包含/,说明非Windows风格path
                prefix = StringUtils.EMPTY;
            }
        }
        if (pathToUse.startsWith(SLASH)) {
            prefix += SLASH;
            pathToUse = pathToUse.substring(1);
        }

        List<String> pathList = Arrays.asList(StringUtils.split(pathToUse, SLASH));
        List<String> pathElements = new LinkedList<String>();
        int tops = 0;

        String element;
        for (int i = pathList.size() - 1; i >= 0; i--) {
            element = pathList.get(i);
            if (DOT.equals(element)) {
                // 当前目录，丢弃
            } else if (DOUBLE_DOT.equals(element)) {
                tops++;
            } else {
                if (tops > 0) {
                    // 有上级目录标记时按照个数依次跳过
                    tops--;
                } else {
                    // Normal path element found.
                    pathElements.add(0, element);
                }
            }
        }

        return prefix + StringUtils.join(pathElements, SLASH);
    }

    /**
     * 获得相对子路径
     *
     * @param rootDir 绝对父路径
     * @param file    文件
     * @return 相对子路径
     * @throws IOException io 异常
     */
    public static String subPath(String rootDir, File file) throws IOException {

        return subPath(rootDir, file.getCanonicalPath());
    }

    /**
     * 获得相对子路径，忽略大小写
     *
     * @param dirPath  父路径
     * @param filePath 文件路径
     * @return 相对子路径
     */
    public static String subPath(String dirPath, String filePath) {
        if (StringUtils.isNotEmpty(dirPath) && StringUtils.isNotEmpty(filePath)) {

            dirPath = StringUtils.removeEnd(normalize(dirPath), "/");
            filePath = normalize(filePath);

            final String result = StringUtils.removeStartIgnoreCase(filePath, dirPath);
            return StringUtils.removeStart(result, "/");
        }
        return filePath;
    }

    /**
     * 获取指定位置的子路径部分，支持负数，例如index为-1表示从后数第一个节点位置
     *
     * @param path  路径
     * @param index 路径节点位置，支持负数（负数从后向前计数）
     * @return 获取的子路径
     */
    public static Path getPathEle(Path path, int index) {
        return subPath(path, index, index == -1 ? path.getNameCount() : index + 1);
    }

    /**
     * 获取指定位置的最后一个子路径部分
     *
     * @param path 路径
     * @return 获取的最后一个子路径
     */
    public static Path getLastPathEle(Path path) {
        return getPathEle(path, path.getNameCount() - 1);
    }

    /**
     * 获取指定位置的子路径部分，支持负数，例如起始为-1表示从后数第一个节点位置
     *
     * @param path      路径
     * @param fromIndex 起始路径节点（包括）
     * @param toIndex   结束路径节点（不包括）
     * @return 获取的子路径
     */
    public static Path subPath(Path path, int fromIndex, int toIndex) {
        if (null == path) {
            return null;
        }
        final int len = path.getNameCount();

        if (fromIndex < 0) {
            fromIndex = len + fromIndex;
            if (fromIndex < 0) {
                fromIndex = 0;
            }
        } else if (fromIndex > len) {
            fromIndex = len;
        }

        if (toIndex < 0) {
            toIndex = len + toIndex;
            if (toIndex < 0) {
                toIndex = len;
            }
        } else if (toIndex > len) {
            toIndex = len;
        }

        if (toIndex < fromIndex) {
            int tmp = fromIndex;
            fromIndex = toIndex;
            toIndex = tmp;
        }

        if (fromIndex == toIndex) {
            return null;
        }
        return path.subpath(fromIndex, toIndex);
    }

    /**
     * 返回主文件名
     *
     * @param file 文件
     * @return 主文件名
     */
    public static String mainName(File file) {
        if (file.isDirectory()) {
            return file.getName();
        }
        return mainName(file.getName());
    }

    /**
     * 返回主文件名
     *
     * @param fileName 完整文件名
     * @return 主文件名
     */
    public static String mainName(String fileName) {
        if (StringUtils.isBlank(fileName) || !fileName.contains(DOT)) {
            return fileName;
        }
        return StringUtils.substring(fileName, fileName.lastIndexOf(DOT));
    }

    /**
     * 获取文件扩展名，扩展名不带“.”
     *
     * @param file 文件
     * @return 扩展名
     */
    public static String extName(File file) {
        if (null == file) {
            return null;
        }
        if (file.isDirectory()) {
            return null;
        }
        return extName(file.getName());
    }

    /**
     * 获得文件的扩展名，扩展名不带“.”
     *
     * @param fileName 文件名
     * @return 扩展名
     */
    public static String extName(String fileName) {
        if (fileName == null) {
            return null;
        }
        int index = fileName.lastIndexOf(DOT);
        if (index == -1) {
            return null;
        } else {
            String ext = fileName.substring(index + 1);

            // 扩展名中不能包含路径相关的符号
            return (ext.contains(SLASH) || ext.contains(BACKSLASH)) ? null : ext;
        }
    }

    /**
     * 判断文件路径是否有指定后缀，忽略大小写FILE_TYPE_MAP
     * 常用语判断扩展名
     *
     * @param file   文件或目录
     * @param suffix 后缀
     * @return 是否有指定后缀
     */
    public static boolean pathEndsWith(File file, String suffix) {
        return file.getPath().toLowerCase().endsWith(suffix);
    }

    /**
     * 获得输入流
     *
     * @param path Path
     * @return 输入流
     * @throws IOException 文件未找到
     */
    public static BufferedInputStream getInputStream(Path path) throws IOException {

        return new BufferedInputStream(Files.newInputStream(path));
    }

    /**
     * 获得输入流
     *
     * @param file 文件
     * @return 输入流
     * @throws IOException 文件未找到
     */
    public static BufferedInputStream getInputStream(File file) throws IOException {
        return new BufferedInputStream(IOUtils.toInputStream(file.getName(), UTF_8));
    }

    /**
     * 获取指定层级的父路径
     *
     * @param file  目录或文件
     * @param level 层级
     * @return 路径File，如果不存在返回null
     */
    public static File getParent(File file, int level) {
        if (level < 1 || null == file) {
            return file;
        }

        final File parentFile = file.getParentFile();
        if (1 == level) {
            return parentFile;
        }
        return getParent(parentFile, level - 1);
    }

    /**
     * 判断文件是否存在
     *
     * @param fileName 文件名
     * @return boolean
     * @date 2019/11/03 11:08:22
     */
    public static boolean isExists(String fileName) {

        return Files.exists(Paths.get(fileName));
    }

    /**
     * 判断是否是文件夹
     *
     * @param path 文件夹
     * @date 2019/11/03 11:08:22
     */
    public static boolean isDirectory(String path) {

        Path p = Paths.get(path);

        return !Files.isRegularFile(p);
    }

    /**
     * 拼接路径
     *
     * @param first 第一个路径
     * @param more  子路径
     * @return Path
     * @date 2019/11/12 00:13:22
     */
    public static Path get(String first, String... more) {

        return Paths.get(first, more);
    }

    /**
     * 将URI对象转化成Path对象
     *
     * @param fileName 文件路径名
     * @return Path对象
     */
    public static Path get(String fileName) {

        URI uri = URI.create(fileName);
        return Paths.get(uri);
    }

    /**
     * File和URI之间的转换
     *
     * @param file 文件
     * @return uri
     */
    public static String get(File file) {

        Path p1 = file.toPath();
        p1.toFile();
        return file.toURI().toString();

    }

    /**
     * 删除目录
     *
     * @param directory 目录
     */
    public static void deleteDirectory(String directory) throws IOException {

        Path path = Paths.get(directory);
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                Files.deleteIfExists(file);

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {

                Files.delete(dir);

                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * 创建文件，目录
     *
     * @param dirPath 文件，目录名
     * @param flag    创建文件。还是目录， true: 文件， false：目录
     * @return
     */
    public static String mkdirs(String dirPath, boolean flag) {

        try {

            if (flag) {
                String filePath = dirPath.substring(0, dirPath.lastIndexOf(File.separator));
                Path path = Paths.get(filePath);
                if (Files.notExists(path)) {
                    Files.createDirectories(path);
                    path = Paths.get(dirPath);
                    Files.createFile(path);
                }
            } else {
                Path path = Paths.get(dirPath);
                if (Files.notExists(path)) {
                    Files.createDirectories(path);
                }
            }

            return dirPath;
        } catch (Exception e) {
            logger.error("mkdirs {}", e.getMessage());
        }
        return null;
    }

    /**
     * 读取Text文件操作
     *
     * @param fileName 文件名
     */
    public static String readText(String fileName) throws IOException {

        byte[] data = Files.readAllBytes(Paths.get(fileName));
        return new String(data, StandardCharsets.UTF_8);
    }

    /**
     * 读取Text文件操作
     *
     * @param fileName 文件名
     */
    public static List<String> readTextLines(String fileName) throws IOException {

        return Files.readAllLines(Paths.get(fileName));
    }

    /**
     * 写入Text文件操作
     *
     * @param fileName 文件名
     * @param content  内容
     * @param isAppend 是否追加
     */
    public static void writeText(String fileName, String content, boolean isAppend) throws IOException {

        if (isAppend) {
            Files.write(Paths.get(fileName), content.getBytes(), StandardOpenOption.APPEND);
        } else {
            Files.write(Paths.get(fileName), content.getBytes());
        }
    }

    /**
     * 按行写入Text文件操作
     *
     * @param fileName 文件名
     * @param content  内容
     */
    public static void writeTextLines(String fileName, List<String> content) throws IOException {

        Files.write(Paths.get(fileName), content, UTF_8);
    }

    /**
     * base64 转 file
     *
     * @param base64  base64
     * @param dirPath 文件存储地址
     * @return File 文件
     * @throws IOException io异常
     */
    public static File base64ToFile(String base64, String dirPath) throws IOException {

        base64 = base64.replaceAll(BASE64_REG, StringUtils.EMPTY);

        String filename = UUIDUtils.uuid() + "-" + System.currentTimeMillis() + JPEG_SUFFIX;

        if (!StringUtils.endsWith(dirPath, SLASH)) {
            dirPath = dirPath + SLASH;
        }

        return Files.write(Paths.get(dirPath + filename), Base64.getDecoder().decode(base64), StandardOpenOption.CREATE).toFile();
    }

    /**
     * 根据byte数组，生成文件
     *
     * @param bfile    文件数组
     * @param filePath 文件存放路径
     * @param fileName 文件名称
     * @return File 生成的文件
     */
    public static File byte2File(byte[] bfile, String filePath, String fileName) throws IOException {

        mkdirs(filePath, Boolean.FALSE);

        Path path = Paths.get(filePath + File.separator + fileName);
        Files.write(path, bfile);

        return path.toFile();
    }

    /**
     * 获取文件大小
     *
     * @param fileName 文件名
     * @return 文件大小
     */
    public static String getFileSize(String fileName) {

        Long number = 0L;

        Path testPath = Paths.get(fileName);
        BasicFileAttributeView basicView = Files.getFileAttributeView(testPath, BasicFileAttributeView.class);

        try {
            BasicFileAttributes basicFileAttributes = basicView.readAttributes();

            return readableFileSize(basicFileAttributes.size());

        } catch (Exception e) {
            logger.error("getFileSize {}", e.getMessage());
        }

        return String.valueOf(number);
    }

    /**
     * 文件大小转换
     *
     * @param size 文件大小
     * @return 带单位的文件大小
     * @date 2019/10/31 11:00:22
     */
    public static String readableFileSize(long size) {

        if (size <= 0L) {
            return String.valueOf(0);
        }

        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};

        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));

        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    /**
     * 删除文件, 目录 （空目录）
     *
     * @param file 文件，目录
     */
    public static Boolean delete(String file) {

        Boolean flag = Boolean.FALSE;

        try {
            Path target = Paths.get(file);
            Files.deleteIfExists(target);
            flag = Boolean.TRUE;

        } catch (Exception e) {
            logger.error("delete {}", e.getMessage());
        }

        return flag;
    }

    /**
     * 拷贝文件  如果目标文件存在，则替换它
     *
     * @param source 源文件
     * @param target 目标文件
     * @return Boolean 复制成功与否
     */
    public static Path copyFile(String source, String target) throws IOException {

        return Files.copy(Paths.get(source), Paths.get(target), REPLACE_EXISTING);
    }

    /**
     * 拷贝文件
     *
     * @param source       源文件
     * @param outputStream 目标流
     * @return Path
     */
    public static Path copyFile(String source, OutputStream outputStream) throws IOException {

        Path path = Paths.get(source);

        Files.copy(path, outputStream);

        return path;
    }

    /**
     * 拷贝文件  如果目标文件存在，则替换它
     *
     * @param inputStream 输入流
     * @param target      目标文件
     * @return Path
     */
    public static Path copyFile(InputStream inputStream, String target) throws IOException {

        Path path = Paths.get(target);

        Files.copy(inputStream, path, REPLACE_EXISTING);

        return path;
    }

    /**
     * 移动文件  如果目标文件存在，则替换它
     *
     * @param source 源文件
     * @param target 目标文件
     * @return Path
     */
    public static Path moveFile(String source, String target) throws IOException {

        return Files.move(Paths.get(source), Paths.get(target), REPLACE_EXISTING);

    }

    /**
     * 检查文件可读权限
     *
     * @param fileName 文件名
     */
    public static Boolean isReadable(String fileName) {

        Path path = Paths.get(fileName);

        return Files.isReadable(path);
    }

    /**
     * 文件是可写的
     *
     * @param fileName 文件名
     * @return
     */
    public static Boolean isWritable(String fileName) {

        Path path = Paths.get(fileName);
        return Files.isWritable(path);
    }

    /**
     * 文件是是可执行的
     *
     * @param fileName 文件名
     * @return
     */
    public static Boolean isExecutable(String fileName) {

        Path path = Paths.get(fileName);
        return Files.isExecutable(path);

    }

    /**
     * 检查两个都指向了文件系统下的同一个文件源
     *
     * @param fileName1 文件1
     * @param fileName2 文件2
     * @return true/fasle
     */
    public static Boolean isSameFile(String fileName1, String fileName2) {

        Path path1 = Paths.get(fileName1);
        Path path2 = Paths.get(fileName2);

        Boolean flag = Boolean.FALSE;

        try {
            flag = Files.isSameFile(path1, path2);
        } catch (Exception e) {
            logger.error("isSameFile {}", e.getMessage());
        }

        return flag;
    }

    /**
     * 创建临时文件
     *
     * @param dirPath 文件路径
     * @param prefix  文件前缀
     * @param suffix  文件后缀
     */
    public static Boolean creatTempFile(String dirPath, String prefix, String suffix) {

        Boolean flag = Boolean.FALSE;

        try {

            Path path = Paths.get(dirPath);
            if (Files.notExists(path)) {
                Files.createDirectories(path);
            }
            Files.createTempFile(path, prefix, suffix);

            flag = Boolean.TRUE;
        } catch (IOException e) {
            logger.error("creatTempFile {}", e.getMessage());
        }

        return flag;
    }


    /**
     * 文件流转base64
     *
     * @param in 文件流
     * @return String base64
     * @author Rong.Jia
     * @date 2019/01/11 11:20
     */
    public static String inputStream2Base64(InputStream in) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(in, out);
        byte[] data = out.toByteArray();

        IOUtils.closeQuietly(in);
        IOUtils.closeQuietly(out);

        return Base64.getEncoder().encodeToString(data);

    }

    /**
     * base64 转InputStream
     *
     * @param base64string base64文件
     * @return InputStream InputStream流对象
     * @date 2019/01/11 11:20
     * @author Rong.Jia
     */
    public static InputStream base2InputStream(String base64string) {

        base64string = base64string.replaceAll(BASE64_REG, StringUtils.EMPTY);

        byte[] bytes1 = Base64.getDecoder().decode(base64string);

        return new ByteArrayInputStream(bytes1);

    }

    /**
     * URL 转 base64
     *
     * @param requestUrl
     * @return String 图片base64
     * @throws IOException 文件写出异常
     * @author Rong.Jia
     * @date 2019/01/11 11:20
     */
    public static String urlToBase64(String requestUrl) throws IOException {

        URL url = new URL(requestUrl);
        URLConnection connection = url.openConnection();
        InputStream stream = connection.getInputStream();
        return inputStream2Base64(stream);

    }

    /**
     * file 转base64
     *
     * @param file 文件
     * @return String base64
     * @throws IOException 文件转换失败
     */
    public static String fileToBase64(File file) throws IOException {

        return inputStream2Base64(new FileInputStream(file));

    }

    /**
     * 输入流转字节流
     *
     * @return byte[] byte数组
     * @date 2019/09/19 09:42:22
     * @author Rong.Jia
     * @throws IOException io异常
     */
    public static byte[] inputStream2Byte(InputStream in) throws IOException{

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        IOUtils.copy(in, out);

        byte[] data = out.toByteArray();

        IOUtils.closeQuietly(in);
        IOUtils.closeQuietly(out);

        return data;
    }

    /**
     * url 转File
     *
     * @param dirPath  存储路径
     * @param fileName 文件名
     * @return File
     * @throws IOException
     * @date 2018/09/29 14:00
     * @author Rong.Jia
     */
    public static File urlToFile(String requestUrl, String dirPath, String fileName) throws IOException {

        URL url = new URL(requestUrl);
        URLConnection connection = url.openConnection();
        InputStream stream = connection.getInputStream();

        if (!StringUtils.endsWith(dirPath, SLASH)) {
            dirPath = dirPath + SLASH;
        }

        Path path = Paths.get(dirPath + fileName);

        Files.copy(stream, path, REPLACE_EXISTING);

        return path.toFile();

    }

    /**
     * Future 读取文件
     *
     * @param fileName 文件名
     * @return 内容
     * @date 2019/11/12 08:52:22
     */
    public static String readWithFuture(String fileName) throws IOException {

        AsynchronousFileChannel channel = AsynchronousFileChannel.open(Paths.get(fileName), StandardOpenOption.READ);

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        long position = 0;

        Future<Integer> future = channel.read(buffer, position);

        while (!future.isDone()) ;

        buffer.flip();
        byte[] data = new byte[buffer.limit()];
        buffer.get(data);

        buffer.clear();

        return new String(data);

    }

    /**
     * Future 写入文件
     *
     * @param fileName 文件名
     * @param message  文件内容
     * @date 2019/11/12 19:01:22
     */
    public static File writeWithFuture(String fileName, String message) throws IOException {

        mkdirs(fileName, true);

        Path path = Paths.get(fileName);

        AsynchronousFileChannel channel = AsynchronousFileChannel.open(path,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE);

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        long position = 0;

        buffer.put(message.getBytes());
        buffer.flip();

        Future<Integer> operation = channel.write(buffer, position);
        buffer.clear();

        while (!operation.isDone()) ;

        return path.toFile();
    }

    /**
     * CompletionHandler 写入文件
     *
     * @param fileName 文件名
     * @param message  文件内容
     * @date 2019/11/12 19:01:22
     */
    public static File writeWithCompletionHandler(String fileName, String message) throws IOException {

        mkdirs(fileName, Boolean.TRUE);

        Path path = Paths.get(fileName);

        final AsynchronousFileChannel channel =
                AsynchronousFileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE);

        byte[] byteArray = message.getBytes();
        ByteBuffer buffer = ByteBuffer.wrap(byteArray);

        channel.write(buffer, 0, null, new CompletionHandler<Integer, ByteBuffer>() {

            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                logger.error("Write done");
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                logger.error("writeWithCompletionHandler failed {}", exc.getMessage());
            }

        });

        return path.toFile();
    }







}
