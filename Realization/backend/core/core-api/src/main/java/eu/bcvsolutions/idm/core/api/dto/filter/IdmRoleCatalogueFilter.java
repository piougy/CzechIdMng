package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueDto;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;

/**
 * Default filter for role catalogue, parent
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 */
public class IdmRoleCatalogueFilter extends DataFilter implements ExternalIdentifiable {
	
	/**
	 * Parent catalogue item identifier 
	 */
	public static final String PARAMETER_PARENT = IdmTreeNodeFilter.PARAMETER_PARENT;
	/**
	 *  Search roots - true / false.
	 *  @since 9.4.0
	 */
	public static final String PARAMETER_ROOTS = IdmTreeNodeFilter.PARAMETER_ROOTS;
	//
	public static final String PARAMETER_NAME = "name";
	public static final String PARAMETER_CODE = "code";
	/**
	 * Catalogue items recursively down
	 */
	public static final String PARAMETER_RECURSIVELY = "recursively";
	public static final boolean DEFAULT_RECURSIVELY = true;
	
	
	public IdmRoleCatalogueFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmRoleCatalogueFilter(MultiValueMap<String, Object> data) {
		super(IdmRoleCatalogueDto.class, data);
	}

	public String getName() {
		return (String) data.getFirst(PARAMETER_NAME);
	}

	public String getCode() {
		return (String) data.getFirst(PARAMETER_CODE);
	}

	public void setName(String name) {
		data.set(PARAMETER_NAME, name);
	}

	public void setCode(String code) {
		data.set(PARAMETER_CODE, code);
	}

	public UUID getParent() {
		return DtoUtils.toUuid(data.getFirst(PARAMETER_PARENT));
	}

	public void setParent(UUID parent) {
		data.set(PARAMETER_PARENT, parent);
	}
	
	@Override
	public String getExternalId() {
		return (String) data.getFirst(PROPERTY_EXTERNAL_ID);
	}
	
	@Override
	public void setExternalId(String externalId) {
		data.set(PROPERTY_EXTERNAL_ID, externalId);
	}
	
	/**
	 * @since 9.4.0
	 * @return
	 */
	public Boolean getRoots() {
    	return getParameterConverter().toBoolean(data, PARAMETER_ROOTS);
	}
	
	/**
	 * @since 9.4.0
	 * @param roots
	 */
	public void setRoots(Boolean roots) {
		data.set(PARAMETER_ROOTS, roots);
	}
	
	/**
	 * @since 9.4.0
	 * @return
	 */
	public boolean isRecursively() {
    	return getParameterConverter().toBoolean(data, PARAMETER_RECURSIVELY, DEFAULT_RECURSIVELY);
    }

	/**
	 * @since 9.4.0
	 * @param recursively
	 */
    public void setRecursively(boolean recursively) {
    	data.set(PARAMETER_RECURSIVELY, recursively);
    }
}
