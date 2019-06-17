package eu.bcvsolutions.idm.core.api.dto;

import org.springframework.hateoas.core.Relation;

/**
 * DTO for show changes on assigned identity roles
 *
 * @author Vít Švanda
 */
@Relation(collectionRelation = "requestIdentityRoles")
public class IdmRequestIdentityRoleDto extends IdmConceptRoleRequestDto {

	private static final long serialVersionUID = 1L;


 }