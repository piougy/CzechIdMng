package eu.bcvsolutions.idm.acc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import eu.bcvsolutions.idm.acc.entity.SysProvisioningArchive;
import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;

/**
 * Provisioning log
 * 
 * @author Radek Tomiška
 *
 */
@RepositoryRestResource(
		collectionResourceRel = "provisioningArchives",
		path = "provisioning-archives",
		itemResourceRel = "provisioningArchive",
		exported = false
)
public interface SysProvisioningArchiveRepository extends AbstractEntityRepository<SysProvisioningArchive, EmptyFilter> {

	/*
	 * (non-Javadoc)
	 * @see eu.bcvsolutions.idm.core.api.repository.BaseEntityRepository#find(eu.bcvsolutions.idm.core.api.dto.BaseFilter, Pageable)
	 */
	@Override
	@Query(value = "select e from #{#entityName} e")
	Page<SysProvisioningArchive> find(EmptyFilter filter, Pageable pageable);
}
