package eu.bcvsolutions.idm.core.bulk.action.impl.event;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractRemoveBulkAction;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityEventDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityEventFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmEntityEventService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission;

/**
 * Delete entity events from queue
 * 
 * @author Radek Tomiška
 *
 */
@Component(EntityEventDeleteBulkAction.NAME)
@Description("Delete entity events from queue.")
public class EntityEventDeleteBulkAction extends AbstractRemoveBulkAction<IdmEntityEventDto, IdmEntityEventFilter> {

	public static final String NAME = "core-entity-event-delete-bulk-action";

	@Autowired private IdmEntityEventService service;
	
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(IdmGroupPermission.APP_ADMIN);
	}

	@Override
	public ReadWriteDtoService<IdmEntityEventDto, IdmEntityEventFilter> getService() {
		return service;
	}
	
	@Override
	protected OperationResult processDto(IdmEntityEventDto dto) {
		if (service.get(dto) == null) {
			// already deleted - skipping
			return new OperationResult.Builder(OperationState.EXECUTED).build();
		}
		try {
			this.getService().delete(dto);
			return new OperationResult.Builder(OperationState.EXECUTED).build();
		} catch(ResultCodeException ex) {
			return new OperationResult.Builder(OperationState.EXCEPTION).setException(ex).build();
		} catch (EmptyResultDataAccessException ex) {
			// already deleted - skipping
			return new OperationResult.Builder(OperationState.EXECUTED).build();
		} catch(Exception ex) {
			return new OperationResult.Builder(OperationState.EXCEPTION).setCause(ex).build();
		}
	}
}
