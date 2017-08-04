package eu.bcvsolutions.idm.example.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;
import eu.bcvsolutions.idm.example.domain.ExampleGroupPermission;
import eu.bcvsolutions.idm.example.dto.ExampleProductDto;
import eu.bcvsolutions.idm.example.dto.filter.ExampleProductFilter;
import eu.bcvsolutions.idm.example.entity.ExampleProduct;
import eu.bcvsolutions.idm.example.repository.ExampleProductRepository;
import eu.bcvsolutions.idm.example.service.api.ExampleProductService;

/**
 * Default product service implementation
 * 
 * @author Radek Tomiška
 *
 */
@Service("exampleProductService")
public class DefaultExampleProductService 
		extends AbstractReadWriteDtoService<ExampleProductDto, ExampleProduct, ExampleProductFilter>
		implements ExampleProductService {
	
	private final ExampleProductRepository repository;
	
	@Autowired
	public DefaultExampleProductService(ExampleProductRepository repository) {
		super(repository);
		//
		this.repository = repository;
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(ExampleGroupPermission.EXAMPLEPRODUCT, getEntityClass());
	}
	
	@Override
	@Transactional(readOnly = true)
	public ExampleProductDto getByCode(String code) {
		return toDto(repository.findOneByCode(code));
	}

}
