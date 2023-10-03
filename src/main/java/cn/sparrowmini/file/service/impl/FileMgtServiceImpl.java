package cn.sparrowmini.file.service.impl;

import java.io.FilePermission;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import cn.sparrowmini.common.restapi.impl.BaseJpaServiceImpl;
import cn.sparrowmini.file.model.SparrowFile;
import cn.sparrowmini.file.repository.FileMgtRepository;
import cn.sparrowmini.file.service.FileService;
import cn.sparrowmini.file.service.FileUtil;
import cn.sparrowmini.file.service.StorageService;

@Service
public class FileMgtServiceImpl extends BaseJpaServiceImpl<SparrowFile, String> implements FileService {

	private final StorageService storageService;

	@Autowired
	public FileMgtServiceImpl(StorageService storageService) {
		this.storageService = storageService;
	}

	@Autowired
	FileMgtRepository fileRepository;

	@Override
	public void share(String fileId, FilePermission filePermission) {

	}

	@Override
	public String upload(MultipartFile file) {
		// upload file and caculate the hash
		try {
			String shaChecksum = FileUtil.getChecksum(file.getInputStream());
			storageService.store(file.getInputStream(), shaChecksum);
			SparrowFile sparrowFile = new SparrowFile();
			sparrowFile.setName(file.getName());
			sparrowFile.setFileName(file.getOriginalFilename());
			sparrowFile.setHash(shaChecksum);
			sparrowFile.setUrl(storageService.load(shaChecksum).toString());
			fileRepository.save(sparrowFile);
			return sparrowFile.getId();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public Resource dowload(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void forward(String id, String[] user) {
		// TODO Auto-generated method stub

	}

}
