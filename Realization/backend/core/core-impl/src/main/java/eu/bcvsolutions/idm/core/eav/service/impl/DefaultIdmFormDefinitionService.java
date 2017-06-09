package eu.bcvsolutions.idm.core.eav.service.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.eav.dto.filter.FormAttributeFilter;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.core.eav.repository.IdmFormDefinitionRepository;
import eu.bcvsolutions.idm.core.eav.service.api.IdmFormAttributeService;
import eu.bcvsolutions.idm.core.eav.service.api.IdmFormDefinitionService;

/**
 * Default implementation of form definition service
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultIdmFormDefinitionService extends AbstractReadWriteEntityService<IdmFormDefinition, QuickFilter> implements IdmFormDefinitionService {

	private final IdmFormDefinitionRepository formDefinitionRepository;
	private final IdmFormAttributeService formAttributeService;

	@Autowired
	public DefaultIdmFormDefinitionService(IdmFormDefinitionRepository formDefinitionRepository,
			IdmFormAttributeService formAttributeService) {
		super(formDefinitionRepository);
		//
		Assert.notNull(formAttributeService);
		//
		this.formDefinitionRepository = formDefinitionRepository;
		this.formAttributeService = formAttributeService;
	}
	
	/**
	 * Fill default definition code and name, if no code / name is given
	 */
	@Override
	@Transactional
	public IdmFormDefinition save(IdmFormDefinition entity) {
		if (StringUtils.isEmpty(entity.getCode())) {
			entity.setMain(true);
			entity.setCode(DEFAULT_DEFINITION_CODE);
		}
		if (StringUtils.isEmpty(entity.getName())) {
			entity.setName(entity.getCode());
		}
		if (entity.isMain()) {
			this.formDefinitionRepository.clearMain(entity.getType(), entity.getId(), new DateTime());
		}
		return super.save(entity);
	}
	
	@Override
	@Transactional(readOnly = true)
	public IdmFormDefinition findOneByTypeAndCode(String type, String code) {
		return formDefinitionRepository.findOneByTypeAndCode(type, code != null ? code : DEFAULT_DEFINITION_CODE);
	}
	
	@Override
	@Transactional(readOnly = true)
	public IdmFormDefinition findOneByMain(String type) {
		return formDefinitionRepository.findOneByTypeAndMainIsTrue(type);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<IdmFormDefinition> findAllByType(String type) {
		return formDefinitionRepository.findAllByType(type);
	}

	@Override
	@Transactional
	public void delete(IdmFormDefinition entity) {
		// delete all attributes in definition
		FormAttributeFilter filter = new FormAttributeFilter();
		filter.setFormDefinition(entity);
		formAttributeService.find(filter, null).forEach(formAttribute -> {
			formAttributeService.delete(formAttribute);
		});
		entity.setFormAttributes(null); // prevent cascade - duplicit removal
		//
		super.delete(entity);
	}
}
