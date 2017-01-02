package eu.bcvsolutions.idm.ic.impl;

import eu.bcvsolutions.idm.ic.api.IcObjectPoolConfiguration;

public class IcObjectPoolConfigurationImpl implements IcObjectPoolConfiguration {
	
	/**
     * Max objects (idle+active).
     */
    private int maxObjects = 10;

    /**
     * Max idle objects.
     */
    private int maxIdle = 10;

    /**
     * Max time to wait if the pool is waiting for a free object to become
     * available before failing. Zero means don't wait.
     */
    private long maxWait = 150 * 1000;

    /**
     * Minimum time to wait before evicting an idle object. Zero means don't
     * wait
     */
    private long minEvictableIdleTimeMillis = 120 * 1000;

    /**
     * Minimum number of idle objects.
     */
    private int minIdle = 1;

	/**
	 * @return the maxObjects
	 */
	@Override
	public int getMaxObjects() {
		return maxObjects;
	}

	/**
	 * @param maxObjects the maxObjects to set
	 */
	@Override
	public void setMaxObjects(int maxObjects) {
		this.maxObjects = maxObjects;
	}

	/**
	 * @return the maxIdle
	 */
	@Override
	public int getMaxIdle() {
		return maxIdle;
	}

	/**
	 * @param maxIdle the maxIdle to set
	 */
	@Override
	public void setMaxIdle(int maxIdle) {
		this.maxIdle = maxIdle;
	}

	/**
	 * @return the maxWait
	 */
	@Override
	public long getMaxWait() {
		return maxWait;
	}

	/**
	 * @param maxWait the maxWait to set
	 */
	@Override
	public void setMaxWait(long maxWait) {
		this.maxWait = maxWait;
	}

	/**
	 * @return the minEvictableIdleTimeMillis
	 */
	@Override
	public long getMinEvictableIdleTimeMillis() {
		return minEvictableIdleTimeMillis;
	}

	/**
	 * @param minEvictableIdleTimeMillis the minEvictableIdleTimeMillis to set
	 */
	@Override
	public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
		this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
	}

	/**
	 * @return the minIdle
	 */
	@Override
	public int getMinIdle() {
		return minIdle;
	}

	/**
	 * @param minIdle the minIdle to set
	 */
	@Override
	public void setMinIdle(int minIdle) {
		this.minIdle = minIdle;
	}
    
    
    
}
