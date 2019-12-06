package eu.bcvsolutions.idm.core.model.repository;

import java.util.UUID;

import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmPassword;

/**
 * Storing crypted passwords to IdM
 * 
 * @author Ondřej Kopr
 * @author Radek Tomiška
 *
 */
public interface IdmPasswordRepository extends AbstractEntityRepository<IdmPassword> {
	
	/**
	 * Identity has one password (or any).
	 * 
	 * @param identityId
	 * @return
	 * @deprecated @since 9.6.0
	 */
	@Deprecated
	IdmPassword findOneByIdentity_Id(@Param("identityId") UUID identityId);

	/**
	 * Identity has one password (or any).
	 *
	 * @param username
	 * @return
	 * @deprecated @since 9.6.0
	 */
	@Deprecated
	IdmPassword findOneByIdentity_username(@Param("username") String username);
}
