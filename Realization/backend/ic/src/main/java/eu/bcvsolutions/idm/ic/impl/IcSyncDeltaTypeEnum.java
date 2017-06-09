package eu.bcvsolutions.idm.ic.impl;

/**
 * The type of change.
 */
public enum IcSyncDeltaTypeEnum {
    /**
     * The change represents either a create or an update in the resource.
     *
     * These are combined into a single value because:
     * <ol>
     * <li>Many resources will not be able to distinguish a create from an
     * update. Those that have an audit log will be able to. However, many
     * implementations will only have the current record and a modification
     * timestamp.</li>
     * <li>Regardless of whether or not the resource can distinguish the two
     * cases, the application needs to distinguish.</li>
     * </ol>
     */
    CREATE_OR_UPDATE,

    /**
     * The change represents a DELETE in the resource.
     */
    DELETE,

    /**
     * The change represents a CREATE in the resource.
     * <p/>
     * Experimental type to support better event mechanism where it's possible.
     *
     * @see #CREATE_OR_UPDATE
     * @since 1.4
     */
    CREATE,

    /**
     * The change represents a UPDATE in the resource.
     * <p/>
     * Experimental type to support better event mechanism where it's possible.
     *
     * @see #CREATE_OR_UPDATE
     * @since 1.4
     */
    UPDATE
}
