package eu.bcvsolutions.idm.core.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.NotEmpty;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.model.domain.IdmRuleCategory;

/**
 * Default entity for Rules
 * * Name
 * * Category
 * * Groovy script (string)
 * * description
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Entity
@Table(name = "idm_rule", indexes = { 
		@Index(name = "ux_rule_name", columnList = "name", unique = true), 
		@Index(name = "ux_rule_category", columnList = "category") 
		})
public class IdmRule  extends AbstractEntity {

	private static final long serialVersionUID = -3827618803196757060L;

	@Audited
	@NotEmpty
	@Size(min = 0, max = DefaultFieldLengths.NAME)
	@Column(name = "name", length = DefaultFieldLengths.NAME, nullable = false)
	private String name;
	
	@Audited
	@Column(name = "script")
	private String script;
	
	@Audited
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "category", nullable = false)
	private IdmRuleCategory category = IdmRuleCategory.DEFAULT;
	
	@Audited
	@Column(name = "description")
	private String description;

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public IdmRuleCategory getCategory() {
		return category;
	}

	public void setCategory(IdmRuleCategory category) {
		this.category = category;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	
}
