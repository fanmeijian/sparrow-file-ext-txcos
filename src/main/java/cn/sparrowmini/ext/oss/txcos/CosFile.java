package cn.sparrowmini.ext.oss.txcos;

import cn.sparrowmini.common.model.BaseFile;
import cn.sparrowmini.common.model.TablePrefix;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = TablePrefix.NAME + "cos_file")
public class CosFile extends BaseFile {
}
