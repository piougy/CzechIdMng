package eu.bcvsolutions.idm.core.bulk.action.impl.event;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.service.IdmEntityStateService;
import eu.bcvsolutions.idm.core.model.entity.IdmEntityState;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

import java.util.Arrays;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;


/**
 * Delete entity state from queue integration test
 *
 * @author artem
 */
public class EntityStateDeleteBulkActionIntegrationTest extends AbstractBulkActionTest {

    @Autowired
    private IdmEntityStateService service;

    @Before
    public void login() {
        loginAsAdmin();
    }

    @After
    public void logout() {
        super.logout();
    }

    @Test
    public void processBulkActionByIds() {

        List<OperationState> states = getStates();
        List<IdmEntityStateDto> operationStates = createOperationStates(states);

        IdmBulkActionDto bulkAction = findBulkAction(IdmEntityState.class, EntityStateDeleteBulkAction.NAME);

        Set<UUID> ids = getIdFromList(operationStates);
        bulkAction.setIdentifiers(ids);
        IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);

        checkResultLrt(processAction, (long) states.size(), null, null);

        for (UUID id : ids) {
            Assert.assertNull(service.get(id));
        }
    }

    @Test
    public void processBulkActionByFilter() {

        List<OperationState> states = getStates();
        List<IdmEntityStateDto> operationStates = createOperationStates(states);
        Set<UUID> ids = new HashSet<>();
        ids.add(operationStates.get(0).getId());
        ids.add(operationStates.get(2).getId());
        ids.add(operationStates.get(3).getId());

        IdmBulkActionDto bulkAction = findBulkAction(IdmEntityState.class, EntityStateDeleteBulkAction.NAME);
        bulkAction.setIdentifiers(ids);
        IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
        checkResultLrt(processAction, (long) ids.size(), null, null);

        Assert.assertNotNull(service.get(operationStates.get(1)));
        Assert.assertNotNull(service.get(operationStates.get(4)));
        Assert.assertNotNull(service.get(operationStates.get(5)));
        Assert.assertNotNull(service.get(operationStates.get(6)));
        Assert.assertNull(service.get(operationStates.get(0)));
        Assert.assertNull(service.get(operationStates.get(2)));
        Assert.assertNull(service.get(operationStates.get(3)));
    }

    private List<IdmEntityStateDto> createOperationStates(List<OperationState> states) {
        List<IdmEntityStateDto> results = new ArrayList<>();
        IdmEntityStateDto dto;
        for (OperationState operationState : states) {
            dto = new IdmEntityStateDto();
            dto.setOwnerId(UUID.randomUUID());
            dto.setOwnerType("mock");
            dto.setInstanceId("mock");
            dto.setResult(new OperationResultDto(operationState));
            results.add(service.save(dto));
        }

        return results;
    }

    private List<OperationState> getStates() {
        return Arrays.asList(
                OperationState.CREATED,
                OperationState.RUNNING,
                OperationState.EXECUTED,
                OperationState.EXCEPTION,
                OperationState.NOT_EXECUTED,
                OperationState.BLOCKED,
                OperationState.CANCELED
        );
    }
}
