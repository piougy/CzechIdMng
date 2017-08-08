package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmConfigurationDto;
import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.utils.SpinalCase;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.event.TreeTypeEvent;
import eu.bcvsolutions.idm.core.model.event.TreeTypeEvent.TreeTypeEventType;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeTypeRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmConfigurationService;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeTypeService;

/**
 * Operations with IdmTreeType
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 */
@Service("treeTypeService")
public class DefaultIdmTreeTypeService extends AbstractReadWriteEntityService<IdmTreeType, QuickFilter> implements IdmTreeTypeService {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultIdmTreeTypeService.class);
	
	private final IdmTreeTypeRepository repository;
	private final IdmConfigurationService configurationService;
	private final EntityEventManager entityEventManager;
	
	@Autowired
	public DefaultIdmTreeTypeService(
			IdmTreeTypeRepository treeTypeRepository,
			IdmConfigurationService configurationService,
			EntityEventManager entityEventManager) {
		super(treeTypeRepository);
		//
		Assert.notNull(configurationService);
		Assert.notNull(entityEventManager);
		//
		this.repository = treeTypeRepository;
		this.configurationService = configurationService;
		this.entityEventManager = entityEventManager;
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
		LOG.debug("Deleting tree type [{}]", treeType.getCode());
		entityEventManager.process(new TreeTypeEvent(TreeTypeEventType.DELETE, treeType));
	}

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
	public List<IdmConfigurationDto> getConfigurations(IdmTreeType treeType) {
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

