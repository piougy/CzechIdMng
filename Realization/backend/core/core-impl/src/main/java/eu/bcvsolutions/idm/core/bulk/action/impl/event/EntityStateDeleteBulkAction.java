package eu.bcvsolutions.idm.core.bulk.action.impl.event;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractRemoveBulkAction;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityStateFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.EntityStateManager;
import eu.bcvsolutions.idm.core.api.service.IdmEntityStateService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

/**
 * Delete entity's states from queue
 *
 * @author artem
 */
@Component(EntityStateDeleteBulkAction.NAME)
@Description("Delete entity state from queue.")
public class EntityStateDeleteBulkAction extends AbstractRemoveBulkAction<IdmEntityStateDto, IdmEntityStateFilter> {

    public static final String NAME = "core-entity-state-delete-bulk-action";
    //
    @Autowired private IdmEntityStateService service;
    @Autowired private EntityStateManager entityStateManager;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public ReadWriteDtoService<IdmEntityStateDto, IdmEntityStateFilter> getService() {
        return service;
    }

    @Override
    protected OperationResult processDto(IdmEntityStateDto dto) {
        if (service.get(dto) == null) {
            // already deleted - skipping
            return new OperationResult.Builder(OperationState.EXECUTED).build();
        }
        try {
            entityStateManager.deleteState(dto);
            return new OperationResult.Builder(OperationState.EXECUTED).build();
        } catch (ResultCodeException ex) {
            return new OperationResult.Builder(OperationState.EXCEPTION).setException(ex).build();
        } catch (EmptyResultDataAccessException ex) {
            // already deleted - skipping
            return new OperationResult.Builder(OperationState.EXECUTED).build();
        } catch (Exception ex) {
            return new OperationResult.Builder(OperationState.EXCEPTION).setCause(ex).build();
        }
    }
}
