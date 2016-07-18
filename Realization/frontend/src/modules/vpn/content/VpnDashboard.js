

import React from 'react';
import { Link }  from 'react-router';
import * as Basic from '../../../components/basic';


export default class VpnDashboard extends Basic.AbstractContent {

  onUsersVpns(open){
    this.context.router.push('/user/' + this.props.userID + '/vpns?open=' + (open ? '1' : '0'))
  }

  getContentKey() {
    return 'vpn:content.dashboard';
  }

  render() {
    return (
      <Basic.Panel>
        <Basic.PanelHeader text={this.i18n('header')}/>
        <Basic.PanelBody>
          <div className="col-xs-2">
            <Basic.Icon type="fa" icon="key" className="fa-4x" style={{color: '#A94442'}}/>
          </div>
          <div className="col-xs-8">
            <Basic.Button className="btn-block" disabled={false} level="success" onClick={this.onUsersVpns.bind(this, true)}>
              {this.i18n('button.addVpn')}
            </Basic.Button>
            <Basic.Button className="btn-block" disabled={false} level="info" onClick={this.onUsersVpns.bind(this, false)}>
              {this.i18n('button.showVpns')}
            </Basic.Button>
          </div>
        </Basic.PanelBody>
      </Basic.Panel>
    );
  }
}
