
/**
 * Long polling service
 *
 * @author Vít Švanda
 * @since 9.7.7
 */
export default class LongPollingManager {

  static sendLongPollingRequest(entityId, service) {
    // Is pooling enabled? Do we have all required permissions
    if (!this._isLongPollingEnabled()) {
      this.canSendLongPollingRequest = false;
    }
    if (!this.canSendLongPollingRequest) {
      this.setState({longPollingInprogress: false});
    } else {
      // I do not want wait to render, I need to send request ASAP
      this.setState({longPollingInprogress: true});
      service.sendLongPollingRequest(entityId).then(result => {
        if (this.canSendLongPollingRequest) {
          if (result && result.state === 'RUNNING') {
            // Change of entity was detected, we need to execute
            // refresh and create new long-polling reqeust.
            this.setState({longPollingInprogress: true}, () => {
              this._sendLongPollingRequest(entityId);
              this._refreshAll();
            });
          } else if (result && result.state === 'NOT_EXECUTED') {
            // None change for entity was made. We will send next long-polling checking request
            this._sendLongPollingRequest(entityId);
          //  this._refreshAll();
          } else if (result && result.state === 'BLOCKED') {
            // Long pooling is blocked on BE!
            this.setState({longPollingInprogress: false,
              automaticRefreshOn: false}, () => {
              this.canSendLongPollingRequest = false;
            });
          }
        } else {
          this.setState({longPollingInprogress: false});
        }
      })
        .catch(error => {
          this.addError(error);
          this.canSendLongPollingRequest = false;
          this.setState({longPollingInprogress: false});
        });
    }
  }

  static toggleAutomaticRefresh() {
    const canSendLongPollingRequest = this.canSendLongPollingRequest;

    this.canSendLongPollingRequest = !canSendLongPollingRequest;
    this.setState({
      automaticRefreshOn: !canSendLongPollingRequest
    }, () => {
      if (this.canSendLongPollingRequest) {
        this._refreshAll();
      }
    });
  }
}
