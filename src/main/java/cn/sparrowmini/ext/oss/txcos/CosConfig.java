package cn.sparrowmini.ext.oss.txcos;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "sparrow.cos.tx")
public class CosConfig {

	private String secretId;
	private String secretKey;
	private String region;
	private String bucket;
	private String[] allowPrefixes;
}
