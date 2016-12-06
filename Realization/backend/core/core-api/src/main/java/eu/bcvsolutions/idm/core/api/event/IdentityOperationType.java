package eu.bcvsolutions.idm.core.api.event;

/**
 * Supported identity events
 * 
 * @author Radek Tomiška
 *
 */
public enum IdentityOperationType {
	SAVE, DELETE, PASSWORD // TODO: split SAVE to UPDATE / CREATE?
}
