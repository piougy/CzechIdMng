package eu.bcvsolutions.idm.core.event;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormDefinitionService;
import eu.bcvsolutions.idm.core.eav.processor.FormInstanceMonitoredFieldsProcessor;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationTemplateDto;
import eu.bcvsolutions.idm.core.notification.api.dto.NotificationConfigurationDto;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationFilter;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationConfigurationService;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationLogService;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationTemplateService;
import eu.bcvsolutions.idm.core.notification.entity.IdmEmailLog;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

public class FormInstanceMonitoredFieldsProcessorTest extends AbstractIntegrationTest {

	@Autowired
	private ConfigurationService configurationService;
	@Autowired
	private TestHelper testHelper;
	@Autowired
	private IdmNotificationLogService notificationLogService;
	@Autowired
	private IdmNotificationConfigurationService notificationConfigurationService;
	@Autowired
	private IdmNotificationTemplateService notificationTemplateService;
	@Autowired
	private IdmFormDefinitionService formDefinitionService;

	@Test
	public void testNotSendNotificationWhenNull() {
		String eavName = "FVCNtestEavName001";
		String roleName = "FVCNtestRoleName001";
		String type = "IdmIdentity";
		String valueBefore = "valueBefore";
		String valueAfter = "valueAfter";
		// config
		configurationService.setValue(createProperty(type, FormInstanceMonitoredFieldsProcessor.NAME_SUFFIX), eavName);
		configurationService.setValue(createProperty(type, FormInstanceMonitoredFieldsProcessor.ROLES_SUFFIX),
				roleName);
		//
		IdmRoleDto role = testHelper.createRole(roleName);
		IdmIdentityDto identity = testHelper.createIdentity();
		testHelper.createIdentityRole(identity, role);
		// identity for eav
		IdmIdentityDto eavIdentity = testHelper.createIdentity();
		// create notification config
		createNotificationConfiguration(type);
		// save eav value
		createFormDefinition(IdmIdentity.class.toString());
		IdmFormAttributeDto eavAttribute = testHelper.createEavAttribute(eavName, IdmIdentity.class,
				PersistentType.SHORTTEXT);
		// start processor
		testHelper.setEavValue(eavIdentity, eavAttribute, IdmIdentity.class, valueBefore, PersistentType.SHORTTEXT);
		// check if notification not created
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(identity.getUsername());
		filter.setNotificationType(IdmEmailLog.class);

		long count = notificationLogService.count(filter);
		assertEquals(0, count);

		// second test
		testHelper.setEavValue(eavIdentity, eavAttribute, IdmIdentity.class, valueAfter, PersistentType.SHORTTEXT);

		count = notificationLogService.count(filter);
		assertEquals(1, count);

		// nothing changed test
		testHelper.setEavValue(eavIdentity, eavAttribute, IdmIdentity.class, valueAfter, PersistentType.SHORTTEXT);

		count = notificationLogService.count(filter);
		assertEquals(1, count);

		testHelper.setEavValue(eavIdentity, eavAttribute, IdmIdentity.class, null, PersistentType.SHORTTEXT);

		count = notificationLogService.count(filter);
		//previous one stays
		assertEquals(1, count);
	}

	@Test
	public void testNoRoleSet() {
		String eavName = "FVCNtestEavName002";
		String roleName = "FVCNtestRoleName002";
		String type = "IdmIdentity";
		String valueBefore = "valueBefore";
		String valueAfter = "valueAfter";
		String last = "last";
		// config
		configurationService.setValue(createProperty(type, FormInstanceMonitoredFieldsProcessor.NAME_SUFFIX), eavName);
		configurationService.setValue(createProperty(type, FormInstanceMonitoredFieldsProcessor.ROLES_SUFFIX),
				roleName);
		//
		IdmRoleDto role = testHelper.createRole(roleName);
		IdmIdentityDto identity = testHelper.createIdentity();
		testHelper.createIdentityRole(identity, role);
		// identity for eav
		IdmIdentityDto eavIdentity = testHelper.createIdentity();
		// create notification config
		createNotificationConfiguration(type);
		// save eav value
		createFormDefinition(IdmIdentity.class.toString());
		IdmFormAttributeDto eavAttribute = testHelper.createEavAttribute(eavName, IdmIdentity.class,
				PersistentType.SHORTTEXT);
		// start processor
		testHelper.setEavValue(eavIdentity, eavAttribute, IdmIdentity.class, valueBefore, PersistentType.SHORTTEXT);
		// check if notification not created
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(identity.getUsername());
		filter.setNotificationType(IdmEmailLog.class);

		long count = notificationLogService.count(filter);
		assertEquals(0, count);

		testHelper.setEavValue(eavIdentity, eavAttribute, IdmIdentity.class, valueAfter, PersistentType.SHORTTEXT);

		count = notificationLogService.count(filter);
		assertEquals(1, count);

		configurationService.setValue(createProperty(type, FormInstanceMonitoredFieldsProcessor.ROLES_SUFFIX), "");
		testHelper.setEavValue(eavIdentity, eavAttribute, IdmIdentity.class, last, PersistentType.SHORTTEXT);

		count = notificationLogService.count(filter);
		assertEquals(1, count);
	}

	@Test
	public void testDifferentTypes() {
		String eavName = "FVCNtestEavName003";
		String roleName = "FVCNtestRoleName003";
		String type = "IdmIdentityContract";
		Integer valueBefore = 5;
		Integer valueAfter = 6;
		// config
		configurationService.setValue(createProperty(type, FormInstanceMonitoredFieldsProcessor.NAME_SUFFIX), eavName);
		configurationService.setValue(createProperty(type, FormInstanceMonitoredFieldsProcessor.ROLES_SUFFIX),
				roleName);
		//
		IdmRoleDto role = testHelper.createRole(roleName);
		IdmIdentityDto identity = testHelper.createIdentity();
		testHelper.createIdentityRole(identity, role);
		// identity for eav
		IdmIdentityDto eavIdentity = testHelper.createIdentity();
		IdmIdentityContractDto contract = testHelper.createIdentityContact(eavIdentity);
		// create notification config
		createNotificationConfiguration(type);
		// save eav value
		createFormDefinition(IdmIdentityContract.class.toString());
		IdmFormAttributeDto eavAttribute = testHelper.createEavAttribute(eavName, IdmIdentityContract.class,
				PersistentType.INT);
		// start processor
		testHelper.setEavValue(contract, eavAttribute, IdmIdentityContract.class, valueBefore, PersistentType.INT);
		// check if notification not created
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(identity.getUsername());
		filter.setNotificationType(IdmEmailLog.class);

		long count = notificationLogService.count(filter);
		assertEquals(0, count);

		testHelper.setEavValue(contract, eavAttribute, IdmIdentityContract.class, valueAfter, PersistentType.INT);

		count = notificationLogService.count(filter);
		assertEquals(1, count);
	}

	private String createProperty(String type, String suffix) {
		return FormInstanceMonitoredFieldsProcessor.CONFIGURATION_PREFIX + type + suffix;
	}

	private void createNotificationConfiguration(String type) {
		String typeTopic = type + FormInstanceMonitoredFieldsProcessor.TOPIC;
		String topic = String.format(CoreModuleDescriptor.MODULE_ID + ":%s", typeTopic);
		NotificationConfigurationDto notificationConfiguration =
				notificationConfigurationService.getConfigurationByTopicLevelNotificationType(
						topic,
						NotificationLevel.WARNING,
						IdmEmailLog.NOTIFICATION_TYPE);
		if (notificationConfiguration == null) {
			notificationConfiguration = new NotificationConfigurationDto();
			notificationConfiguration.setTopic(topic);
			notificationConfiguration.setNotificationType(IdmEmailLog.NOTIFICATION_TYPE);
			notificationConfiguration.setLevel(NotificationLevel.WARNING);

			//get notification template
			IdmNotificationTemplateDto idmNotificationTemplateDto =
					notificationTemplateService.getByCode(topic);
			if (idmNotificationTemplateDto == null) {
				idmNotificationTemplateDto = createNotificationTemplate(topic);
			}
			notificationConfiguration.setTemplate(idmNotificationTemplateDto.getId());
			notificationConfigurationService.save(notificationConfiguration);
		}
	}

	private IdmNotificationTemplateDto createNotificationTemplate(String topic){
		IdmNotificationTemplateDto idmNotificationTemplateDto = new IdmNotificationTemplateDto();
		idmNotificationTemplateDto.setCode(topic);
		idmNotificationTemplateDto.setSubject("TestSubject001");
		idmNotificationTemplateDto.setName(topic);
		return notificationTemplateService.save(idmNotificationTemplateDto);
	}

	private void createFormDefinition(String type) {
		if (formDefinitionService.findOneByTypeAndCode(type, IdmFormDefinitionService.DEFAULT_DEFINITION_CODE) == null) {
			IdmFormDefinitionDto formDefinition = new IdmFormDefinitionDto();
			formDefinition.setCode(IdmFormDefinitionService.DEFAULT_DEFINITION_CODE);
			formDefinition.setMain(true);
			formDefinition.setType(type);
			formDefinition.setName(IdmFormDefinitionService.DEFAULT_DEFINITION_CODE);
		}
	}
}