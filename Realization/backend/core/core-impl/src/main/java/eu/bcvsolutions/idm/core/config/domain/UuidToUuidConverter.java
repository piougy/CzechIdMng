package eu.bcvsolutions.idm.core.config.domain;

import java.util.UUID;

import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;

/**
 * Converter for UUID to UUID. This converter must be set for only one purpose... workaround fixed error in ModelMapper.
 * When is in DTO field (applicant for example) with type UUID (with conversion to IdmIdentity) and other UUID field (for example modifierId),
 * but with same value as first field, then mapper will be set converted value from first field (applicant) to second field -> Class cast exception will be throw. 
 * 
 * @author svandav
 *
 */
public class UuidToUuidConverter implements Converter<UUID, UUID> {

	@Override
	public UUID convert(MappingContext<UUID, UUID> context) {
		return (UUID)context.getSource();
	}
}