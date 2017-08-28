package eu.bcvsolutions.idm.vs.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * Relation between virtual system requests
 * 
 * @author Svanda
 * @author Tomiska
 *
 */
@Entity
@Table(name = "vs_request_batch")
public class VsRequestBatch extends AbstractEntity {

	private static final long serialVersionUID = 1L;

}