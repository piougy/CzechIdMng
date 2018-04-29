package eu.bcvsolutions.idm.core.api.bulk.operation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.bcvsolutions.idm.core.api.bulk.operation.dto.IdmBulkOperationDto;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.utils.FilterConverter;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractLongRunningTaskExecutor;

/**
 * Abstract parent for all bulk operations
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 * @param <DTO>
 */
public abstract class AbstractBulkOperation<DTO extends BaseDto>
		extends AbstractLongRunningTaskExecutor<Boolean>
		implements IdmBulkOperation<DTO> {

	protected static final int PAGE_SIZE = 100;
	@Autowired
	private LookupService lookupService;
	@Autowired(required = false)
	@Qualifier("objectMapper")
	private ObjectMapper mapper;
	private FilterConverter filterConverter;
	
	private IdmBulkOperationDto operation;
	
	public IdmBulkOperationDto getOperation() {
		return operation;
	}

	public void setOperation(IdmBulkOperationDto operation) {
		this.operation = operation;
	}
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		return new ArrayList<>();
	}
	
	protected Pageable getPageable() {
		return new PageRequest(0, PAGE_SIZE);
	}
	
	protected FilterConverter getParameterConverter() {
		if (filterConverter == null) {
			filterConverter = new FilterConverter(lookupService, mapper);
		}
		return filterConverter;
	}
	
	protected BaseFilter toFilter(Map<String, Object> parameters) {
		return getParameterConverter().toFilter(parameters, getFilterClazz());
	}
	
	private Class<? extends BaseFilter> getFilterClazz( ) {
		String filterClass = getFilterClass();
		try {
			Class<?> forName = Class.forName(filterClass);
			if (BaseFilter.class.isAssignableFrom(forName)) {
				return (Class<? extends BaseFilter>) Class.forName(filterClass);
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
