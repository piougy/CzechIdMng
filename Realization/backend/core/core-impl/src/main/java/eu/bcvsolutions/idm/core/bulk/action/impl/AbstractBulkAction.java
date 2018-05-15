package eu.bcvsolutions.idm.core.api.bulk.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractLongRunningTaskExecutor;

/**
 * Abstract parent for all bulk actions
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 * @param <DTO>
 */
public abstract class AbstractBulkAction<DTO extends BaseDto>
		extends AbstractLongRunningTaskExecutor<Boolean>
		implements IdmBulkAction<DTO> {

	private IdmBulkActionDto action;
	
	@Override
	public IdmBulkActionDto getAction() {
		return action;
	}

	@Override
	public void setAction(IdmBulkActionDto action) {
		this.action = action;
	}
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		return new ArrayList<>();
	}
	
	@Override
	public void validate() {
		Assert.notNull(action, "Action can't be null");
		//
		if (action.getIdentifiers() == null && action.getFilter() == null) {
			throw new ResultCodeException(CoreResultCode.BULK_ACTION_ENTITIES_ARE_NOT_SPECIFIED);
		}
		//
		if (action.getIdentifiers() != null && action.getFilter() != null) {
			throw new ResultCodeException(CoreResultCode.BULK_ACTION_ONLY_ONE_FILTER_CAN_BE_APPLIED);
		}
		//
		for (IdmFormAttributeDto attribute : this.getFormAttributes()) {
			if (attribute.isRequired()) {
				Object value = action.getProperties().get(attribute.getCode());
				if (value == null) {
					throw new ResultCodeException(CoreResultCode.BULK_ACTION_REQUIRED_PROPERTY, ImmutableMap.of("attributeCode", attribute.getCode()));
				} 
				if (attribute.isMultiple()) {
					Collection<?> multivaluedValue= (Collection<?>)value;
					if (multivaluedValue.isEmpty()) {
						throw new ResultCodeException(CoreResultCode.BULK_ACTION_REQUIRED_PROPERTY, ImmutableMap.of("attributeCode", attribute.getCode()));
					}
				}
			}
		}
	}
	
	@Override
	public Map<String, Object> getProperties() {
		if (this.getAction() == null) {
			return super.getProperties();
		}
		if (this.getAction().getProperties() == null) {
			return super.getProperties();
		}
		return this.getAction().getProperties();
	}
}
