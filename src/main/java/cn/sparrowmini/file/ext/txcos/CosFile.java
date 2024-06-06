package cn.sparrowmini.file.ext.txcos;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Table;

import cn.sparrowmini.common.model.BaseFile;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "spu_cos_file")
@Entity
@Data
@NoArgsConstructor
public class CosFile extends BaseFile implements Serializable {

	private static final long serialVersionUID = 1L;
	
    private String bucket;
    private String region;

}
