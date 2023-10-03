package cn.sparrowmini.file.service;

import java.io.FilePermission;

import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import cn.sparrowmini.common.restapi.BaseJpaService;
import cn.sparrowmini.file.model.SparrowFile;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping("/files-mgt")
@Tag(name = "file", description = "文件服务")
public interface FileService extends BaseJpaService<SparrowFile, String> {

	@GetMapping("/{id}/download")
	@ResponseBody
	public Resource dowload(@PathVariable String id);

	@PostMapping("/{id}/share")
	@ResponseBody
	public void share(@PathVariable String id, @RequestBody FilePermission filePermission);

	@PostMapping("/{id}/forward")
	@ResponseBody
	public void forward(@PathVariable String id, @RequestBody String[] user);

	@PostMapping("/upload")
	@ResponseBody
	public String upload(MultipartFile file);
}
