package eu.bcvsolutions.idm.core.eav.service.impl;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormAttributeFilter;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormDefinitionFilter;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormAttributeService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormDefinitionService;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute_;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition_;
import eu.bcvsolutions.idm.core.eav.repository.IdmFormDefinitionRepository;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Default implementation of form definition service
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultIdmFormDefinitionService 
		extends AbstractReadWriteDtoService<IdmFormDefinitionDto, IdmFormDefinition, IdmFormDefinitionFilter> 
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
	
	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.FORMDEFINITION, getEntityClass());
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
	protected IdmFormDefinitionDto toDto(IdmFormDefinition entity, IdmFormDefinitionDto dto) {
		dto = super.toDto(entity, dto);
		if (dto != null && !dto.isTrimmed()) {
			// set mapped attributes
			IdmFormAttributeFilter filter = new IdmFormAttributeFilter();
			filter.setDefinitionId(dto.getId());
			dto.setFormAttributes(
					formAttributeService
					.find(filter, new PageRequest(0, Integer.MAX_VALUE, new Sort(IdmFormAttribute_.seq.getName(), IdmFormAttribute_.name.getName())))
					.getContent());
		}
		return dto;
	}
	
	@Override
	@Transactional
	public void deleteInternal(IdmFormDefinitionDto dto) {
		// delete all attributes in definition
		IdmFormAttributeFilter filter = new IdmFormAttributeFilter();
		filter.setDefinitionId(dto.getId());
		formAttributeService.find(filter, null).forEach(formAttribute -> {
			formAttributeService.delete(formAttribute);
		});
		//
		super.deleteInternal(dto);
	}
	
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmFormDefinition> root, CriteriaQuery<?> query,
			CriteriaBuilder builder, IdmFormDefinitionFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		// quick - "fulltext"
		if (StringUtils.isNotEmpty(filter.getText())) {
			predicates.add(builder.or(
					builder.like(builder.lower(root.get(IdmFormDefinition_.type)), "%" + filter.getText().toLowerCase() + "%"),
					builder.like(builder.lower(root.get(IdmFormDefinition_.code)), "%" + filter.getText().toLowerCase() + "%"),
					builder.like(builder.lower(root.get(IdmFormDefinition_.name)), "%" + filter.getText().toLowerCase() + "%"),
					builder.like(builder.lower(root.get(IdmFormDefinition_.description)), "%" + filter.getText().toLowerCase() + "%")			
					));
		}
		//
		if (StringUtils.isNotEmpty(filter.getType())) {
			predicates.add(builder.equal(root.get(IdmFormDefinition_.type), filter.getType()));
		}
		//
		if (StringUtils.isNotEmpty(filter.getCode())) {
			predicates.add(builder.equal(root.get(IdmFormDefinition_.code), filter.getCode()));
		}
		//
		if (StringUtils.isNotEmpty(filter.getName())) {
			predicates.add(builder.equal(root.get(IdmFormDefinition_.name), filter.getName()));
		}
		//
		if (filter.getMain() != null) {
			predicates.add(builder.equal(root.get(IdmFormDefinition_.main), filter.getMain()));
		}
		return predicates;
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
}
