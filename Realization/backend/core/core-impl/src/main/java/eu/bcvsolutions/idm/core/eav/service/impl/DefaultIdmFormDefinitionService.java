package eu.bcvsolutions.idm.core.eav.service.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.eav.dto.IdmFormDefinitionDto;
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
public class DefaultIdmFormDefinitionService 
		extends AbstractReadWriteDtoService<IdmFormDefinitionDto, IdmFormDefinition, QuickFilter> 
		implements IdmFormDefinitionService {

	private final IdmFormDefinitionRepository formDefinitionRepository;
	private final IdmFormAttributeService formAttributeService;

	@Autowired
	public DefaultIdmFormDefinitionService(
			IdmFormDefinitionRepository formDefinitionRepository,
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
	public IdmFormDefinitionDto saveInternal(IdmFormDefinitionDto dto) {
		if (StringUtils.isEmpty(dto.getCode())) {
			dto.setMain(true);
			dto.setCode(DEFAULT_DEFINITION_CODE);
		}
		if (StringUtils.isEmpty(dto.getName())) {
			dto.setName(dto.getCode());
		}
		if (dto.isMain()) {
			// TODO: find / update - skips audit
			this.formDefinitionRepository.clearMain(dto.getType(), dto.getId(), new DateTime());
		}
		return super.saveInternal(dto);
	}
	
	@Override
	@Transactional(readOnly = true)
	public IdmFormDefinitionDto findOneByTypeAndCode(String type, String code) {
		return toDto(formDefinitionRepository.findOneByTypeAndCode(type, code != null ? code : DEFAULT_DEFINITION_CODE));
	}
	
	@Override
	@Transactional(readOnly = true)
	public IdmFormDefinitionDto findOneByMain(String type) {
		return toDto(formDefinitionRepository.findOneByTypeAndMainIsTrue(type));
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<IdmFormDefinitionDto> findAllByType(String type) {
		return toDtos(formDefinitionRepository.findAllByType(type), true);
	}

	@Override
	@Transactional
	public void deleteInternal(IdmFormDefinitionDto dto) {
		// delete all attributes in definition
		FormAttributeFilter filter = new FormAttributeFilter();
		filter.setFormDefinitionId(dto.getId());
		formAttributeService.find(filter, null).forEach(formAttribute -> {
			formAttributeService.delete(formAttribute);
		});
		//
		super.deleteInternal(dto);
	}
}
