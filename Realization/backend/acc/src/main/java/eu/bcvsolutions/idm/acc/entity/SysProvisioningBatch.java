package eu.bcvsolutions.idm.acc.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * Provisioning requests in the same batch. Any operation has request and batch.
 * 
 * @author Filip Mestanek
 * @author Radek Tomiška
 *
 */
@Entity
@Table(name = "sys_provisioning_batch", indexes = {
		@Index(name = "idx_sys_p_b_next", columnList = "next_attempt")
		})
public class SysProvisioningBatch extends AbstractEntity {

	private static final long serialVersionUID = -546573793473482877L;
	
	@Column(name = "next_attempt")
	private DateTime nextAttempt;
	
	@JsonIgnore
	@OneToMany(mappedBy = "batch")
	@SuppressWarnings("deprecation") // jpa FK constraint does not work in hibernate 4
	@org.hibernate.annotations.ForeignKey( name = "none" )
	private List<SysProvisioningRequest> requests = new ArrayList<>();

	public DateTime getNextAttempt() {
		return nextAttempt;
	}

	public void setNextAttempt(DateTime nextAttempt) {
		this.nextAttempt = nextAttempt;
	}
	
	public void setRequests(List<SysProvisioningRequest> requests) {
		this.requests = requests;
	}
	
	public List<SysProvisioningRequest> getRequests() {
		if (requests == null) {
			requests = new ArrayList<>();
		}
		return requests;
	}
	
	public void addRequest(SysProvisioningRequest request) {
		if (requests.contains(request)) return;
		
		requests.add(request);
		request.setBatch(this);
	}
	
	public void removeRequest(SysProvisioningRequest request) {
		requests.remove(request);
	}

	/**
	 * Returns requests sorted by oldest to newest. 
	 */
	@JsonIgnore
	public List<SysProvisioningRequest> getRequestsByTimeline() {
		List<SysProvisioningRequest> sortedList = new ArrayList<>(getRequests());
		Collections.sort(sortedList, (a, b) -> a.getCreated().compareTo(b.getCreated()));
		return Collections.unmodifiableList(sortedList);
	}
	
	/**
	 * Returns oldest request.
	 */
	@JsonIgnore
	public SysProvisioningRequest getFirstRequest() {
		List<SysProvisioningRequest> requests = getRequestsByTimeline();
		return (requests.isEmpty()) ? null : requests.get(0);
	}
	
	/**
	 * Returns newest request.
	 */
	@JsonIgnore
	public SysProvisioningRequest getLastRequest() {
		List<SysProvisioningRequest> requests = getRequestsByTimeline();
		return (requests.isEmpty()) ? null : requests.get(requests.size() - 1);
	}
}