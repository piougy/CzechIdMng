package eu.bcvsolutions.idm.core.model.service.api;

import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.core.model.dto.filter.RuleFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRule;

@Service
public interface IdmRuleService extends ReadWriteEntityService<IdmRule, RuleFilter> {

}
