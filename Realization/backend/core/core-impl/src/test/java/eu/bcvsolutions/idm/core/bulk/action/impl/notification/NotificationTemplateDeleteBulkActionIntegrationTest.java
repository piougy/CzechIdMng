package eu.bcvsolutions.idm.core.bulk.action.impl.notification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationTemplateDto;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationTemplateFilter;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationTemplateService;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationTemplate;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Integration tests for {@link NotificationTemplateDeleteBulkAction}
 *
 * @author Ondrej Husnik
 *
 */

public class NotificationTemplateDeleteBulkActionIntegrationTest extends AbstractBulkActionTest {
	
	private static final String TEST_TEMPLATE = "testTemplate";
	private static final String TEST_TEMPLATE_TWO = "testTemplateTwo";

	@Autowired
	private IdmNotificationTemplateService templateService;
	
	@Before
	public void login() {
		IdmIdentityDto adminIdentity = this.createUserWithAuthorities(IdmBasePermission.DELETE, IdmBasePermission.READ);
		loginAsNoAdmin(adminIdentity.getUsername());
	}
	
	@After
	public void logout() {
		super.logout();
	}
	
	@Test
	public void processBulkActionByIds() {
		templateService.init();
		IdmNotificationTemplateDto template1 = templateService.getByCode(TEST_TEMPLATE);
		IdmNotificationTemplateDto template2 = templateService.getByCode(TEST_TEMPLATE_TWO);
		template1.setUnmodifiable(false);
		template2.setUnmodifiable(false);
		template1 = templateService.save(template1);
		template2 = templateService.save(template2);
		
		Set<UUID> templates = new HashSet<UUID>();
		templates.add(template1.getId());
		templates.add(template2.getId());
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmNotificationTemplate.class, NotificationTemplateDeleteBulkAction.NAME);
		bulkAction.setIdentifiers(templates);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 2l, null, null);
		
		for (UUID id : templates) {
			IdmNotificationTemplateDto templateDto = templateService.get(id);
			assertNull(templateDto);
		}
	}
	

	@Test
	public void processBulkActionByFilter() {
		templateService.init();
		String desc = "script description" + getHelper().createName();
		IdmNotificationTemplateDto template1 = templateService.getByCode(TEST_TEMPLATE);
		IdmNotificationTemplateDto template2 = templateService.getByCode(TEST_TEMPLATE_TWO);
		template1.setUnmodifiable(false);
		template1.setSubject(desc);
		template2.setUnmodifiable(false);
		template2.setSubject(desc);
		template1 = templateService.save(template1);
		template2 = templateService.save(template2);
		
		Set<UUID> templates = new HashSet<UUID>();
		templates.add(template1.getId());
		templates.add(template2.getId());
		
		IdmNotificationTemplateFilter filter = new IdmNotificationTemplateFilter();
		filter.setText(desc);

		List<IdmNotificationTemplateDto> checkTemplates = templateService.find(filter, null).getContent();
		assertEquals(2, checkTemplates.size());

		IdmBulkActionDto bulkAction = this.findBulkAction(IdmNotificationTemplate.class, NotificationTemplateDeleteBulkAction.NAME);
		bulkAction.setTransformedFilter(filter);
		bulkAction.setFilter(toMap(filter));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 2l, null, null);
		
		for (UUID id : templates) {
			IdmNotificationTemplateDto templateDto = templateService.get(id);
			assertNull(templateDto);
		}
	}
	
	
	@Test
	public void prevalidationBulkActionByIds() {
		templateService.init();
		IdmNotificationTemplateDto template1 = templateService.getByCode(TEST_TEMPLATE);
		IdmNotificationTemplateDto template2 = templateService.getByCode(TEST_TEMPLATE_TWO);
		template1.setUnmodifiable(true);
		template2.setUnmodifiable(false);
		template1 = templateService.save(template1);
		template2 = templateService.save(template2);
		
		Set<UUID> templates = new HashSet<UUID>();
		templates.add(template1.getId());
		templates.add(template2.getId());
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmNotificationTemplate.class, NotificationTemplateDeleteBulkAction.NAME);
		bulkAction.setIdentifiers(templates);		
		ResultModels resultModels = bulkActionManager.prevalidate(bulkAction);
		List<ResultModel> results = resultModels.getInfos();
		assertEquals(1, results.size());
		assertEquals(results.get(0).getStatusEnum(), CoreResultCode.NOTIFICATION_SYSTEM_TEMPLATE_DELETE_FAILED.toString());
		String code = (String)results.get(0).getParameters().get("template");
		assertEquals(code, template1.getCode());
	}
	

	@Test
	public void processBulkActionWithoutPermission() {
		templateService.init();
		IdmNotificationTemplateDto template1 = templateService.getByCode(TEST_TEMPLATE);
		IdmNotificationTemplateDto template2 = templateService.getByCode(TEST_TEMPLATE_TWO);
		template1.setUnmodifiable(false);
		template2.setUnmodifiable(false);
		template1 = templateService.save(template1);
		template2 = templateService.save(template2);
		
		Set<UUID> templates = new HashSet<UUID>();
		templates.add(template1.getId());
		templates.add(template2.getId());
		
		// user hasn't permission for update role
		IdmIdentityDto readerIdentity = this.createUserWithAuthorities(IdmBasePermission.READ);
		loginAsNoAdmin(readerIdentity.getUsername());
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmNotificationTemplate.class, NotificationTemplateDeleteBulkAction.NAME);
		bulkAction.setIdentifiers(templates);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 0l, null, null);
		
		for (UUID id : templates) {
			IdmNotificationTemplateDto templateDto = templateService.get(id);
			assertNotNull(templateDto);
		}
	}
}
