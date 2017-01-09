package eu.bcvsolutions.idm.core.model.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.api.service.GroovyScriptService;
import eu.bcvsolutions.idm.core.model.dto.filter.RuleFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRule;
import eu.bcvsolutions.idm.core.model.service.api.IdmRuleService;

@Service
public class DefaultRuleService extends AbstractReadWriteEntityService<IdmRule, RuleFilter> implements IdmRuleService {
	
	private final GroovyScriptService groovyScriptService;
	
	@Autowired
	public DefaultRuleService(AbstractEntityRepository<IdmRule, RuleFilter> repository, GroovyScriptService groovyScriptService) {
		super(repository);
		
		this.groovyScriptService = groovyScriptService;
	}
	
	@Override
	public void delete(IdmRule entity) {
		super.delete(entity);
	}
	
	@Override
	public IdmRule save(IdmRule entity) {
		if (entity.getScript() != null) {
			groovyScriptService.validateScript(entity.getScript());
		}
		
		return super.save(entity);
	}
}
