package com.sky.Utils;

import com.aliyun.oss.*;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.common.comm.SignVersion;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.sky.properties.AliOssProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;


@Data
@AllArgsConstructor
@Slf4j
public class AliOSSUtils {


    private String endpoint; // 设置你的 OSS endpoint
    private String bucketName; // 填写你自己的 OSS 存储桶名称
    private String region;

    public String upload(MultipartFile file) throws IOException, ClientException, com.aliyuncs.exceptions.ClientException {
        // 区域，和你创建 OSS 存储桶时选择的区域一致
        // 从环境变量中获取凭证
        EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();

        // 获取上传的文件的输入流
        InputStream inputStream = file.getInputStream();

        // 避免文件覆盖，使用 UUID 来生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String fileName = UUID.randomUUID().toString() + originalFilename.substring(originalFilename.lastIndexOf("."));

        // 创建 OSS 客户端
        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);
        OSS ossClient = OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(clientBuilderConfiguration)
                .region(region)
                .build();


        String url = null;

        try {
            // 创建 PutObjectRequest 对象
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName, inputStream);
            // 上传文件
            PutObjectResult result = ossClient.putObject(putObjectRequest);

            // 构建文件访问的 URL
            url = String.format("https://%s.%s/%s", bucketName, endpoint.split("//")[1], fileName);
        } catch (OSSException oe) {
            System.err.println("Caught an OSSException, which means your request made it to OSS, but was rejected.");
            System.err.println("Error Message: " + oe.getErrorMessage());
            System.err.println("Error Code: " + oe.getErrorCode());
            System.err.println("Request ID: " + oe.getRequestId());
            System.err.println("Host ID: " + oe.getHostId());
        } catch (com.aliyun.oss.ClientException ce) {
            System.err.println("Caught a ClientException, which means the client encountered a serious internal problem.");
            System.err.println("Error Message: " + ce.getMessage());
        } finally {
            // 确保在操作完成后关闭客户端
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        // 返回上传的文件 URL
        return url;
    }
}
