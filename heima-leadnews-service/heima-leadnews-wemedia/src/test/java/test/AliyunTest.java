
package test;

import com.heima.common.aliyun.GreenImageScan;
import com.heima.common.aliyun.GreenTextScan;
import com.heima.common.baidu.service.BaiduCensorService;
import com.heima.file.service.FileStorageService;
import com.heima.wemedia.WemediaApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Map;

@SpringBootTest(classes = WemediaApplication.class)
@RunWith(SpringRunner.class)
public class AliyunTest {
    @Autowired
    private BaiduCensorService baiduCensorService;

    @Autowired
    private GreenTextScan greenTextScan;

    @Autowired
    private GreenImageScan greenImageScan;

    @Autowired
    private FileStorageService fileStorageService;

    @Test
    public void testScanText() throws Exception {
        Map map = greenTextScan.greenTextScan("我是一个好人,冰毒");

        System.out.println(map);
    }
    @Test
    public void baiduText() throws Exception{
        System.out.println(baiduCensorService.greenTextScan("我是一个好人"));
        System.out.println(baiduCensorService.greenTextScan("我是一个好人,冰毒"));
    }

    @Test
    public void testScanImage() throws Exception {
        byte[] bytes = fileStorageService.downLoadFile("http://minio-server:9000/leadnews/2023/08/24/829e7eeab30344ffab92bc5193426645.png");
        Map map = greenImageScan.imageScan(Arrays.asList(bytes));
        System.out.println(map);
    }

    @Test
    public void baiduImage() throws Exception {
        byte[] bytes = fileStorageService.downLoadFile("http://minio-server:9000/leadnews/2023/08/24/829e7eeab30344ffab92bc5193426645.png");
        Map map = baiduCensorService.imageScan(bytes);
        System.out.println(map);
    }
}