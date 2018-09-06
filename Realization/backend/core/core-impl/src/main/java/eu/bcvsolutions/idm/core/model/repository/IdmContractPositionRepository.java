package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import eu.bcvsolutions.idm.core.api.domain.RecursionType;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmContractPosition;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;

/**
 * Identity contract's other position
 * 
 * @author Radek Tomiška
 * @since 9.1.0
 */
public interface IdmContractPositionRepository extends AbstractEntityRepository<IdmContractPosition> {

	@Query(value = "select e from #{#entityName} e join e.workPosition n"
			+ " where"
			+ " (n.treeType = ?#{[0].treeType})" // more tree types
			+ " and"
			+ " ("
				+ " (n = ?#{[0]})" // takes all recursion
				+ " or"
				+ " (?#{[1].name()} = 'DOWN' and n.forestIndex.lft between ?#{[0].lft} and ?#{[0].rgt})"
				+ " or"
				+ " (?#{[1].name()} = 'UP' and ?#{[0].lft} between n.forestIndex.lft and n.forestIndex.rgt)"
			+ " )")
	List<IdmContractPosition> findAllByWorkPosition(IdmTreeNode workPosition, RecursionType recursionType);
}
