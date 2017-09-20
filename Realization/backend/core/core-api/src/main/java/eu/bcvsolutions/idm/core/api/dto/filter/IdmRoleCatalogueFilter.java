package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueDto;

/**
 * Default filter for role catalogue, parent
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 */
public class IdmRoleCatalogueFilter extends DataFilter {
	
	private UUID parent;
	private String name;
	private String code;
	
	public IdmRoleCatalogueFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmRoleCatalogueFilter(MultiValueMap<String, Object> data) {
		super(IdmRoleCatalogueDto.class, data);
	}

	public String getName() {
		return name;
	}

	public String getCode() {
		return code;
	}

	public void setName(String name) {
		this.name = name;
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
}
