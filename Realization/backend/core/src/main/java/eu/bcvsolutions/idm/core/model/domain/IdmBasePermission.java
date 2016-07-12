package eu.bcvsolutions.idm.core.model.domain;

public enum IdmBasePermission implements BasePermission {
	
	READ,
	WRITE,
	DELETE;
	
	@Override
	public String getName() {
		return name();
	}
}
