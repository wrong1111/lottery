package com.qihang.service.upload;

import com.qihang.common.util.upload.LocalUtil;
import com.qihang.common.vo.BaseVO;
import com.qihang.controller.upload.vo.FileVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;


public class LocalUploadServiceImpl implements IUploadService {

    @Autowired
    LocalUtil localUtil;

    public IUploadService setProperties(LocalUtil localUtil) {
        this.localUtil = localUtil;
        return this;
    }

    @Override
    public BaseVO upload(MultipartFile file) {
        FileVO fileVO = new FileVO();
        String url = localUtil.upload(file);
        fileVO.setUrl(url);
        return fileVO;
    }

    @Override
    public String upload(InputStream in) {
        return localUtil.upload(in);
    }

    @Override
    public String upload(InputStream in, File file) {
        return localUtil.upload(in);
    }
}
