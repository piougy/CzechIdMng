package eu.bcvsolutions.idm.acc.service.api;

import java.util.Map;

import org.springframework.core.Ordered;

import eu.bcvsolutions.idm.acc.dto.ConnectorTypeDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;

/**
 * Connector type extends standard IC connector for more metadata (image, wizard, ...).
 *
 * @author Vít Švanda
 * @since 10.7.0
 */
public interface ConnectorType extends Ordered {

	String STEP_FINISH = "finish";
	String CREATES_ROLE_WITH_SYSTEM = "createRoleWithSystem";
	String MAPPING_ID = "mappingId";
	String SCHEMA_ID = "schemaId";
	String NEW_ROLE_WITH_SYSTEM_CODE = "newRoleWithSystemCode";
	String ROLE_SYSTEM_ID = "roleSystemId";
	String SYSTEM_DTO_KEY = "system";
	String STEP_MAPPING = "mapping";
	String ENTITY_TYPE = "entityType";
	String TREE_TYPE_ID = "treeTypeId";
	String OPERATION_TYPE = "operationType";
	String MAPPING_DTO_KEY = "mapping";
	String SYNC_DTO_KEY = "sync";
	String ALERT_MORE_MAPPINGS = "alertMoreMappings";

	/**
	 * Bean name / unique identifier (spring bean name).
	 *
	 * @return
	 */
	String getId();

	/**
	 * Defines for which connector could be used.
	 *
	 * @return
	 */
	String getConnectorName();

	/**
	 * Name of component of the FE, keeps main connector image.
	 *
	 * @return
	 */
	default String getIconKey() {return "default-connector";}

	/**
	 * Returns module
	 *
	 * @return
	 */
	default String getModule() {
		return EntityUtils.getModule(this.getClass());
	}

	/**
	 * Defines if original IC connector should be hidden in the UI.
	 *
	 * @return
	 */
	default boolean hideParentConnector() {return true;}


	/**
	 * Order of connectors.
	 *
	 * @return
	 */
	@Override
	int getOrder();

	/**
	 * If false, then connector type will be not visible to a user.
	 *
	 * @return
	 */
	boolean supports();


	/**
	 * Specific data for a connector type (attributes).
	 */
	Map<String, String> getMetadata();

	/**
	 * Execute connector type -> execute some wizard step.
	 *
	 */
	default ConnectorTypeDto execute(ConnectorTypeDto connectorType) {
		return connectorType;
	}

	/**
	 * Load data for specific wizard/step (for open existing system in the wizard).
	 */
	default ConnectorTypeDto load(ConnectorTypeDto connectorType) {
		return connectorType;
	}

	/**
	 * Returns true if this connector type should be use for open given system.
	 */
	boolean supportsSystem(SysSystemDto systemDto);
}
