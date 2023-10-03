package cn.sparrowmini.file.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonProperty;

import cn.sparrowmini.common.CommonProp;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 文件对象
 */

@Getter
@Setter
@Entity(name = "spr_file1")
@Table(name = "spr_file1")
public class SparrowFile extends CommonProp {
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@EqualsAndHashCode.Include
	@Id
	@GenericGenerator(name = "id-generator", strategy = "uuid")
	@GeneratedValue(generator = "id-generator")
	private String id;

	private String name;
	private String fileName;
	private String type;
	private String url;
	private String hash;
}
