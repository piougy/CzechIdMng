package eu.bcvsolutions.idm.core.model.domain;

public enum CustomBasePermission implements BasePermission {
	
	ADMIN;
	
	@Override
	public String getName() {
		return name();
	}
}
