package eu.bcvsolutions.idm.core.api.bulk.action;

import java.util.List;

import org.springframework.core.Ordered;
import org.springframework.plugin.core.Plugin;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;

/**
 * Interface for bulk operation
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public interface IdmBulkAction<DTO extends AbstractDto, F extends BaseFilter>
		extends Plugin<Class<? extends BaseEntity>>, Ordered {

	int DEFAULT_ORDER = 0;
	
	/**
	 * Get list of form attributes
	 *
	 * @return
	 */
	List<IdmFormAttributeDto> getFormAttributes();

	/**
	 * Get name of bulk action
	 *
	 * @return
	 */
	String getName();
	
	/**
	 * Get bulk action
	 *
	 * @return
	 */
	IdmBulkActionDto getAction();

	/**
	 * Set bulk action
	 *
	 * @param action
	 */
	void setAction(IdmBulkActionDto action);
	
	/**
	 * Get module
	 *
	 * @return
	 */
	String getModule();
	
	/**
	 * Return service. With the service will be executed bulk action.
	 *
	 * @return
	 */
	ReadWriteDtoService<DTO, F> getService();
	
	/**
	 * Validate given bulk action. Is necessary specify the action by setter {@link IdmBulkAction#setAction(IdmBulkActionDto)}
	 */
	void validate();
	
	/**
	 * Return authorities required for process one item.
	 *
	 * @return
	 */
	List<String> getAuthorities();
}
