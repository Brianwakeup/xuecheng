package com.xuecheng.media;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @Package:com.xuecheng.media
 * @Auther:Brianwei
 * @date:2024/3/1:9:15
 * @discribe:
 */
@SpringBootTest
public class BigFileTest {


    /**
     * 测试分段上传
     */
    @Test
    public void chunk() throws Exception {
        //源文件
        File file = new File("C:\\Users\\BrianWei\\Desktop\\rename.mp4");
        //分块文件存储路径
        String chunkFilePath = "C:\\Users\\BrianWei\\Desktop\\brianwei\\";
        //分块文件大小
        int size = 1024 * 1024 * 5;
        //分块文件个数
        int chunkNum = (int) Math.ceil(file.length() * 1.0 / size);
        //使用流从源文件中读取数据，向分块文件中写入数据
        RandomAccessFile r = new RandomAccessFile(file, "r");
        //缓存区
        byte[] bytes = new byte[1024];
        for (int i = 0; i < chunkNum; i++) {
            File file1 = new File(chunkFilePath + i);
            //分块文件写入流
            RandomAccessFile rw = new RandomAccessFile(file1, "rw");
            int len = -1;
            while ((len = r.read(bytes)) != -1){
                rw.write(bytes,0,len);
                if (file1.length() >= size){
                    break;
                }
            }
            rw.close();
        }
        r.close();
    }

    @Test
    public void merge() throws IOException {
        //原文件
        File or = new File("C:\\Users\\BrianWei\\Desktop\\brianwei.mp4");
        //创建完整的新文件
        File ne = new File("C:\\Users\\BrianWei\\Desktop\\brianwei\\brianwei.mp4");
        //块文件目录
        File block = new File("C:\\Users\\BrianWei\\Desktop\\brianwei\\");
        //将读取到的文件写入到一个完整的文件当中
        File[] files = block.listFiles();
        //将数组转换为list
        List<File> list = Arrays.asList(files);
        Collections.sort(list, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return Integer.parseInt(o1.getName()) - Integer.parseInt(o2.getName());
            }
        });
        //缓存区
        byte[] bytes = new byte[1024];
        //遍历分块文件，向合并的文件中进行写入
        RandomAccessFile rw = new RandomAccessFile(ne, "rw");
        for (File file : list) {
            //读分块的流
            RandomAccessFile r = new RandomAccessFile(file, "r");
            //向合并的文件去写
            int yuchen = -1;
            while ((yuchen = r.read(bytes)) != -1){
                rw.write(bytes);
            }
            r.close();
        }
        rw.close();
    }


}
