package eu.bcvsolutions.idm.vs.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * Relation between virtual system requests
 * 
 * @author Svanda
 * @author Tomiska
 *
 */
@Entity
@Table(name = "vs_request_batch")
public class VsRequestBatch extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	@JsonIgnore
	@OneToMany(mappedBy = "batch")
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in
										// hibernate 4
	@org.hibernate.annotations.ForeignKey(name = "none")
	private List<VsRequest> requests = new ArrayList<>();

	public void setRequests(List<VsRequest> requests) {
		this.requests = requests;
	}

	public List<VsRequest> getRequests() {
		if (requests == null) {
			requests = new ArrayList<>();
		}
		return requests;
	}

	public void addRequest(VsRequest request) {
		if (requests.contains(request)) {
			return;
		}

		requests.add(request);
	}

	public void removeRequest(VsRequest request) {
		requests.remove(request);
	}

	/**
	 * Returns requests sorted by oldest to newest.
	 */
	@JsonIgnore
	public List<VsRequest> getRequestsByTimeline() {
		List<VsRequest> sortedList = new ArrayList<>(getRequests());
		Collections.sort(sortedList, (a, b) -> a.getCreated().compareTo(b.getCreated()));
		return Collections.unmodifiableList(sortedList);
	}

	/**
	 * Returns oldest request.
	 */
	@JsonIgnore
	public VsRequest getFirstRequest() {
		List<VsRequest> requests = getRequestsByTimeline();
		return (requests.isEmpty()) ? null : requests.get(0);
	}

	/**
	 * Returns newest request.
	 */
	@JsonIgnore
	public VsRequest getLastRequest() {
		List<VsRequest> requests = getRequestsByTimeline();
		return (requests.isEmpty()) ? null : requests.get(requests.size() - 1);
	}
}