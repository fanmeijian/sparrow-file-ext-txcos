package cn.sparrowmini.file.ext.txcos;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.StorageClass;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.utils.IOUtils;
import com.tencent.cloud.CosStsClient;
import com.tencent.cloud.Response;

@Service
public class ObjectStorageServiceImpl implements ObjectStorageService {

    @Autowired
    private CosConfig config;

    @Autowired
    private CosFileRepository cosFileRepository;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private HttpServletResponse response;

    @Override
    public Response getUploadTmpKey(String fileName, String path) {
        String[] allowActions = new String[]{
                // 简单上传
                "name/cos:PutObject",
                // 表单上传、小程序上传
                "name/cos:PostObject",
                // 分块上传
                "name/cos:InitiateMultipartUpload", "name/cos:ListMultipartUploads", "name/cos:ListParts",
                "name/cos:UploadPart", "name/cos:CompleteMultipartUpload"};
        return this.getTmpkey(fileName, allowActions, path);
    }

    @Override
    public Response getDownloadTmpKey(String fileName, String path) {
        String[] allowActions = new String[]{
                // 下载
                "name/cos:GetObject"};
        return this.getTmpkey(fileName, allowActions, path);
    }

    private Response getTmpkey(String fileName, String[] allowActions, String path) {
        TreeMap<String, Object> config = new TreeMap<String, Object>();

        try {
            // 这里的 SecretId 和 SecretKey 代表了用于申请临时密钥的永久身份（主账号、子账号等），子账号需要具有操作存储桶的权限。
            // 替换为您的云 api 密钥 SecretId
            config.put("secretId", this.config.getSecretId());
            // 替换为您的云 api 密钥 SecretKey
            config.put("secretKey", this.config.getSecretKey());

            // 设置域名:
            // 如果您使用了腾讯云 cvm，可以设置内部域名
            // config.put("host", "sts.internal.tencentcloudapi.com");

            // 临时密钥有效时长，单位是秒，默认 1800 秒，目前主账号最长 2 小时（即 7200 秒），子账号最长 36 小时（即 129600）秒
            config.put("durationSeconds", 300);

            // 换成您的 bucket
            config.put("bucket", this.config.getBucket());// sportunione-1252583813
            // 换成 bucket 所在地区
            config.put("region", this.config.getRegion());// ap-guangzhou

            // 这里改成允许的路径前缀，可以根据自己网站的用户登录态判断允许上传的具体路径
            // 列举几种典型的前缀授权场景：
            // 1、允许访问所有对象："*"
            // 2、允许访问指定的对象："a/a1.txt", "b/b1.txt"
            // 3、允许访问指定前缀的对象："a*", "a/*", "b/*"
            // 如果填写了“*”，将允许用户访问所有资源；除非业务需要，否则请按照最小权限原则授予用户相应的访问权限范围。
            config.put("allowPrefixes",  this.config.getAllowPrefixes());// upload/*"

            // 密钥的权限列表。必须在这里指定本次临时密钥所需要的权限。
            // 简单上传、表单上传和分块上传需要以下的权限，其他权限列表请看
            // https://cloud.tencent.com/document/product/436/31923

            config.put("allowActions", allowActions);

            Response response = CosStsClient.getCredential(config);

            // System.out.println(response.credentials.tmpSecretId);
            // System.out.println(response.credentials.tmpSecretKey);
            // System.out.println(response.credentials.sessionToken);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("no valid secret !");
        }
    }

    @Transactional
    @Override
    public CosFile upload(MultipartFile file) {

        // 调用 COS 接口之前必须保证本进程存在一个 COSClient 实例，如果没有则创建
        // 详细代码参见本页：简单操作 -> 创建 COSClient
        COSClient cosClient = createCOSClient();

        // 存储桶的命名格式为 BucketName-APPID，此处填写的存储桶名称必须为此格式
        String bucketName = this.config.getBucket();
        // 对象键(Key)是对象在存储桶中的唯一标识。

        // 这里创建一个 ByteArrayInputStream 来作为示例，实际中这里应该是您要上传的 InputStream 类型的流

        InputStream inputStream;
        try {
            byte data[] = file.getBytes();
            String key = DigestUtils.md5Hex(data).toUpperCase();

            CosFile cosFile = new CosFile();
            cosFile.setBucket(this.config.getBucket());
            cosFile.setRegion(this.config.getRegion());
            cosFile.setName(key);
            cosFile.setFileName(file.getOriginalFilename());

            this.cosFileRepository.save(cosFile);
            inputStream = new ByteArrayInputStream(data);
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
            cosFile.setUrl(String.join("/", url, "cos/tx", cosFile.getId(), "download"));
            this.cosFileRepository.save(cosFile);

            return cosFile;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (CosServiceException e) {
            e.printStackTrace();
        } catch (CosClientException e) {
            e.printStackTrace();
        }

        // 确认本进程不再使用 cosClient 实例之后，关闭即可
        cosClient.shutdown();
        return null;

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

    @Override
    public byte[] download(String fileId) {
        CosFile cosFile = this.cosFileRepository.findById(fileId).get();
        // 调用 COS 接口之前必须保证本进程存在一个 COSClient 实例，如果没有则创建
        // 详细代码参见本页：简单操作 -> 创建 COSClient
        COSClient cosClient = createCOSClient();

        // 存储桶的命名格式为 BucketName-APPID，此处填写的存储桶名称必须为此格式
        String bucketName = cosFile.getBucket();
        // 对象键(Key)是对象在存储桶中的唯一标识。详情请参见
        // [对象键](https://cloud.tencent.com/document/product/436/13324)
        String key = cosFile.getName();

        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, key);
        InputStream cosObjectInput = null;

        try {
            COSObject cosObject = cosClient.getObject(getObjectRequest);
            cosObjectInput = cosObject.getObjectContent();
        } catch (CosServiceException e) {
            e.printStackTrace();
        } catch (CosClientException e) {
            e.printStackTrace();
        }

        // 处理下载到的流
        // 这里是直接读取，按实际情况来处理
        byte[] bytes = null;
        try {
            bytes = IOUtils.toByteArray(cosObjectInput);
            // 在流没有处理完之前，不能关闭 cosClient
            // 确认本进程不再使用 cosClient 实例之后，关闭即可
            cosClient.shutdown();

            response.setHeader("Content-Disposition", "attachment; filename=\"" + cosFile.getFileName() + "\"");

            return bytes;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 用完流之后一定要调用 close()
            try {
                cosObjectInput.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // 在流没有处理完之前，不能关闭 cosClient
        // 确认本进程不再使用 cosClient 实例之后，关闭即可
        cosClient.shutdown();
        return bytes;

    }

    @Override
    public CosFile createFile(CosFile file) {
        return this.cosFileRepository.save(file);
    }

}
