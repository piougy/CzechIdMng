package eu.bcvsolutions.idm.core.model.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.GroovyScriptService;
import eu.bcvsolutions.idm.core.model.dto.IdmScriptDto;
import eu.bcvsolutions.idm.core.model.dto.filter.ScriptFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmScript;
import eu.bcvsolutions.idm.core.model.repository.IdmScriptRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmScriptAuthorityService;
import eu.bcvsolutions.idm.core.model.service.api.IdmScriptService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Default service for script
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Service("scriptService")
public class DefaultIdmScriptService extends AbstractReadWriteDtoService<IdmScriptDto, IdmScript, ScriptFilter> implements IdmScriptService {
	
	private final GroovyScriptService groovyScriptService;
	private final IdmScriptAuthorityService scriptAuthorityService;
	
	@Autowired
	public DefaultIdmScriptService(IdmScriptRepository repository,
			GroovyScriptService groovyScriptService,
			IdmScriptAuthorityService scriptAuthorityService) {
		super(repository);
		//
		Assert.notNull(scriptAuthorityService);
		Assert.notNull(groovyScriptService);
		//
		this.scriptAuthorityService = scriptAuthorityService;
		this.groovyScriptService = groovyScriptService;
	}
	
	@Override
	public void deleteInternal(IdmScriptDto dto) {
		// remove all IdmScriptAuthority for this script
		scriptAuthorityService.deleteAllByScript(dto.getId());
		//
		super.deleteInternal(dto);
	}
	
	@Override
	public IdmScriptDto save(IdmScriptDto dto, BasePermission... permission) {
		if (dto.getScript() != null) {
			groovyScriptService.validateScript(dto.getScript());
		}
		return super.save(dto, permission);
	}
}
