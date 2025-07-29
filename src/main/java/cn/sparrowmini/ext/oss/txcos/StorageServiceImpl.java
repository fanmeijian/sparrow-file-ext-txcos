package cn.sparrowmini.ext.oss.txcos;

import cn.sparrowmini.common.model.BaseFile;
import cn.sparrowmini.common.service.StorageService;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.*;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.utils.IOUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Service
public class StorageServiceImpl implements StorageService {
    @Autowired private ObjectStorageService objectStorageService;
    @Autowired
    private CosConfig config;


    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private HttpServletResponse response;
    @Override
    public byte[] download(BaseFile file) {
        COSClient cosClient = createCOSClient();

        // 存储桶的命名格式为 BucketName-APPID，此处填写的存储桶名称必须为此格式
        String bucketName = file.getBucket();
        // 对象键(Key)是对象在存储桶中的唯一标识。详情请参见
        // [对象键](https://cloud.tencent.com/document/product/436/13324)
        String key = file.getName();

        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, key);
        InputStream cosObjectInput = null;

        try {
            COSObject cosObject = cosClient.getObject(getObjectRequest);
            cosObjectInput = cosObject.getObjectContent();
        } catch (CosClientException e) {
            log.error(e.getMessage(), e);
        }

        // 处理下载到的流
        // 这里是直接读取，按实际情况来处理
        byte[] bytes = null;
        try {
            bytes = IOUtils.toByteArray(cosObjectInput);
            // 在流没有处理完之前，不能关闭 cosClient
            // 确认本进程不再使用 cosClient 实例之后，关闭即可
            cosClient.shutdown();

            response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getFileName() + "\"");

            return bytes;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            // 用完流之后一定要调用 close()
            try {
                cosObjectInput.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }

        // 在流没有处理完之前，不能关闭 cosClient
        // 确认本进程不再使用 cosClient 实例之后，关闭即可
        cosClient.shutdown();
        return bytes;
    }

    @Override
    public BaseFile upload(InputStream inputStream, String fileName) {

        // 调用 COS 接口之前必须保证本进程存在一个 COSClient 实例，如果没有则创建
        // 详细代码参见本页：简单操作 -> 创建 COSClient
        COSClient cosClient = createCOSClient();

        // 存储桶的命名格式为 BucketName-APPID，此处填写的存储桶名称必须为此格式
        String bucketName = this.config.getBucket();
        // 对象键(Key)是对象在存储桶中的唯一标识。

        // 这里创建一个 ByteArrayInputStream 来作为示例，实际中这里应该是您要上传的 InputStream 类型的流

        try {
            byte data[] = inputStream.readAllBytes();
            String key = DigestUtils.md5Hex(data).toUpperCase();

            BaseFile cosFile = new BaseFile();
            cosFile.setBucket(this.config.getBucket());
            cosFile.setRegion(this.config.getRegion());
            cosFile.setName(key);
            cosFile.setFileName(fileName);

            ObjectMetadata objectMetadata = new ObjectMetadata();
            // 上传的流如果能够获取准确的流长度，则推荐一定填写 content-length
            // 如果确实没办法获取到，则下面这行可以省略，但同时高级接口也没办法使用分块上传了
            objectMetadata.setContentLength(inputStream.available());

            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, inputStream, objectMetadata);

            // 设置存储类型（如有需要，不需要请忽略此行代码）, 默认是标准(Standard), 低频(standard_ia)
            // 更多存储类型请参见 https://cloud.tencent.com/document/product/436/33417
            putObjectRequest.setStorageClass(StorageClass.Standard_IA);
            PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);

            cosFile.setHash(putObjectResult.getContentMd5());
            String url = httpServletRequest.getRequestURL().toString().replace(httpServletRequest.getServletPath(), "");

            return cosFile;
        } catch (IOException |CosClientException e) {
            log.error(e.getMessage(), e);
        }
        // 确认本进程不再使用 cosClient 实例之后，关闭即可
        cosClient.shutdown();
        return null;
    }

    @Override
    public void remove(BaseFile file) {

    }

    // 创建 COSClient 实例，这个实例用来后续调用请求
    private COSClient createCOSClient() {
        // 设置用户身份信息。
        // SECRETID 和 SECRETKEY 请登录访问管理控制台 https://console.cloud.tencent.com/cam/capi
        // 进行查看和管理
        String secretId = this.config.getSecretId();// 用户的 SecretId，建议使用子账号密钥，授权遵循最小权限指引，降低使用风险。子账号密钥获取可参见
        // https://cloud.tencent.com/document/product/598/37140
        String secretKey = this.config.getSecretKey();// 用户的 SecretKey，建议使用子账号密钥，授权遵循最小权限指引，降低使用风险。子账号密钥获取可参见
        // https://cloud.tencent.com/document/product/598/37140
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);

        // ClientConfig 中包含了后续请求 COS 的客户端设置：
        ClientConfig clientConfig = new ClientConfig();

        // 设置 bucket 的地域
        // COS_REGION 请参见 https://cloud.tencent.com/document/product/436/6224
        clientConfig.setRegion(new Region(this.config.getRegion()));

        // 设置请求协议, http 或者 https
        // 5.6.53 及更低的版本，建议设置使用 https 协议
        // 5.6.54 及更高版本，默认使用了 https
        clientConfig.setHttpProtocol(HttpProtocol.https);

        // 以下的设置，是可选的：

        // 设置 socket 读取超时，默认 30s
        clientConfig.setSocketTimeout(30 * 1000);
        // 设置建立连接超时，默认 30s
        clientConfig.setConnectionTimeout(30 * 1000);

        // 如果需要的话，设置 http 代理，ip 以及 port
//		clientConfig.setHttpProxyIp("httpProxyIp");
//		clientConfig.setHttpProxyPort(80);

        // 生成 cos 客户端。
        return new COSClient(cred, clientConfig);
    }
}
