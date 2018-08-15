import React from 'react';
import * as Basic from '../../components/basic';
// import { RequestManager} from '../../redux';
// const requestManager = new RequestManager();

/**
 * Request info component
 *
 * @author Vít Švanda
 */
class RequestInfo extends Basic.AbstractContent {

  componentDidMount() {
    super.componentDidMount();
  }

  _gotToRequest() {
    const {requestId} = this.props.params;
    debugger;
    // Redirect to request
    this.context.router.push(`requests/${requestId}/detail`);
  }

  render() {
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
            onClick={ this._gotToRequest.bind(this) }
            title={this.i18n('content.requestInfo.button.showChanges.tooltip')}
            titlePlacement="bottom">
            {' '}
            { this.i18n('content.requestInfo.button.showChanges.label') }
            </Basic.Button>,
            <Basic.Button
              level="primary"
              style={{marginLeft: '5px'}}
              onClick={ this._gotToRequest.bind(this) }
              titlePlacement="bottom">
              <Basic.Icon type="fa" icon="exchange"/>
              {' '}
              { this.i18n('content.requestInfo.button.goToRequest.label') }
            </Basic.Button>
          ]}/>
          { this.props.children }
      </div>
    );
  }
}

export default RequestInfo;
