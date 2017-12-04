package eu.bcvsolutions.idm.core.model.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

/**
 * Automatic role that is assignment by value in attribute.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @since 7.6.0
 *
 */

@Audited
@Entity
@Table(name = "idm_auto_role_attribute")
public class IdmAutomaticRoleAttribute extends IdmAutomaticRole {

	private static final long serialVersionUID = 1L;

}