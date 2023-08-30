package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.heima.apis.article.IArticleClient;
import com.heima.common.baidu.service.BaiduCensorService;
import com.heima.file.service.FileStorageService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.wemedia.mapper.WmChannelMapper;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.mapper.WmUserMapper;
import com.heima.wemedia.service.WmNewsAutoScanService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
@Transactional
public class WmNewsAutoScanServiceImpl implements WmNewsAutoScanService {
    @Autowired
    private BaiduCensorService baiduCensorService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private WmNewsMapper wmNewsMapper;

    @Autowired
    private IArticleClient articleClient;

    @Autowired
    private WmChannelMapper wmChannelMapper;

    @Autowired
    private WmUserMapper wmUserMapper;

    /**
     * 自媒体文章审核
     *
     * @param id 文章id
     */
    @Override
    public void autoScanWmNews(Integer id) {
        // 查询自媒体文章
        WmNews wmNews = wmNewsMapper.selectById(id);
        if (wmNews == null) {
            throw new RuntimeException("WmNewsAutoScanServiceImpl: " + "文章不存在 id： " + id);
        }
        if (wmNews.getStatus().equals(WmNews.Status.SUBMIT.getCode())) {//文章待审核
            // 从内容中提取纯文本内容和图片
            Map<String, Object> textAndImages = handleTextAndImages(wmNews);


            // 审核文本内容
            boolean isTextScan = handleTextScan((String) textAndImages.get("content"), wmNews);
            if (!isTextScan) {
                return;
            }


            // 审核图片内容
            boolean isImageScan = handleImageScan((List<String>) textAndImages.get("images"), wmNews);
            if (!isImageScan) return;

            // 审核成功，保存app端相关文章设计
            ResponseResult responseResult = saveAppArticle(wmNews);
            if (!responseResult.getCode().equals(200)){
                throw new RuntimeException("WmNewsAutoScanServiceImpl-文章审核，保存app端相关文章数据失败");
            }

            // 回填articleID
            wmNews.setArticleId((Long) responseResult.getData());
            updateWmNews(wmNews,WmNews.Status.PUBLISHED.getCode(),"审核成功");
        }

    }

    /**
     * 保存app端相关的文章数据
     * @param wmNews
     */
    private ResponseResult saveAppArticle(WmNews wmNews) {
        ArticleDto dto=new ArticleDto();
        // 属性拷贝
        BeanUtils.copyProperties(wmNews,dto);

        dto.setLayout(wmNews.getType());

        WmChannel wmChannel = wmChannelMapper.selectById(wmNews.getChannelId());
        if (wmChannel!=null){
            dto.setChannelName(wmChannel.getName());
        }
        // 作者
        dto.setAuthorId(wmNews.getUserId().longValue());
        WmUser wmUser = wmUserMapper.selectById(wmNews.getUserId());
        if(wmUser!=null){
            dto.setAuthorName(wmUser.getName());
        }
        // 设置文章id
        if(wmNews.getArticleId()!=null){
            dto.setId(wmNews.getArticleId());
        }
        dto.setCreatedTime(new Date());

        return articleClient.saveArticle(dto);

    }

    /**
     * 审核图片内容
     *
     * @param images
     * @param wmNews
     * @return
     */
    private boolean handleImageScan(List<String> images, WmNews wmNews) {

        boolean flag=true;
        if (images==null||images.size()==0){
            return true;
        }

        // 去重
        images = images.stream().distinct().collect(Collectors.toList());
        List<byte[]> imageList = new ArrayList<>();
        for (String image : images) {
            byte[] bytes = fileStorageService.downLoadFile(image);
            imageList.add(bytes);
        }


        try {
            Map map=baiduCensorService.imageScan(imageList);
            if (map != null) {
                // 审核失败
                if (map.get("suggestion").equals("block")) {
                    flag = false;
                    updateWmNews(wmNews, WmNews.Status.FAIL.getCode(), "当前文章中存在违规内容");
                }
                // 不确定，需要人工审核
                if (map.get("suggestion").equals("review")) {
                    flag = false;
                    updateWmNews(wmNews, WmNews.Status.ADMIN_AUTH.getCode(), "当前文章中存在疑似违规内容，需要进行人工审核");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return flag;
    }

    /**
     * 审核纯文本内容
     *
     * @param content
     * @param wmNews
     * @return
     */
    private boolean handleTextScan(String content, WmNews wmNews) {
        boolean flag = true;
        if ((wmNews.getTitle() + "-" + content).length() == 1) {
            return true;
        }
        try {
            Map map = baiduCensorService.greenTextScan(wmNews.getTitle() + "-" + content);
            if (map != null) {
                // 审核失败
                if (map.get("suggestion").equals("block")) {
                    flag = false;
                    updateWmNews(wmNews, WmNews.Status.FAIL.getCode(), "当前文章中存在违规内容");
                }

                // 不确定，需要人工审核
                if (map.get("suggestion").equals("review")) {
                    flag = false;
                    updateWmNews(wmNews, WmNews.Status.ADMIN_AUTH.getCode(), "当前文章中存在疑似违规内容，需要进行人工审核");

                }
            }
        } catch (Exception e) {
            flag = false;
            throw new RuntimeException(e);
        }
        return flag;
    }

    /**
     * 修改文章内容
     *
     * @param wmNews
     * @param
     * @param reason
     */
    private void updateWmNews(WmNews wmNews, short status, String reason) {
        wmNews.setStatus(status);
        wmNews.setReason(reason);
        wmNewsMapper.updateById(wmNews);
    }


    /**
     * 1.从自媒体文章内容中提取文章和图片
     * 2.提取文章的封面图片
     *
     * @param wmNews
     * @return
     */
    private Map<String, Object> handleTextAndImages(WmNews wmNews) {
        StringBuilder stringBuilder = new StringBuilder();
        List<String> images = new ArrayList<>();

        if (StringUtils.isNotBlank(wmNews.getContent())) {
            List<Map> maps = JSONArray.parseArray(wmNews.getContent(), Map.class);
            for (Map map : maps) {
                if (map.get("type").equals("text")) {
                    stringBuilder.append(map.get("value"));
                }

                if (map.get("type").equals("image")) {
                    images.add((String) map.get("value"));
                }
            }

        }
        if (StringUtils.isNotBlank(wmNews.getImages())) {
            String[] split = wmNews.getImages().split(",");
            images.addAll(Arrays.asList(split));

        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("content", stringBuilder.toString());
        resultMap.put("images", images);
        return resultMap;
    }
}
