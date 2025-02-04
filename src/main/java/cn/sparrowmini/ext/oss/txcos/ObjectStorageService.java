package cn.sparrowmini.ext.oss.txcos;

import cn.sparrowmini.file.model.BaseCosFile;
import cn.sparrowmini.file.model.CosFile;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.tencent.cloud.Response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "cos")
@RequestMapping(value = "cos/tx")
public interface ObjectStorageService {
	@GetMapping(value = "/uploadTmpKeys", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Response getUploadTmpKey(@Nullable @RequestParam String fileName,@Nullable @RequestParam String path);

	@GetMapping(value = "/downloadTmpKeys", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Response getDownloadTmpKey(@RequestParam String fileName, @RequestParam String path);

	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "上传文件到TXCOS", operationId = "uploadToTxCos")
	@ResponseBody
	public CosFile upload(@RequestParam MultipartFile file);
	
	@PostMapping(value = "/cos-files")
	@Operation(summary = "创建cosfile对象", operationId = "createCosFile")
	@ResponseBody
	public CosFile createFile(@RequestBody CosFile file);

	@GetMapping(value = "/{fileId}/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	@Operation(summary = "从TXCOS下载文件", operationId = "download")
	@ResponseBody
	public byte[] download(@PathVariable String fileId);

	public byte[] download(BaseCosFile cosFile);
}
