package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;

/**
 * Target system configuration
 * 
 * @author Radek Tomiška
 *
 */
public interface SysSystemRepository extends AbstractEntityRepository<SysSystem> {

	SysSystem findOneByName(@Param("name") String name);
}
