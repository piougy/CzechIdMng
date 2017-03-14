package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.ConfigurationDto;
import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.api.utils.SpinalCase;
import eu.bcvsolutions.idm.core.exception.TreeTypeException;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeNodeRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeTypeRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmConfigurationService;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeTypeService;

@Service
public class DefaultIdmTreeTypeService extends AbstractReadWriteEntityService<IdmTreeType, QuickFilter> implements IdmTreeTypeService {

	private final IdmTreeTypeRepository repository;
	private final IdmTreeNodeRepository treeNodeRepository;
	private final IdmIdentityContractRepository identityContractRepository;
	private final IdmConfigurationService configurationService;
	
	@Autowired
	public DefaultIdmTreeTypeService(
			IdmTreeTypeRepository treeTypeRepository,
			IdmTreeNodeRepository treeNodeRepository,
			IdmIdentityContractRepository identityContractRepository,
			IdmConfigurationService configurationService) {
		super(treeTypeRepository);
		//
		Assert.notNull(treeNodeRepository);
		Assert.notNull(identityContractRepository);
		Assert.notNull(configurationService);
		//
		this.repository = treeTypeRepository;
		this.treeNodeRepository = treeNodeRepository;
		this.identityContractRepository = identityContractRepository;
		this.configurationService = configurationService;
	}
	
	@Override
	@Transactional
	public IdmTreeType save(IdmTreeType entity) {
		if (entity.isDefaultTreeType()) {
			this.repository.clearDefaultTreeType(entity.getId());
		}
		return super.save(entity);
	}
	
	/**
	 * Deletes tree type, if no children a contracts assigned to this type exists. 
	 */
	@Override
	@Transactional
	public void delete(IdmTreeType treeType) {
		Assert.notNull(treeType);
		//	
		Page<IdmTreeNode> nodes = treeNodeRepository.findChildren(treeType.getId(), null, new PageRequest(0, 1));
		if (nodes.getTotalElements() > 0) {
			throw new TreeTypeException(CoreResultCode.TREE_TYPE_DELETE_FAILED_HAS_CHILDREN,  ImmutableMap.of("treeType", treeType.getName()));
		}		
		if (identityContractRepository.countByWorkingPosition_TreeType(treeType) > 0) {
			throw new TreeTypeException(CoreResultCode.TREE_TYPE_DELETE_FAILED_HAS_CONTRACTS,  ImmutableMap.of("treeType", treeType.getName()));
		}		
		//
		super.delete(treeType);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public IdmTreeType getByCode(String code) {
		return repository.findOneByCode(code);
	}

	@Override
	@Transactional(readOnly = true)
	public IdmTreeType getDefaultTreeType() {
		return repository.findOneByDefaultTreeTypeIsTrue();
	}

	@Override
	@Transactional(readOnly = true)
	public List<ConfigurationDto> getConfigurations(IdmTreeType treeType) {
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

	@Override
	@Transactional
	public int clearDefaultTreeNode(IdmTreeNode defaultTreeNode) {
		return repository.clearDefaultTreeNode(defaultTreeNode);
	}
}

