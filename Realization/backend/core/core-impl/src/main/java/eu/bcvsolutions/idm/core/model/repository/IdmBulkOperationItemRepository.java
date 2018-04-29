package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmBulkOperationItem;

public interface IdmBulkOperationItemRepository extends AbstractEntityRepository<IdmBulkOperationItem> {

	
	@Modifying
    @Query(value = "insert into #{#entityName} (redirect,user_id) VALUES (:item)", nativeQuery = true)
    void bulkInsert(@Param("items") List<IdmBulkOperationItem> items);
}
