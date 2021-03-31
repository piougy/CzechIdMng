package eu.bcvsolutions.idm.core.bulk.action.impl.script;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractBulkAction;
import eu.bcvsolutions.idm.core.api.config.domain.RoleConfiguration;
import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.domain.TransactionContextHolder;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmScriptAuthorityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmScriptDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityStateFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmScriptAuthorityFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmScriptFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEvent.CoreEventType;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.EntityStateManager;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmScriptAuthorityService;
import eu.bcvsolutions.idm.core.api.service.IdmScriptService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.eav.api.domain.BaseCodeList;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.event.RoleEvent;
import eu.bcvsolutions.idm.core.model.event.RoleEvent.RoleEventType;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import groovy.lang.Script;

/**
 * Duplicate script definition.
 *
 * @author Ondrej Husnik
 * @since 11.0.0
 */
@Component(ScriptDuplicateBulkAction.NAME)
@Description("Duplicate script.")
public class ScriptDuplicateBulkAction extends AbstractBulkAction<IdmScriptDto, IdmScriptFilter> {

	public static final String NAME = "core-duplicate-script-bulk-action";

	@Autowired private IdmScriptService scriptService;
	@Autowired private IdmScriptAuthorityService scriptAuthorityService;

	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public int getOrder() {
		return super.getOrder() + 100;
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(CoreGroupPermission.SCRIPT_CREATE, CoreGroupPermission.SCRIPT_READ);
	}
	
	@Override
	public ReadWriteDtoService<IdmScriptDto, IdmScriptFilter> getService() {
		return scriptService;
	}
	
	@Override
	protected OperationResult processDto(IdmScriptDto dto) {
		String newCode = getUniqueCode(dto.getCode(), 0);
		// find script authorities
		IdmScriptAuthorityFilter authorityFilt = new IdmScriptAuthorityFilter();
		authorityFilt.setScriptId(dto.getId());
		List<IdmScriptAuthorityDto> authorityDtos = scriptAuthorityService.find(authorityFilt, null).getContent();
		
		//script attributes duplication
		IdmScriptDto newDto = scriptService.getByCode(dto.getCode());
		newDto.setId(null);
		EntityUtils.clearAuditFields(newDto);
		newDto.setCode(newCode);
		newDto = scriptService.save(newDto, IdmBasePermission.CREATE);
				
		// script authority
		for (IdmScriptAuthorityDto authorityDto : authorityDtos) {
			authorityDto.setId(null);
			EntityUtils.clearAuditFields(authorityDto);
			authorityDto.setScript(newDto.getId());
		}
		scriptAuthorityService.saveAll(authorityDtos, IdmBasePermission.CREATE);		
		return new OperationResult.Builder(OperationState.EXECUTED).build();
	}
	
	/**
	 * Get unique code for script
	 * 
	 * @param i
	 * @return
	 */
	private String getUniqueCode(String code, int i) {
		String newCode;
		if (i > 0) {
			newCode = MessageFormat.format("{0}_{1}", code, i);
		} else {
			newCode = code;
		}
		
		if (scriptService.getByCode(newCode) == null) {
			return newCode;
		}
		return getUniqueCode(code, i + 1);
	}	
}
