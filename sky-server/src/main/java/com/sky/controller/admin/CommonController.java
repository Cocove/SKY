package com.sky.controller.admin;

import com.aliyuncs.exceptions.ClientException;
import com.sky.Utils.AliOSSUtils;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/admin/common")
@Slf4j
public class CommonController {

    @Autowired
    private AliOSSUtils aliOSSUtils;

    @PostMapping("/upload")
    public Result upload(MultipartFile file) throws IOException, ClientException {
        log.info("文件上传：{}", file);

        String url = aliOSSUtils.upload(file);
        log.info("文件上传完成，文件访问链接：{}", url);


        return Result.success(url);
    }
}
