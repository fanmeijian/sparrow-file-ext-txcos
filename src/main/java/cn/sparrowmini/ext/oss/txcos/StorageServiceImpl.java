package cn.sparrowmini.ext.oss.txcos;

import cn.sparrowmini.file.model.BaseCosFile;
import cn.sparrowmini.file.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StorageServiceImpl implements StorageService {
    @Autowired private ObjectStorageService objectStorageService;

    @Override
    public byte[] download(Object params) {
        return this.objectStorageService.download((BaseCosFile) params);
    }

    @Override
    public String upload(MultipartFile file) {
        return "";
    }

    @Override
    public void remove(Object params) {

    }
}
