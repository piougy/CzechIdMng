package eu.bcvsolutions.idm.core.model.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.api.service.GroovyScriptService;
import eu.bcvsolutions.idm.core.model.dto.filter.ScriptFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmScript;
import eu.bcvsolutions.idm.core.model.service.api.IdmScriptService;

@Service
public class DefaultIdmScriptService extends AbstractReadWriteEntityService<IdmScript, ScriptFilter> implements IdmScriptService {
	
	private final GroovyScriptService groovyScriptService;
	
	@Autowired
	public DefaultIdmScriptService(AbstractEntityRepository<IdmScript, ScriptFilter> repository, GroovyScriptService groovyScriptService) {
		super(repository);
		
		this.groovyScriptService = groovyScriptService;
	}
	
	@Override
	public void delete(IdmScript entity) {
		super.delete(entity);
	}
	
	@Override
	public IdmScript save(IdmScript entity) {
		if (entity.getScript() != null) {
			groovyScriptService.validateScript(entity.getScript());
		}
		
		return super.save(entity);
	}
}
