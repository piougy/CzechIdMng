package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.config.domain.TreeConfiguration;
import eu.bcvsolutions.idm.core.api.dto.IdmConfigurationDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmTreeTypeFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.utils.SpinalCase;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeTypeRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmConfigurationService;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeTypeService;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Operations with IdmTreeType
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 */
public class DefaultIdmTreeTypeService 
		extends AbstractEventableDtoService<IdmTreeTypeDto, IdmTreeType, IdmTreeTypeFilter> 
		implements IdmTreeTypeService {
	
	private final IdmTreeTypeRepository repository;
	private final IdmConfigurationService configurationService;
	private final TreeConfiguration treeConfiguration;
	
	@Autowired
	public DefaultIdmTreeTypeService(
			IdmTreeTypeRepository treeTypeRepository,
			IdmConfigurationService configurationService,
			TreeConfiguration treeConfiguration,
			EntityEventManager entityEventManager) {
		super(treeTypeRepository, entityEventManager);
		//
		Assert.notNull(configurationService);
		Assert.notNull(treeConfiguration);
		//
		this.repository = treeTypeRepository;
		this.configurationService = configurationService;
		this.treeConfiguration = treeConfiguration;
	}
	
	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.TREETYPE, getEntityClass());
	}

	@Override
	@Transactional(readOnly = true)
	public IdmTreeTypeDto getByCode(String code) {
		return toDto(repository.findOneByCode(code));
	}

	@Override
	@Transactional(readOnly = true)
	public IdmTreeTypeDto getDefaultTreeType() {
		return treeConfiguration.getDefaultType();
	}

	@Override
	@Transactional(readOnly = true)
	public List<IdmConfigurationDto> getConfigurations(UUID treeTypeId) {
		Assert.notNull(treeTypeId);
		IdmTreeTypeDto treeType = get(treeTypeId);
		Assert.notNull(treeType);
		//
		return new ArrayList<>(configurationService.getConfigurations(getConfigurationPrefix(treeType.getCode())).values());
	}
	
	private static String getConfigurationPrefix(String treeTypeCode) {
		Assert.notNull(treeTypeCode);
		//
		return String.format("%s%s.", CONFIGURATION_PREFIX, SpinalCase.format(treeTypeCode));
	}
	
	@Override
	public String getConfigurationPropertyName(String treeTypeCode, String propertyName) {
		Assert.notNull(propertyName);
		//
		return String.format("%s%s", getConfigurationPrefix(treeTypeCode), propertyName);
	}
}

