package com.qihang.service.upload;

import com.qihang.common.util.upload.S3Util;
import com.qihang.common.vo.BaseVO;
import com.qihang.controller.upload.vo.FileVO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.InputStream;

/**
 * @author: bright
 * @description:
 * @time: 2022-07-17 18:30
 */
public class UploadServiceImpl implements IUploadService {

    @Resource
    private S3Util s3Util;

    public IUploadService setProperties(S3Util s3Util) {
        this.s3Util = s3Util;
        return this;
    }

    @Override
    public BaseVO upload(MultipartFile file) {
        FileVO fileVO = new FileVO();
        String url = s3Util.upload(file);
        fileVO.setUrl(url);
        return fileVO;
    }

    @Override
    public String upload(InputStream in) {
        return s3Util.upload(in);
    }

    @Override
    public String upload(InputStream in, File file) {
        throw new RuntimeException("未实现upload方法");
    }
}
