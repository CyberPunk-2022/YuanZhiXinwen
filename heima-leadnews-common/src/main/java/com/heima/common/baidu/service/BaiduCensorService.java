package com.heima.common.baidu.service;

import com.baidu.aip.contentcensor.AipContentCensor;
import com.heima.file.service.FileStorageService;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
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
            resultMap = turnResponseToMap(response);


            //JSONArray taskResults=response.getJSONArray("data");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultMap;
    }


    public Map imageScan(byte[] image) throws Exception {
        if (image == null) {
            return null;
        }

        try {
            JSONObject response = censorClient.imageCensorUserDefined(image, null);
            return turnResponseToMap(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println(response.toString());
        return null;
    }

    public Map imageScan(List<byte[]> imageList) throws Exception {
        boolean isBlock = false;
        boolean isReview = false;
        Map<String, Object> tempMap;
        Map<String, Object> resultMap = new HashMap<>();
        for (byte[] image : imageList) {
            try {
                JSONObject response = censorClient.imageCensorUserDefined(image, null);
                tempMap = turnResponseToMap(response);
                if ((tempMap.get("suggestion").equals("block"))) {
                    isBlock = true;
                    break;
                }
                if ((tempMap.get("suggestion").equals("review"))) {
                    isReview = true;
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        if (isBlock) {
            resultMap.put("suggestion", "block");
        } else if (isReview) {
            resultMap.put("suggestion", "review");
        } else {
            resultMap.put("suggestion", "pass");
        }
        return resultMap;
    }

    private Map<String, Object> turnResponseToMap(JSONObject response) {
        Map<String, Object> resultMap = new HashMap<>();
        String conclusion = String.valueOf(response.toMap().get("conclusion"));
        if (conclusion.equals("合规")) {
            resultMap.put("suggestion", "pass");
        } else if (conclusion.equals("不合规")) {
            resultMap.put("suggestion", "block");
            //JSONArray taskResults=response.getJSONArray("data");
            List dataList = (List) response.toMap().get("data");
            Map dataMap = (Map) dataList.get(0);
            resultMap.put("label", dataMap.get("msg"));
            return resultMap;

        } else if (conclusion.equals("疑似")) {
            resultMap.put("suggestion", "review");
        }

        return resultMap;
    }


}
