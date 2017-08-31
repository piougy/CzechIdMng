package eu.bcvsolutions.idm.core.config.domain;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.config.domain.AbstractConfiguration;
import eu.bcvsolutions.idm.core.api.config.domain.TreeConfiguration;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.service.LookupService;

/**
 * Configuration for features with identities.
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultTreeConfiguration extends AbstractConfiguration implements TreeConfiguration {	
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultTreeConfiguration.class);
	private final LookupService lookupService;
	
	@Autowired
	public DefaultTreeConfiguration(LookupService lookupService) {
		Assert.notNull(lookupService);
		//
		this.lookupService = lookupService;
	}
	
	@Override
	public IdmTreeTypeDto getDefaultType() {
		String codeable = getConfigurationService().getValue(PROPERTY_DEFAULT_TYPE);
		if (StringUtils.isBlank(codeable)) {
			LOG.debug("Default tree type is not configuration, returning null. Change configuration [{}]", PROPERTY_DEFAULT_TYPE);
			return null;
		}
		// lookup - uuid or code could be given
		IdmTreeTypeDto treeType = (IdmTreeTypeDto) lookupService.lookupDto(IdmTreeTypeDto.class, codeable);
		if (treeType == null) {
			LOG.warn("Default tree type with codeable [{}] not found, returning null. Change configuration [{}]", codeable, PROPERTY_DEFAULT_TYPE);
			return null;
		}
		return treeType;
	}
	
	@Override
	public void setDefaultType(UUID treeTypeId) {
		getConfigurationService().setValue(PROPERTY_DEFAULT_TYPE, treeTypeId == null ? null : treeTypeId.toString());
	}
	
	@Override
	public IdmTreeNodeDto getDefaultNode() {
		String codeable = getConfigurationService().getValue(PROPERTY_DEFAULT_NODE);
		if (StringUtils.isBlank(codeable)) {
			LOG.debug("Default tree type is not configuration, returning null. Change configuration [{}]", PROPERTY_DEFAULT_NODE);
			return null;
		}
		// lookup - uuid or code could be given
		IdmTreeNodeDto treeNode = (IdmTreeNodeDto) lookupService.lookupDto(IdmTreeNodeDto.class, codeable);
		if (treeNode == null) {
			LOG.warn("Default tree node with codeable [{}] not found, returning null. Change configuration [{}]", codeable, PROPERTY_DEFAULT_NODE);
			return null;
		}
		return treeNode;
	}
	
	@Override
	public void setDefaultNode(UUID treeNodeId) {
		getConfigurationService().setValue(PROPERTY_DEFAULT_NODE, treeNodeId == null ? null : treeNodeId.toString());
	}
}
