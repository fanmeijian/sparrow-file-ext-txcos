package cn.sparrowmini.file.repository;

import cn.sparrowmini.common.api.repository.CommonPropRepository;
import cn.sparrowmini.file.model.SparrowFile;

public interface FileMgtRepository extends CommonPropRepository<SparrowFile, String> {

//	@Query("SELECT s FROM SprFile s,UserFile uf WHERE s.id = uf.id.fileId AND uf.id.permission = 'LIST' AND uf.id.username IN (?1,'ANONYMOUSE','AUTHENTICATED')")
//	List<SparrowFile> listByPermission(String username);
//	
//	@Query("SELECT s FROM SprFile s,UserFile uf WHERE s.id = uf.id.fileId AND uf.id.permission = 'DOWNLOAD' AND uf.id.username IN (?1,'ANONYMOUSE','AUTHENTICATED') AND s.id=?2")
//	SparrowFile getByUsernameAndFileId(String username,String fileId);
}
