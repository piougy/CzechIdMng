import React from 'react';
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';

export default class ProfileDashboard extends Basic.AbstractContent {

  _goToProfil() {
    this.context.router.push('/user/' + this.props.userID + '/profile');
  }

  getContentKey() {
    return 'dashboard.profileDashboard';
  }

  render() {
    return (
      <Basic.Panel style={{maxWidth: '500px'}}>
        <Basic.PanelHeader text={this.i18n('header')}/>
        <Basic.PanelBody >
          <Advanced.IdentityInfo username={this.props.userID}/>
          <div className="col-lg-8 col-lg-offset-2">
            <Basic.Button className="btn-block" level="success" onClick={this._goToProfil.bind(this)}>
              {this.i18n('goToProfil')}
            </Basic.Button>
          </div>
        </Basic.PanelBody>
      </Basic.Panel>
    );
  }
}
