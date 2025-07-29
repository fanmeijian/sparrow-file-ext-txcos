package cn.sparrowmini.ext.oss.txcos;

import cn.sparrowmini.common.service.DownloadPermission;
import com.tencent.cloud.CosStsClient;
import com.tencent.cloud.Response;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.util.TreeMap;

@Slf4j
@Service
@RestController
public class ObjectStorageServiceImpl implements ObjectStorageService {

    @Autowired
    private CosConfig config;


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

    @DownloadPermission
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
            config.put("allowPrefixes", this.config.getAllowPrefixes());// upload/*"

            // 密钥的权限列表。必须在这里指定本次临时密钥所需要的权限。
            // 简单上传、表单上传和分块上传需要以下的权限，其他权限列表请看
            // https://cloud.tencent.com/document/product/436/31923

            config.put("allowActions", allowActions);

            Response response = CosStsClient.getCredential(config);

            return response;
        } catch (Exception e) {
            throw new IllegalArgumentException("no valid secret !");
        }
    }

}
