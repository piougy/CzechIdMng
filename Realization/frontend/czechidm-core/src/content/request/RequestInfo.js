import React from 'react';
import * as Basic from '../../components/basic';
import RequestItemChangesTable from './RequestItemChangesTable';
import {RequestManager } from '../../redux';

/**
 * Request info component
 *
 * @author Vít Švanda
 */
class RequestInfo extends Basic.AbstractContent {

  componentDidMount() {
    super.componentDidMount();
    this.requestManager = new RequestManager();
  }

  _gotToRequest() {
    const {requestId} = this.props.params;
    // Redirect to request
    this.context.router.push(`requests/${requestId}/detail`);
  }

  _showItemChanges() {
    const params = this.props.params;
    const entityId = this._getEntityId(params, this.props.location.pathname);
    this.requestManager.getService().getChanges(params.requestId, entityId)
    .then(json => {
      this.setState({itemDetail: {changes: json, show: true}});
    })
    .catch(error => {
      this.addError(error);
    });
  }

  _getEntityId(params, path) {
    const paths = path.split('/').reverse();
    for (const p of paths) {
      for (const key in params) {
        if (params.hasOwnProperty(key)) {
          if (p === params[key]) {
            return params[key];
          }
        }
      }
    }
    return null;
  }

  closeDetail() {
    this.setState({itemDetail: {show: false}});
  }

  render() {
    const {_showLoading} = this.props;
    const itemDetail = this.state ? this.state.itemDetail : null;

    return (
      <div>
        <Basic.Alert
          level="warning"
          title={this.i18n('content.requestInfo.title')}
          text={this.i18n('content.requestInfo.text')}
          className="no-margin"
          buttons={[
            <Basic.Button
            level="warning"
            key="showChanges"
            onClick={ this._showItemChanges.bind(this) }
            title={this.i18n('content.requestInfo.button.showChanges.tooltip')}
            titlePlacement="bottom">
            {' '}
            { this.i18n('content.requestInfo.button.showChanges.label') }
            </Basic.Button>,
            <Basic.Button
              level="primary"
              key="gotToRequest"
              style={{marginLeft: '5px'}}
              onClick={ this._gotToRequest.bind(this) }
              titlePlacement="bottom">
              <Basic.Icon type="fa" icon="exchange"/>
              {' '}
              { this.i18n('content.requestInfo.button.goToRequest.label') }
            </Basic.Button>
          ]}/>
          { this.props.children }
          <Basic.Modal
            show={itemDetail && itemDetail.show}
            onHide={this.closeDetail.bind(this)}
            backdrop="static"
            keyboard={!_showLoading}>
              <Basic.Modal.Header closeButton={ !_showLoading } text={this.i18n('content.requestDetail.itemDetail.header')}/>
              <Basic.Modal.Body>
                <RequestItemChangesTable
                  itemData={itemDetail ? itemDetail.changes : null}/>
              </Basic.Modal.Body>
              <Basic.Modal.Footer>
                <Basic.Button
                  level="link"
                  onClick={ this.closeDetail.bind(this) }
                  showLoading={ _showLoading }>
                  { this.i18n('button.close') }
                </Basic.Button>
              </Basic.Modal.Footer>
          </Basic.Modal>
      </div>
    );
  }
}

export default RequestInfo;
