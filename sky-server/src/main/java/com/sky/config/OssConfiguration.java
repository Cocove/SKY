package com.sky.config;

import com.sky.Utils.AliOSSUtils;
import com.sky.properties.AliOssProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class OssConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AliOSSUtils aliOSSUtils(AliOssProperties aliOssProperties){
        log.info("开始创建阿里云文件上传工具类对象：{}", aliOssProperties);
        return new AliOSSUtils(aliOssProperties.getEndpoint(),
                aliOssProperties.getBucketName(),
                aliOssProperties.getRegion()
        );
    }


}
