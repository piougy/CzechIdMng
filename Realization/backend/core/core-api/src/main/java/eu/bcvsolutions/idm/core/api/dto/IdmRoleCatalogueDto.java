package eu.bcvsolutions.idm.core.api.dto;

import java.util.UUID;

import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Embedded;

/**
 * Role catalogue DTO
 * 
 * @author Radek Tomiška
 *
 */
@Relation(collectionRelation = "roleCatalogues")
public class IdmRoleCatalogueDto extends AbstractDto implements Codeable {

	private static final long serialVersionUID = 1L;

	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	private String name;
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	private String code;
	@Embedded(dtoClass = IdmRoleCatalogueDto.class)
	private UUID parent;
	@Size(max = DefaultFieldLengths.DESCRIPTION)
	private String description;
	@Size(max = DefaultFieldLengths.NAME)
	private String url;
	@Size(max = DefaultFieldLengths.NAME)
	private String urlTitle;
	private long lft; // forest index
	private long rgt; // forest index

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public UUID getParent() {
		return parent;
	}

	public void setParent(UUID parent) {
		this.parent = parent;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrlTitle() {
		return urlTitle;
	}

	public void setUrlTitle(String urlTitle) {
		this.urlTitle = urlTitle;
	}

	public long getLft() {
		return lft;
	}

	public void setLft(long lft) {
		this.lft = lft;
	}

	public long getRgt() {
		return rgt;
	}

	public void setRgt(long rgt) {
		this.rgt = rgt;
	}

	/**
	 * Children count based on index
	 * 
	 * @return
	 */
	public Integer getChildrenCount() {
		return (int) ((rgt - lft) / 2);
	}
}
