package eu.bcvsolutions.idm.ic.api;

import java.io.Serializable;

/**
 * Keep pool configuration
 * @author svandav
 *
 */
public interface IcObjectPoolConfiguration extends Serializable {

	/**
	 * @return the maxObjects
	 */
	int getMaxObjects();

	/**
	 * @param maxObjects the maxObjects to set
	 */
	void setMaxObjects(int maxObjects);

	/**
	 * @return the maxIdle
	 */
	int getMaxIdle();

	/**
	 * @param maxIdle the maxIdle to set
	 */
	void setMaxIdle(int maxIdle);

	/**
	 * @return the maxWait
	 */
	long getMaxWait();

	/**
	 * @param maxWait the maxWait to set
	 */
	void setMaxWait(long maxWait);

	/**
	 * @return the minEvictableIdleTimeMillis
	 */
	long getMinEvictableIdleTimeMillis();

	/**
	 * @param minEvictableIdleTimeMillis the minEvictableIdleTimeMillis to set
	 */
	void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis);

	/**
	 * @return the minIdle
	 */
	int getMinIdle();

	/**
	 * @param minIdle the minIdle to set
	 */
	void setMinIdle(int minIdle);

}