package eu.bcvsolutions.idm.example.repository;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.example.dto.filter.ExampleProductFilter;
import eu.bcvsolutions.idm.example.entity.ExampleProduct;

/**
 * Example product repository
 * 
 * @author Radek Tomiška
 *
 */
public interface ExampleProductRepository extends AbstractEntityRepository<ExampleProduct, ExampleProductFilter> {

	/**
	 * Returns product by unique code.
	 * 
	 * @param code
	 * @return
	 */
	ExampleProduct findOneByCode(String code);
	
}
