package eu.bcvsolutions.idm.acc.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

import eu.bcvsolutions.idm.acc.test.AbstractTestResource;

/**
 * Entity for test table resource
 * 
 * @author svandav
 *
 */
@Entity
@Table(name = TestResource.TABLE_NAME)
public class TestResource extends AbstractTestResource {
	
}
