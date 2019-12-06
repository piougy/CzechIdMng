package eu.bcvsolutions.idm.core.api.exception;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.eav.api.dto.InvalidFormAttributeDto;

/**
 * Form doesn't pass validation for given attributes
 * 
 * @author Radek Tomiška
 * @since 9.4.0
 */
public class InvalidFormException extends ResultCodeException {

	private static final long serialVersionUID = 1L;
	//
	private final List<InvalidFormAttributeDto> attributes = new ArrayList<>();
	
	public InvalidFormException(List<InvalidFormAttributeDto> attributes) {
		super(CoreResultCode.FORM_INVALID, ImmutableMap.of(
				"attributes", attributes == null ? String.valueOf((Object) null) : attributes
				));
		if (attributes != null) {
			this.attributes.addAll(attributes);
		}
	}
	
	public List<InvalidFormAttributeDto> getAttributes() {
		return Lists.newArrayList(attributes);
	}
	
}
