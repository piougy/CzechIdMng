package eu.bcvsolutions.idm.acc.dto;

import java.util.UUID;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;

/*
 * Tree node account DTO
 * 
 */
@Relation(collectionRelation = "treeAccounts")
public class AccTreeAccountDto extends AbstractDto implements EntityAccountDto {

	private static final long serialVersionUID = 1L;
	
	@Embedded(dtoClass=AccAccountDto.class)
	private UUID account;
	@Embedded(dtoClass=IdmTreeNodeDto.class)
	private UUID treeNode;
	@Embedded(dtoClass=SysRoleSystemDto.class)
	private UUID roleSystem;
	private boolean ownership = true;
	
	public UUID getAccount() {
		return account;
	}
	public void setAccount(UUID account) {
		this.account = account;
	}
	public UUID getRoleSystem() {
		return roleSystem;
	}
	public void setRoleSystem(UUID roleSystem) {
		this.roleSystem = roleSystem;
	}
	public boolean isOwnership() {
		return ownership;
	}
	public void setOwnership(boolean ownership) {
		this.ownership = ownership;
	}
	public UUID getTreeNode() {
		return treeNode;
	}
	public void setTreeNode(UUID treeNode) {
		this.treeNode = treeNode;
	}
	@Override
	public UUID getEntity() {
		return this.treeNode;
	}
	@Override
	public void setEntity(UUID entity) {
		this.treeNode = entity;
	} 
	
}
