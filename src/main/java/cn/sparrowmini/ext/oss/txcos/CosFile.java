package cn.sparrowmini.ext.oss.txcos;

import cn.sparrowmini.file.model.BaseCosFile;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
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

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@Id
	@GenericGenerator(name = "id-generator", strategy = "uuid")
	@GeneratedValue(generator = "id-generator")
	private String id;

	@ElementCollection
	private Set<String> catalog;


	public CosFile(String key, String bucket){
		this.setName(key);
		this.setBucket(bucket);
	}
}
