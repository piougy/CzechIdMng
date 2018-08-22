package eu.bcvsolutions.idm.core.rest.impl;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmTreeNodeFilter;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;

/**
 * Controller tests
 * - CRUD
 * - filters
 * 
 * @author Radek Tomiška
 * @author Peter Šourek
 *
 */
public class IdmTreeNodeControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmTreeNodeDto> {

	@Autowired private IdmTreeNodeController controller;
	
	@Override
	protected AbstractReadWriteDtoController<IdmTreeNodeDto, ?> getController() {
		return controller;
	}

	@Override
	protected IdmTreeNodeDto prepareDto() {
		IdmTreeNodeDto dto = new IdmTreeNodeDto();
		dto.setName(getHelper().createName());
		dto.setCode(getHelper().createName());
		dto.setTreeType(getHelper().getDefaultTreeType().getId());
		return dto;
	}
	
	@Test
	@Transactional
	public void testFindChildrenRecursivelyWithoutTreeTypeSpecified() {
		IdmTreeNodeDto rootOne;
		IdmTreeNodeDto childOne;
		try {
			getHelper().loginAdmin();
			//
			IdmTreeTypeDto treeTypeOne = getHelper().createTreeType();
			IdmTreeTypeDto treeTypeTwo = getHelper().createTreeType();
			//
			rootOne = getHelper().createTreeNode(treeTypeOne, null);
			childOne = getHelper().createTreeNode(treeTypeOne, rootOne);
			IdmTreeNodeDto rootTwo = getHelper().createTreeNode(treeTypeTwo, null);
			getHelper().createTreeNode(treeTypeTwo, rootTwo);
		} finally {
			getHelper().logout();
		}
		//
		IdmTreeNodeFilter filter = new IdmTreeNodeFilter();
		filter.setTreeNode(rootOne.getId());
		//
		List<IdmTreeNodeDto> results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(childOne, results.get(0));
	}
	
	@Test
	@Transactional
	public void testFindByDefaultTreeType() {
		String code = getHelper().createName();
		IdmTreeNodeDto nodeDefault;
		try {
			getHelper().loginAdmin();
			//
			IdmTreeTypeDto defaultTreeType = getHelper().getDefaultTreeType();
			IdmTreeTypeDto treeTypeTwo = getHelper().createTreeType();
			//
			nodeDefault = getHelper().createTreeNode(defaultTreeType, code, null);
			// other
			getHelper().createTreeNode(treeTypeTwo, code, null);
		} finally {
			getHelper().logout();
		}
		
		IdmTreeNodeFilter filter = new IdmTreeNodeFilter();
		filter.setDefaultTreeType(true);
		filter.setCode(code);
		
		List<IdmTreeNodeDto> results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(nodeDefault, results.get(0));
		
	}
	
	@Test
	@Transactional
	public void testFilters() {
		// ****** PREPARE DATA ******
		//
		UUID t1Id = null;
		UUID t2Id = null;
		UUID r1Uuid = null;
		UUID n1Uuid = null;
		UUID n5Uuid = null;
		UUID r2Uuid = null;
		UUID n12Uuid = null;
		try {
			getHelper().loginAdmin();
			//
			IdmTreeTypeDto t1 = getHelper().createTreeType("TYPE1");
			IdmTreeTypeDto t2 = getHelper().createTreeType("TYPE2");
			t1Id = t1.getId();
			t2Id = t2.getId();
			/*
			        o r1
			       / \
			   n1 o   o n3
			  / \
		      n2 o   o n4
			      \
			       o n5
			*/
			IdmTreeNodeDto r1 = getHelper().createTreeNode(t1, "ROOT1", null);
			IdmTreeNodeDto n1 = getHelper().createTreeNode(t1, "NODE1", r1);
			getHelper().createTreeNode(t1, "NODE2", n1);
			getHelper().createTreeNode(t1, "NODE3", r1);
			IdmTreeNodeDto n4 = getHelper().createTreeNode(t1, "NODE4", n1);
			IdmTreeNodeDto n5 = getHelper().createTreeNode(t1, "NODE5", n4);
			/*
			         o r2
			        /
			 n12 o-o n11
			 */
			IdmTreeNodeDto r2 = getHelper().createTreeNode(t2, "ROOT2", null);
			IdmTreeNodeDto n11 = getHelper().createTreeNode(t2, "NODE11", r2);
			IdmTreeNodeDto n12 = getHelper().createTreeNode(t2, "NODE12", n11);
			//
			r1Uuid = r1.getId();
			n1Uuid = n1.getId();
			n5Uuid = n5.getId();
			r2Uuid = r2.getId();
			n12Uuid = n12.getId();
		} finally {
			getHelper().logout();
		} 
		//
		// ******* TEST *******
		//
		final IdmTreeNodeFilter t1Flter = new IdmTreeNodeFilter();
		t1Flter.setTreeTypeId(t1Id);
		List<IdmTreeNodeDto> result = find(t1Flter);
		Assert.assertEquals(6, result.size());
		//
		final IdmTreeNodeFilter t2Flter = new IdmTreeNodeFilter();
		t2Flter.setTreeTypeId(t2Id);
		result = find(t2Flter);
		Assert.assertEquals(3, result.size());
		//
		// Subtrees
		//
		final IdmTreeNodeFilter subTreeFilter1 = new IdmTreeNodeFilter();
		subTreeFilter1.setTreeNode(n1Uuid);
		subTreeFilter1.setTreeTypeId(t1Id);
		subTreeFilter1.setRecursively(true);
		result = find(subTreeFilter1);
		Assert.assertEquals(3, result.size());
		//
		final IdmTreeNodeFilter subTreeFilter2 = new IdmTreeNodeFilter();
		subTreeFilter2.setTreeNode(n1Uuid);
		subTreeFilter2.setRecursively(false);
		result = find(subTreeFilter2);
		Assert.assertEquals(2, result.size());
		//
		final IdmTreeNodeFilter subTreeFilter3 = new IdmTreeNodeFilter();
		subTreeFilter3.setTreeNode(r2Uuid);
		subTreeFilter3.setRecursively(false);
		result = find(subTreeFilter3);
		Assert.assertEquals(1, result.size());
		//
		final IdmTreeNodeFilter subTreeFilter4 = new IdmTreeNodeFilter();
		subTreeFilter4.setTreeNode(r2Uuid);
		subTreeFilter4.setTreeTypeId(t2Id);
		subTreeFilter4.setRecursively(true);
		result = find(subTreeFilter4);
		Assert.assertEquals(2, result.size());
		//
		final IdmTreeNodeFilter subTreeFilter5 = new IdmTreeNodeFilter();
		subTreeFilter5.setTreeNode(n12Uuid);
		subTreeFilter5.setTreeTypeId(t2Id);
		subTreeFilter5.setRecursively(true);
		result = find(subTreeFilter5);
		Assert.assertEquals(0, result.size());
		//
		final IdmTreeNodeFilter subTreeFilter6 = new IdmTreeNodeFilter();
		subTreeFilter6.setTreeNode(n12Uuid);
		subTreeFilter6.setTreeTypeId(t2Id);
		subTreeFilter6.setRecursively(false);
		result = find(subTreeFilter6);
		Assert.assertEquals(0, result.size());
		//
		// Fulltext
		//
		final IdmTreeNodeFilter fullTextFilter1 = new IdmTreeNodeFilter();
		fullTextFilter1.setText("NODE5");
		result = find(fullTextFilter1);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(n5Uuid, result.get(0).getId());
		//
		final IdmTreeNodeFilter fullTextFilter2 = new IdmTreeNodeFilter();
		fullTextFilter2.setText("NODE");
		fullTextFilter2.setTreeTypeId(t1Id);
		result = find(fullTextFilter2);
		Assert.assertEquals(5, result.size());
		//
		final IdmTreeNodeFilter fullTextFilter3 = new IdmTreeNodeFilter();
		fullTextFilter3.setText("odE");
		fullTextFilter3.setTreeTypeId(t1Id);
		result = find(fullTextFilter3);
		Assert.assertEquals(5, result.size());
		//
		// Property - value pairs
		//
		final IdmTreeNodeFilter dynPropFilter1 = new IdmTreeNodeFilter();
		dynPropFilter1.setProperty("name");
		dynPropFilter1.setValue("ROOT1");
		result = find(dynPropFilter1);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(r1Uuid, result.get(0).getId());
		//
		final IdmTreeNodeFilter dynPropFilter2 = new IdmTreeNodeFilter();
		dynPropFilter2.setProperty("code");
		dynPropFilter2.setValue("ROOT2");
		result = find(dynPropFilter2);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(r2Uuid, result.get(0).getId());
	}
}
