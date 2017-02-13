package eu.bcvsolutions.idm.core.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.NotEmpty;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * Type for tree nodes
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Entity
@Table(name = "idm_tree_type", indexes = { 
		@Index(name = "ux_tree_type_name", columnList = "name"), 
		@Index(name = "ux_tree_type_code", columnList = "code", unique = true) 
		})
public class IdmTreeType extends AbstractEntity {
	
	private static final long serialVersionUID = -3099001738101202320L;
	
	@Audited
	@NotEmpty
	@Size(min = 0, max = DefaultFieldLengths.NAME)
	@Column(name = "code", length = DefaultFieldLengths.NAME, nullable = false)
	private String code;
	
	@Audited
	@NotEmpty
	@Size(min = 0, max = DefaultFieldLengths.NAME)
	@Column(name = "name", length = DefaultFieldLengths.NAME, nullable = false)
	private String name;
	
	@Audited
	@Column(name = "default_tree_type", nullable = false)
	private boolean defaultTreeType = false;
	
	public String getCode() {
		return code;
	}
	
	public void setCode(String code) {
		this.code = code;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Returns true, when this type is defined as default organization structure
	 * 
	 * @return
	 */
	public boolean isDefaultTreeType() {
		return defaultTreeType;
	}
	
	public void setDefaultTreeType(boolean defaultTreeType) {
		this.defaultTreeType = defaultTreeType;
	}
}
