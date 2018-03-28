package eu.bcvsolutions.idm.core.api.dto;

import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleRequestType;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.domain.RecursionType;
import eu.bcvsolutions.idm.core.api.domain.RequestOperationType;

/**
 * DTO for request keeps main information about automatic roles and their
 * assignment
 * 
 * @author svandav
 * @since 8.0.0
 *
 */
@Relation(collectionRelation = "automaticRoleRequests")
public class IdmAutomaticRoleRequestDto extends AbstractRequestDto {

	private static final long serialVersionUID = 1L;

	@NotNull
	private AutomaticRoleRequestType requestType = AutomaticRoleRequestType.ATTRIBUTE;
	@Size(max = DefaultFieldLengths.NAME)
	private String name;
	@Embedded(dtoClass = IdmRoleDto.class)
	private UUID role;
	private UUID automaticRole;
	@Embedded(dtoClass = IdmTreeNodeDto.class)
	private UUID treeNode;
	private RecursionType recursionType = RecursionType.NO;
	@NotNull
	private RequestOperationType operation = RequestOperationType.UPDATE;

	public AutomaticRoleRequestType getRequestType() {
		return requestType;
	}

	public void setRequestType(AutomaticRoleRequestType requestType) {
		this.requestType = requestType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public UUID getRole() {
		return role;
	}

	public void setRole(UUID role) {
		this.role = role;
	}

	public UUID getAutomaticRole() {
		return automaticRole;
	}

	public void setAutomaticRole(UUID automaticRole) {
		this.automaticRole = automaticRole;
	}

	public UUID getTreeNode() {
		return treeNode;
	}

	public void setTreeNode(UUID treeNode) {
		this.treeNode = treeNode;
	}

	public RecursionType getRecursionType() {
		return recursionType;
	}

	public void setRecursionType(RecursionType recursionType) {
		this.recursionType = recursionType;
	}

	public RequestOperationType getOperation() {
		return operation;
	}

	public void setOperation(RequestOperationType operation) {
		this.operation = operation;
	}

}