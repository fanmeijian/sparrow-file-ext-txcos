package cn.sparrowmini.ext.oss.txcos;


import com.tencent.cloud.Response;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@Tag(name = "cos")
@RequestMapping(value = "cos/tx")
public interface ObjectStorageService {
	@GetMapping(value = "/uploadTmpKeys", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Response getUploadTmpKey(String fileName, String path);

	@GetMapping(value = "/downloadTmpKeys", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Response getDownloadTmpKey(String fileName, String path);

}
