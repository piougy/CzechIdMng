package eu.bcvsolutions.idm.vs.repository;

import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.vs.entity.VsAccount;
import eu.bcvsolutions.idm.vs.repository.filter.VsAccountFilter;

/**
 * Repository for virtual system accounts
 * 
 * @author Svanda
 *
 */
public interface VsAccountRepository extends AbstractEntityRepository<VsAccount, VsAccountFilter> {

	VsAccount findOneByUid(@Param("uid") String uid);
	
//	@Override
//	@Query(value = "select e from VsAccount e" +
//	        " where" +
//	        "(?#{[0].text} is null or lower(e.uid) like ?#{[0].text == null ? '%' : '%'.concat([0].text.toLowerCase()).concat('%')})")
//	Page<VsAccount> find(VsAccountFilter filter, Pageable pageable);
	

}
