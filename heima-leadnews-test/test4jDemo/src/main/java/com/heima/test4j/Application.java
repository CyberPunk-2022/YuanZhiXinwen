package com.heima.test4j;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.io.File;


/**
 * 1.动态规划
 * 2.贪心算法
 * 3.回溯
 */
public class Application {
    /**
     * 识别图片中的文字
     * @param args
     */
    public static void main(String[] args) throws TesseractException {
        // 创建实例
        ITesseract tesseract=new Tesseract();

        // 设置字体库路径
        tesseract.setDatapath("D:\\workspace\\heima-leadnews\\heima-leadnews-test\\test4jDemo\\src\\main\\resources");

        // 设置语言
        tesseract.setLanguage("chi_sim");
        // 识别图片
        String result = tesseract.doOCR(new File("C:\\Users\\DELL\\Pictures\\模电实验005.jpg"));
        System.out.println("执行结果: "+result.replaceAll("\\r|\\n","-"));
    }
}
