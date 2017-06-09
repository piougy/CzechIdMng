import React from 'react';
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';

export default class ProfileDashboard extends Basic.AbstractContent {

  _goToProfil() {
    this.context.router.push('/identity/' + this.props.entityId + '/profile');
  }

  getContentKey() {
    return 'dashboard.profileDashboard';
  }

  render() {
    return (
      <Basic.Panel>
        <Basic.PanelHeader text={this.i18n('header')}/>
        <Basic.PanelBody >
          <Advanced.EntityInfo entityType="identity" entityIdentifier={this.props.entityId} showLink/>
        </Basic.PanelBody>
      </Basic.Panel>
    );
  }
}
