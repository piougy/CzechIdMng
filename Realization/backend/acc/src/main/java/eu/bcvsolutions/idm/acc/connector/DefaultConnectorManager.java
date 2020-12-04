package eu.bcvsolutions.idm.acc.connector;

import com.google.common.collect.Lists;
import eu.bcvsolutions.idm.acc.dto.ConnectorTypeDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.service.api.ConnectorManager;
import eu.bcvsolutions.idm.acc.service.api.ConnectorType;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.security.api.service.EnabledEvaluator;
import eu.bcvsolutions.idm.ic.api.IcConnectorInfo;
import eu.bcvsolutions.idm.ic.api.IcConnectorKey;
import eu.bcvsolutions.idm.ic.service.api.IcConfigurationFacade;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.modelmapper.internal.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Connector manager controls connector types, which extends standard IC connectors for more metadata (image, wizard, ...).
 *
 * @author Vít Švanda
 * @since 10.7.0
 */
@Service("connectorManager")
public class DefaultConnectorManager implements ConnectorManager {

	@Autowired
	private ApplicationContext context;
	@Lazy
	@Autowired
	private EnabledEvaluator enabledEvaluator;
	@Autowired
	private IcConfigurationFacade icConfiguration;
	@Autowired
	@Qualifier("default-connector-type")
	private DefaultConnectorType defaultConnectorType;

	@Override
	public List<ConnectorType> getSupportedTypes() {
		return context
				.getBeansOfType(ConnectorType.class)
				.values()
				.stream()
				.filter(enabledEvaluator::isEnabled)
				.filter(ConnectorType::supports)
				.sorted(Comparator.comparing(ConnectorType::getOrder))
				.collect(Collectors.toList());
	}

	@Override
	public ConnectorType getConnectorType(String id) {
		return this.getSupportedTypes().stream()
				.filter(type -> type.getId().equals(id))
				.findFirst()
				.orElse(defaultConnectorType);
	}

	@Override
	public ConnectorTypeDto convertTypeToDto(ConnectorType connectorType) {
		ConnectorTypeDto connectorTypeDto = new ConnectorTypeDto();
		connectorTypeDto.setId(connectorType.getId());
		connectorTypeDto.setName(connectorType.getId());
		connectorTypeDto.setModule(connectorType.getModule());
		connectorTypeDto.setConnectorName(connectorType.getConnectorName());
		connectorTypeDto.setIconKey(connectorType.getIconKey());
		connectorTypeDto.setMetadata(connectorType.getMetadata());
		connectorTypeDto.setHideParentConnector(connectorType.hideParentConnector());
		connectorTypeDto.setOrder(connectorType.getOrder());

		return connectorTypeDto;
	}

	@Override
	public ConnectorTypeDto convertIcConnectorInfoToDto(IcConnectorInfo info) {
		ConnectorTypeDto connectorTypeDto = new ConnectorTypeDto();
		connectorTypeDto.setId(info.getConnectorKey().getConnectorName());
		connectorTypeDto.setName(info.getConnectorDisplayName());
		connectorTypeDto.setModule(EntityUtils.getModule(this.getClass()));
		connectorTypeDto.setConnectorName(info.getConnectorKey().getConnectorName());
		connectorTypeDto.setIconKey("default-connector");
		connectorTypeDto.setVersion(info.getConnectorKey().getBundleVersion());
		connectorTypeDto.setHideParentConnector(true);
		connectorTypeDto.setOrder(1000);

		return connectorTypeDto;
	}

	@Override
	@Transactional
	public ConnectorTypeDto execute(ConnectorTypeDto connectorType) {
		Assert.notNull(connectorType, "Connector type cannot be null!");
		Assert.notNull(connectorType.getId(), "Connector type ID cannot be null!");

		ConnectorType connectorTypeDef = this.getConnectorType(connectorType.getId());
		Assert.notNull(connectorTypeDef, "Connector type definition was not found!");

		return connectorTypeDef.execute(connectorType);
	}

	@Override
	@Transactional
	public ConnectorTypeDto load(ConnectorTypeDto connectorType) {
		Assert.notNull(connectorType, "Connector type cannot be null!");
		Assert.notNull(connectorType.getId(), "Connector type ID cannot be null!");

		ConnectorType connectorTypeDef = this.getConnectorType(connectorType.getId());
		Assert.notNull(connectorTypeDef, "Connector type definition was not found!");

		return connectorTypeDef.load(connectorType);
	}

	@Override
	public IcConnectorKey findConnectorKey(String connectorName) {
		Assert.notNull(connectorName, "Connector name cannot be null!");
		// TODO: Remote connectors !!!
		Map<String, Set<IcConnectorInfo>> availableLocalConnectors = icConfiguration.getAvailableLocalConnectors();
		if (availableLocalConnectors != null) {
			List<IcConnectorInfo> connectorInfos = Lists.newArrayList();
			for (Set<IcConnectorInfo> icConnectorInfos : availableLocalConnectors
					.values()) {
				connectorInfos.addAll(icConnectorInfos);
			}
			IcConnectorInfo connectorInfo = connectorInfos.stream()
					.filter(info -> connectorName.equals(info.getConnectorKey().getConnectorName()))
					.findFirst()
					.orElse(null);
			if (connectorInfo != null) {
				return connectorInfo.getConnectorKey();
			}
		}
		return null;
	}

	@Override
	public ConnectorType findConnectorTypeBySystem(SysSystemDto systemDto) {
		if (systemDto.getConnectorKey() == null){
			return  defaultConnectorType;
		}
		return this.getSupportedTypes().stream()
				.filter(type -> type.supportsSystem(systemDto))
				.findFirst()
				.orElse(defaultConnectorType);
	}
}
