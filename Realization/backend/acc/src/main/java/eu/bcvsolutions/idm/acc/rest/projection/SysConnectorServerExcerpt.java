package eu.bcvsolutions.idm.acc.rest.projection;

import org.springframework.data.rest.core.config.Projection;

import eu.bcvsolutions.idm.acc.entity.SysConnectorServer;
import eu.bcvsolutions.idm.core.api.rest.projection.AbstractDtoProjection;


@Projection(name = "excerpt", types = SysConnectorServer.class)
public interface SysConnectorServerExcerpt extends AbstractDtoProjection {
	
	String getName();
	
	boolean isUseSsl();

	int getTimeout();

	String getHost();

	int getPort();

	String getPassword();

	String getNameConnectorBundle();
}
