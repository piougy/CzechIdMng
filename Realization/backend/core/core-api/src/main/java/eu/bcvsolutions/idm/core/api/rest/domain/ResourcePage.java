package eu.bcvsolutions.idm.core.api.rest.domain;

/**
 * @deprecated use Spring Pageable instead
 */
@Deprecated
public class ResourcePage {
	private long size;
	private long totalElements;
	private long totalPages;
	private long number;
	

	public ResourcePage(long size, long totalElements, long totalPages, long number) {
		super();
		this.size = size;
		this.totalElements = totalElements;
		this.totalPages = totalPages;
		this.number = number;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public long getTotalElements() {
		return totalElements;
	}

	public void setTotalElements(long totalElements) {
		this.totalElements = totalElements;
	}

	public long getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(long totalPages) {
		this.totalPages = totalPages;
	}

	public long getNumber() {
		return number;
	}

	public void setNumber(long number) {
		this.number = number;
	}

}
