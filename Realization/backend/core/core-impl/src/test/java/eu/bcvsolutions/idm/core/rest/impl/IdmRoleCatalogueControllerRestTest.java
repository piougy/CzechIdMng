package eu.bcvsolutions.idm.core.rest.impl;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleCatalogueFilter;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;

/**
 * Controller tests
 * - TODO: move filters here
 * 
 * @author Radek Tomiška
 *
 */
public class IdmRoleCatalogueControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmRoleCatalogueDto> {

	@Autowired private IdmRoleCatalogueController controller;
	
	@Override
	protected AbstractReadWriteDtoController<IdmRoleCatalogueDto, ?> getController() {
		return controller;
	}

	@Override
	protected IdmRoleCatalogueDto prepareDto() {
		IdmRoleCatalogueDto dto = new IdmRoleCatalogueDto();
		dto.setName(getHelper().createName());
		dto.setCode(getHelper().createName());
		return dto;
	}
	
	@Test
	public void testFindByRootsAndChildren() {
		IdmRoleCatalogueDto catalogueItemOne = createDto();
		IdmRoleCatalogueDto catalogueItem = prepareDto();
		catalogueItem.setParent(catalogueItemOne.getId());
		IdmRoleCatalogueDto catalogueItemTwo = createDto(catalogueItem);
		// root
		IdmRoleCatalogueFilter filter = new IdmRoleCatalogueFilter();
		filter.setRoots(Boolean.TRUE);
		filter.setName(catalogueItemOne.getName());
		List<IdmRoleCatalogueDto> results = find(filter);
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(catalogueItemOne.getId())));
		// is root - false
		filter.setRoots(Boolean.FALSE);
		results = find(filter);
		Assert.assertTrue(results.isEmpty());
		//
		// children
		filter.setRoots(null);
		filter.setName(null);
		filter.setParent(catalogueItemOne.getId());
		results = find(filter);
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(catalogueItemTwo.getId())));
		// no children
		filter.setParent(catalogueItemTwo.getId());
		results = find(filter);
		Assert.assertTrue(results.isEmpty());
	}
}
