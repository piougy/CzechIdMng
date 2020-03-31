package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;

/**
 * Filter for tree node.
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 */
public class IdmTreeNodeFilter 
		extends DataFilter 
		implements CorrelationFilter, ExternalIdentifiableFilter, DisableableFilter {

	public static final String PARAMETER_CODE = "code"; // PARAMETER_CODEABLE_IDENTIFIER can be used too
	/**
	 * Tree type identifier
	 */
	public static final String PARAMETER_TREE_TYPE_ID = "treeTypeId";
	/**
	 * Parent tree node identifier
	 * @since 9.4.0
	 */
	public static final String PARAMETER_PARENT = "parent";
	/**
	 * Search for tree nodes within the default tree type 
	 */
	public static final String PARAMETER_DEFAULT_TREE_TYPE = "defaultTreeType";
	/**
	 * Tree nodes by tree structure recursively down
	 */
	public static final String PARAMETER_RECURSIVELY = "recursively";
	public static final boolean DEFAULT_RECURSIVELY = true;
	
	/**
	 *  Search roots - true / false.
	 *  @since 9.4.0
	 */
	public static final String PARAMETER_ROOTS = "roots";

    public IdmTreeNodeFilter() {
        this(new LinkedMultiValueMap<>());
    }

    public IdmTreeNodeFilter(MultiValueMap<String, Object> data) {
        this(data, null);
    }
    
    public IdmTreeNodeFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
        super(IdmTreeNodeDto.class, data, parameterConverter);
    }

    public UUID getTreeTypeId() {
        return getParameterConverter().toUuid(getData(), PARAMETER_TREE_TYPE_ID);
    }

    public void setTreeTypeId(UUID treeTypeId) {
    	set(PARAMETER_TREE_TYPE_ID, treeTypeId);
    }
    
    public UUID getParent() {
    	return getParameterConverter().toUuid(getData(), PARAMETER_PARENT);
	}

	public void setParent(UUID parent) {
		set(PARAMETER_PARENT, parent);
	}

    public Boolean getDefaultTreeType() {
    	return getParameterConverter().toBoolean(data, PARAMETER_DEFAULT_TREE_TYPE);
    }

    public void setDefaultTreeType(Boolean defaultTreeType) {
    	set(PARAMETER_DEFAULT_TREE_TYPE, defaultTreeType);
    }

    public boolean isRecursively() {
    	return getParameterConverter().toBoolean(data, PARAMETER_RECURSIVELY, DEFAULT_RECURSIVELY);
    }

    public void setRecursively(boolean recursively) {
    	set(PARAMETER_RECURSIVELY, recursively);
    }
    
    public String getCode() {
		return (String) data.getFirst(PARAMETER_CODE);
	}

	public void setCode(String code) {
		set(PARAMETER_CODE, code);
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
		set(PARAMETER_ROOTS, roots);
	}
}
