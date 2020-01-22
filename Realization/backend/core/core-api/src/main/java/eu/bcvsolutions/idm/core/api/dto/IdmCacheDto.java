package eu.bcvsolutions.idm.core.api.dto;

import org.springframework.hateoas.core.Relation;

@Relation(collectionRelation = "caches")
public class IdmCacheDto extends AbstractComponentDto {

    private static final long serialVersionUID = 1L;

    private long size;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
