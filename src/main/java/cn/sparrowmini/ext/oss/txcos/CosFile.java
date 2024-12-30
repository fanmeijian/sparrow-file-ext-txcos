package cn.sparrowmini.ext.oss.txcos;

import cn.sparrowmini.file.model.BaseCosFile;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
@Table(name = "spr_cos_file")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class CosFile extends BaseCosFile implements Serializable {

	private static final long serialVersionUID = 1L;



	@ElementCollection
	private Set<String> catalog;


	public CosFile(String key, String bucket){
		this.setName(key);
		this.setBucket(bucket);
	}
}
