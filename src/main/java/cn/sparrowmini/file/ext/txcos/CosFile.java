package cn.sparrowmini.file.ext.txcos;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "spu_cos_file")
@Entity
@Data
@NoArgsConstructor
public class CosFile implements Serializable {

	private static final long serialVersionUID = 1L;

	private String bucket;
	private String region;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@Id
	@GenericGenerator(name = "id-generator", strategy = "uuid")
	@GeneratedValue(generator = "id-generator")
	private String id;

	private int seq;
	private String path;
	private String name;
	private long size;
	private String hash;
	private String fileName;
	private String type;
	@Column(length = 1000)
	private String url;

	@ElementCollection
	private Set<String> catalog;

}
