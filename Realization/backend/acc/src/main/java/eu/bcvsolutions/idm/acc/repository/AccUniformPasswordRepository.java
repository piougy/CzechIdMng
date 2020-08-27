package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.acc.entity.AccUniformPassword;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;

/**
 * Uniform password definition repository
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
public interface AccUniformPasswordRepository extends AbstractEntityRepository<AccUniformPassword> {

	AccUniformPassword findOneByCode(@Param("code") String code);
}
