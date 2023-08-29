package com.heima.common.baidu.service;

import com.alibaba.fastjson.JSON;
import com.baidu.aip.contentcensor.AipContentCensor;
import com.heima.file.service.FileStorageService;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.jnlp.FileSaveService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Service
@ConfigurationProperties(prefix = "baidu")
public class BaiduCensorService {

    private String AppID;

    private String API_Key;

    private String Secret_Key;
    @Autowired
    private FileStorageService fileStorageService;

    AipContentCensor censorClient;

    @PostConstruct
    private void initClient() {
        censorClient = new AipContentCensor(AppID, API_Key, Secret_Key);
        System.out.println("AppID: " + AppID);
        censorClient.setConnectionTimeoutInMillis(2000);
        censorClient.setSocketTimeoutInMillis(60000);
    }

    public Map greenTextScan(String content) throws Exception {
        Map resultMap = new HashMap<>();
        if (content == null) {
            return null;
        }
        try {
            JSONObject response = censorClient.textCensorUserDefined(content);
            String conclusion = String.valueOf(response.toMap().get("conclusion"));
            if (conclusion.equals("合规")) {
                resultMap.put("suggestion", "pass");
            } else {
                resultMap.put("suggestion", "not pass");
                //JSONArray taskResults=response.getJSONArray("data");
                List dataList = (List) response.toMap().get("data");
                Map dataMap = (Map) dataList.get(0);
                resultMap.put("label", dataMap.get("msg"));
                return resultMap;

            }


            //JSONArray taskResults=response.getJSONArray("data");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultMap;
    }


    public Map imageScan(byte[] imageList) throws Exception {
        if(imageList==null){
            return null;
        }

        try {
            JSONObject response = censorClient.imageCensorUserDefined(imageList, null);
            return turnResponseToMap(response);
        }catch (Exception e){
            e.printStackTrace();
        }
        //System.out.println(response.toString());
        return null;
    }
    private Map turnResponseToMap(JSONObject response){
        Map<String,Object> resultMap=new HashMap<>();
        String conclusion = String.valueOf(response.toMap().get("conclusion"));
        if (conclusion.equals("合规")) {
            resultMap.put("suggestion", "pass");
        } else {
            resultMap.put("suggestion", "not pass");
            //JSONArray taskResults=response.getJSONArray("data");
            List dataList = (List) response.toMap().get("data");
            Map dataMap = (Map) dataList.get(0);
            resultMap.put("label", dataMap.get("msg"));

        }
        return resultMap;
    }


}
