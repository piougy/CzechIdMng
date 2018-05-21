package eu.bcvsolutions.idm.core.api.bulk.action;

import java.util.List;
import java.util.Map;

import org.springframework.core.Ordered;
import org.springframework.plugin.core.Plugin;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Interface for bulk operation
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public interface IdmBulkAction<DTO extends BaseDto>
		extends Plugin<Class<? extends AbstractEntity>>, Ordered {

	static int DEFAULT_ORDER = 0;
	
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
	 * Get filter class
	 *
	 * @return
	 */
	String getFilterClass();
	
	/**
	 * Get entity class
	 *
	 * @return
	 */
	String getEntityClass();
	
	/**
	 * Get module
	 *
	 * @return
	 */
	String getModule();
	
	/**
	 * Validate given bulk action. Is necessary specify the action by setter {@link IdmBulkAction#setAction(IdmBulkActionDto)}
	 */
	void validate();
	
	/**
	 * Return permissions required for process one item.
	 * Key is entity and value is array of {@link BasePermission}.
	 * @return
	 */
	Map<String, BasePermission[]> getPermissions();
}
