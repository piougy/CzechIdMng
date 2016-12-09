package eu.bcvsolutions.idm.core.model.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.QuickFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.exception.TreeTypeException;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeNodeRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeTypeRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeTypeService;

@Service
public class DefaultIdmTreeTypeService extends AbstractReadWriteEntityService<IdmTreeType, QuickFilter> implements IdmTreeTypeService {

	private final IdmTreeTypeRepository treeTypeRepository;
	private final IdmTreeNodeRepository treeNodeRepository;
	private final IdmIdentityContractRepository identityContractRepository;
	
	@Autowired
	public DefaultIdmTreeTypeService(
			IdmTreeTypeRepository treeTypeRepository,
			IdmTreeNodeRepository treeNodeRepository,
			IdmIdentityContractRepository identityContractRepository) {
		super(treeTypeRepository);
		//
		Assert.notNull(treeNodeRepository);
		Assert.notNull(identityContractRepository);
		//
		this.treeTypeRepository = treeTypeRepository;
		this.treeNodeRepository = treeNodeRepository;
		this.identityContractRepository = identityContractRepository;
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
		return treeTypeRepository.findOneByCode(code);
	}


}
