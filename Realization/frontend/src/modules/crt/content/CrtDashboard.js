

import React from 'react';
import { Link }  from 'react-router';
//
import * as Basic from '../../../components/basic';


export default class CrtDashboard extends Basic.AbstractContent {

  onUsersCertificates(open){
    this.context.router.push('/user/' + this.props.userID + '/certificates?open=' + (open ? '1' : '0'))
  }

  getContentKey() {
    return 'crt:content.dashboard';
  }

  render() {
    return (
      <Basic.Panel>
        <Basic.PanelHeader text={this.i18n('header')}/>
        <Basic.PanelBody>
          <div className="col-xs-2" style={{color:'#337AB7'}}>
            <Basic.Icon type="fa" icon="certificate" className="fa-4x"/>
          </div>
          <div className="col-xs-8">
            <Basic.Button className="btn-block" disabled={false} level="success" onClick={this.onUsersCertificates.bind(this, true)}>
              {this.i18n('button.addCertificate')}
            </Basic.Button>
            <Basic.Button className="btn-block" disabled={false} level="info" onClick={this.onUsersCertificates.bind(this, false)}>
              {this.i18n('button.showCertificates')}
            </Basic.Button>
          </div>
        </Basic.PanelBody>
      </Basic.Panel>
    );
  }
}
