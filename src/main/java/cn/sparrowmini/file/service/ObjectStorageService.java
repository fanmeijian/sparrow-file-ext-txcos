package cn.sparrowmini.file.service;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tencent.cloud.Response;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "objectStorage")
@RequestMapping(value = "objectStorages")
public interface ObjectStorageService {
    @GetMapping(value = "/uploadTmpKeys", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response getUploadTmpKey(@RequestParam String fileName);

    @GetMapping(value = "/downloadTmpKeys", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response getDownloadTmpKey(@RequestParam String fileName);

}
