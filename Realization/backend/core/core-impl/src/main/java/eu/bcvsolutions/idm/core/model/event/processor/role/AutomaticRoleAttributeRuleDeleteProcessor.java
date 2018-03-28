package eu.bcvsolutions.idm.core.model.event.processor.role;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeRuleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAutomaticRoleAttributeRuleRequestFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeRuleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeRuleService;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.model.event.AutomaticRoleAttributeRuleEvent.AutomaticRoleAttributeRuleEventType;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.task.impl.RemoveAutomaticRoleTaskExecutor;

/**
 * Delete role and do necessary recalculation (if rule is last).
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Component
@Description("Delete rule for automatic role.")
public class AutomaticRoleAttributeRuleDeleteProcessor extends CoreEventProcessor<IdmAutomaticRoleAttributeRuleDto> {

	public static final String PROCESSOR_NAME = "automatic-role-attribute-rule-delete-crocessor";
	
	public static final String SKIP_CHECK_LAST_RULE = "skipCheckLastRule";
	
	private final IdmAutomaticRoleAttributeRuleService automactiRoleAttributeRuleService;
	private final IdmAutomaticRoleAttributeService automaticRoleAttributeRuleService;
	private final LongRunningTaskManager longRunningTaskManager;
	private final IdmIdentityService identityService;
	@Autowired
	private IdmAutomaticRoleAttributeRuleRequestService ruleRequestService;
	
	@Autowired
	public AutomaticRoleAttributeRuleDeleteProcessor(
			IdmAutomaticRoleAttributeRuleService automactiRoleAttributeRuleService,
			LongRunningTaskManager longRunningTaskManage,
			IdmAutomaticRoleAttributeService automaticRoleAttributeRuleService,
			IdmIdentityService identityService) {
		super(AutomaticRoleAttributeRuleEventType.DELETE);
		//
		Assert.notNull(automactiRoleAttributeRuleService);
		Assert.notNull(longRunningTaskManage);
		Assert.notNull(automaticRoleAttributeRuleService);
		Assert.notNull(identityService);
		//
		this.automactiRoleAttributeRuleService = automactiRoleAttributeRuleService;
		this.longRunningTaskManager = longRunningTaskManage;
		this.automaticRoleAttributeRuleService = automaticRoleAttributeRuleService;
		this.identityService = identityService;
	}
	
	@Override
	public EventResult<IdmAutomaticRoleAttributeRuleDto> process(EntityEvent<IdmAutomaticRoleAttributeRuleDto> event) {
		IdmAutomaticRoleAttributeRuleDto dto = event.getContent();
		//
		List<IdmAutomaticRoleAttributeRuleDto> allRules = automactiRoleAttributeRuleService.findAllRulesForAutomaticRole(dto.getAutomaticRoleAttribute());
		//
		// in some case we want skip check for last rule (remove all automatic role)
		// by default is skip value null => false
		if (!this.getBooleanProperty(SKIP_CHECK_LAST_RULE, event.getProperties())) {
			// it's last rule, remove all identity role
			if (allRules.size() == 1 && dto.getId().equals(allRules.get(0).getId())) {
				// before we start delete identity role, we check how many identities has the auto role
				// if doesn't exist identities that has the role, skip remove
				IdmIdentityFilter identityFilter = new IdmIdentityFilter();
				long totalElements = identityService.find(identityFilter, new PageRequest(0, 1)).getTotalElements();
				if (totalElements > 0) {
					UUID automaticRoleAttributeId = dto.getAutomaticRoleAttribute();
					removeAllRoles(automaticRoleAttributeId);
					//
					// we also set concept to false
					IdmAutomaticRoleAttributeDto roleAttributeDto = automaticRoleAttributeRuleService.get(automaticRoleAttributeId);
					roleAttributeDto.setConcept(false);
					roleAttributeDto = automaticRoleAttributeRuleService.save(roleAttributeDto);
				}
			}
		}
		UUID automaticRuleId = dto.getId();
		// Find all automatic role requests and remove relation on rule
		if (automaticRuleId != null) {
			IdmAutomaticRoleAttributeRuleRequestFilter automaticRoleRequestFilter = new IdmAutomaticRoleAttributeRuleRequestFilter();
			automaticRoleRequestFilter.setRuleId(automaticRuleId);

			ruleRequestService.find(automaticRoleRequestFilter, null).getContent().forEach(request -> {
				request.setRule(null);
				ruleRequestService.save(request);
			});
		}
		
		//
		automactiRoleAttributeRuleService.deleteInternal(dto);
		//
		return new DefaultEventResult<>(event, this);
	}
	
	/**
	 * Remove all identity role via request, but automatic role iselft will not be deleted.
	 *
	 * @param automaticRoleId
	 */
	private void removeAllRoles(UUID automaticRoleId) {
		RemoveAutomaticRoleTaskExecutor automaticRoleTask = AutowireHelper.createBean(RemoveAutomaticRoleTaskExecutor.class);
		automaticRoleTask.setAutomaticRoleId(automaticRoleId);
		automaticRoleTask.setDeleteEntity(false); // we dont want delete entity
		longRunningTaskManager.executeSync(automaticRoleTask);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public boolean isDisableable() {
		return false;
	}
	
	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER;
	}

}
