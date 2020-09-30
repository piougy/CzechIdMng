package eu.bcvsolutions.idm.acc.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;

import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.filter.api.IcResultsHandler;

/**
 * Handler for passing  tree-like entity synchronization results 
 * 
 * @author Ondrej Husnik
 *
 */
public class TreeResultsHandler implements IcResultsHandler {

	// List of all accounts
	private Map<String, IcConnectorObject> accountsMap = new HashMap<>();

	public TreeResultsHandler(Map<String, IcConnectorObject> accountsMap) {
		this.accountsMap = accountsMap;
	}

	@Override
	public boolean handle(IcConnectorObject connectorObject) {
		Assert.notNull(connectorObject, "Connector object is required.");
		Assert.notNull(connectorObject.getUidValue(), "Connector object uid is required.");
		String uid = connectorObject.getUidValue();
		accountsMap.put(uid, connectorObject);
		return true;

	}
}
