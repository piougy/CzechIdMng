package eu.bcvsolutions.idm.acc.repository;

import eu.bcvsolutions.idm.acc.dto.AccUniformPasswordDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.entity.AccUniformPasswordSystem;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;

/**
 * Repository for connection between {@link AccUniformPasswordDto} and {@link SysSystemDto}.
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
public interface AccUniformPasswordSystemRepository extends AbstractEntityRepository<AccUniformPasswordSystem> {

}
