package com.xuecheng.media;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.*;
import io.minio.errors.*;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.util.DigestUtils;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Package:PACKAGE_NAME
 * @Auther:Brianwei
 * @date:2024/2/19:20:46
 * @discribe: 测试minio sdk
 */
@SpringBootTest
@SpringBootApplication
public class MinioTest {

    //定义连接客户端的参数
    MinioClient minioClient = MinioClient
            .builder()
            .endpoint("http://192.168.36.111:9090")
            .credentials("admin","admin123456")
            .build();

    @Test
    public void testUp() throws Exception {
        //手动设置文件类型
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(".mp4");
        String minioType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if (extensionMatch != null){
            minioType = extensionMatch.getMimeType();
        }
        //上传文件
        minioClient.uploadObject(
                //上传文件的参数信息
                UploadObjectArgs.builder()
                        .bucket("testbucket")   //指定桶名
                        .filename("D:\\other\\下载\\百度网盘下载\\xcplus_media.sql")    //文件的路径
                        .object("test/01/1.sql")    //对象名称
                        .build()
        );
    }

    @Test
    public void testDelete() throws Exception{
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket("video")
                        .object("c/f/cfff06965ea58156c765d2408737d351")
                        .build()
        );
    }

    //查询文件就是下载文件
    @Test
    public void testGet() throws Exception {
        //get input steam
        //远程流不稳定，md5会有不同
        FilterInputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder().
                        bucket("testbucket")
                        .object("/test/01/1.sql")
                        .build()
        );
        //set the output steam
        FileOutputStream fileOutputStream = new FileOutputStream(new File("D:\\新建文件夹\\2.sql"));
        IOUtils.copy(inputStream,fileOutputStream);
        //md5 check
        FileInputStream fileInputStream = new FileInputStream(new File("D:\\other\\下载\\百度网盘下载\\xcplus_media.sql"));
        String source_md5 = DigestUtils.md5DigestAsHex(fileInputStream);
        String local_md5 = DigestUtils.md5DigestAsHex(new FileInputStream(new File("D:\\新建文件夹\\2.sql")));
        if (!source_md5.equals(local_md5)){
            throw new RuntimeException("there is wrong");
        }
    }

    /**
     * 测试上传分块文件
     * @throws Exception
     */
    @Test
    void testchunk() throws Exception {
        String filename = "C:\\Users\\BrianWei\\Desktop\\brianwei\\";
        File file = new File(filename);

        for (Long i = 0L; i < file.listFiles().length; i++) {
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket("testbucket")
                            //指定本地文件路径
                            .filename(filename + i)
                            .object("chunk/" + i)
                            .build()
            );
            System.out.println("上传分块文件" + i + "成功");
        }
    }

    @Test
    void testmerge() throws Exception {
        //通过流获取objectname的信息
        List<ComposeSource> collect = Stream.iterate(0, i -> ++i).limit(24).map(i -> ComposeSource.builder()
                .bucket("testbucket")
                .object("chunk/" + i)
                .build()).collect(Collectors.toList());
        collect.forEach(composeSource ->
                System.out.println(composeSource.object()));
        //合并文件，minio默认的分块文件大小为5m
        minioClient.composeObject(
                ComposeObjectArgs.builder()
                        .bucket("testbucket")
                        .object("new.mp4")
                        .sources(collect)
                        .build()
        );
    }
}
