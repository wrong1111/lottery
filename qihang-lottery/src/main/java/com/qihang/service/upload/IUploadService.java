package com.qihang.service.upload;

import com.qihang.common.vo.BaseVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * @author: bright
 * @description:
 * @time: 2022-07-17 18:29
 */
public interface IUploadService {
    /**
     * 文件上传
     *
     * @param file
     * @return
     */
    BaseVO upload(MultipartFile file);

    String upload(InputStream in);
}
