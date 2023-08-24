package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.file.service.FileStorageService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.pojos.WmMaterial;
import com.heima.utils.thread.WmThreadLocalUtil;
import com.heima.wemedia.mapper.WmMaterialMapper;
import com.heima.wemedia.service.WmMaterialService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;


@Slf4j
@Service
@Transactional
public class WmMaterialServiceImpl extends ServiceImpl<WmMaterialMapper, WmMaterial> implements WmMaterialService {

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * 素材图片上传
     *
     * @param multipartFile
     * @return
     */
    @Override
    public ResponseResult uploadPicture(MultipartFile multipartFile) {
        // 检查参数
        if (multipartFile == null || multipartFile.getSize() == 0) {
            // 当前参数失效
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        // 上传图片到minIO
        String fileName = UUID.randomUUID().toString().replace("-", "");

        // 拿到原文件的后缀名给fileName
        String originalFilename = multipartFile.getOriginalFilename();
        String fileId = null;
        String postfix = originalFilename.substring(originalFilename.lastIndexOf("."));
        try {
            fileId = fileStorageService.uploadImgFile("", fileName + postfix, multipartFile.getInputStream());
            log.info("上传图片到minIO中,fileId:{}", fileId);
        } catch (IOException e) {
            log.error("WmMaterialServiceImpl-上传文件失败");
            throw new RuntimeException(e);
        }
        
        // 把图片路径保存到数据库
        WmMaterial wmMaterial = new WmMaterial();
        wmMaterial.setUserId(WmThreadLocalUtil.getUser().getId())
                .setUrl(fileId)
                .setIsCollection((short)0)
                .setType((short)0)
                .setCreatedTime(new Date());
        save(wmMaterial);

        // 返回结果
        return ResponseResult.okResult(wmMaterial);
    }
}
