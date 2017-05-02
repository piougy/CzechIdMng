package eu.bcvsolutions.idm.core.model.service.impl;

import java.io.Serializable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.GroovyScriptService;
import eu.bcvsolutions.idm.core.model.domain.IdmScriptCategory;
import eu.bcvsolutions.idm.core.model.dto.IdmScriptDto;
import eu.bcvsolutions.idm.core.model.dto.filter.ScriptFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmScript;
import eu.bcvsolutions.idm.core.model.repository.IdmScriptRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmScriptAuthorityService;
import eu.bcvsolutions.idm.core.model.service.api.IdmScriptService;
import eu.bcvsolutions.idm.core.script.evaluator.AbstractScriptEvaluator;
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
	private final IdmScriptRepository repository;
	private final PluginRegistry<AbstractScriptEvaluator, IdmScriptCategory> pluginExecutors; 
	
	@Autowired
	public DefaultIdmScriptService(IdmScriptRepository repository,
			GroovyScriptService groovyScriptService,
			IdmScriptAuthorityService scriptAuthorityService,
			List<AbstractScriptEvaluator> evaluators) {
		super(repository);
		//
		Assert.notNull(scriptAuthorityService);
		Assert.notNull(groovyScriptService);
		Assert.notNull(repository);
		Assert.notNull(evaluators);
		//
		this.scriptAuthorityService = scriptAuthorityService;
		this.groovyScriptService = groovyScriptService;
		this.repository = repository;
		//
		pluginExecutors = OrderAwarePluginRegistry.create(evaluators);
	}
	
	@Override
	@Transactional(readOnly = true)
	public IdmScriptDto get(Serializable id, BasePermission... permission) {
		IdmScriptDto dto = super.get(id, permission);
		//
		if (dto != null && !dto.isTrimmed()) {
			dto.setTemplate(pluginExecutors.getPluginFor(dto.getCategory()).generateTemplate(dto));
		}
		//
		return dto;
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

	@Override
	public IdmScriptDto getScriptByName(String name) {
		return this.toDto(repository.findOneByName(name));
	}
}
