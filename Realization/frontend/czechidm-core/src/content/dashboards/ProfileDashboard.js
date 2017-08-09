import React from 'react';
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';

/**
 * Identity info with link to profile
 *
 * @author Radek Tomiška
 */
export default class ProfileDashboard extends Basic.AbstractContent {

  getContentKey() {
    return 'dashboard.profileDashboard';
  }

  render() {
    return (
      <Basic.Panel>
        <Basic.PanelHeader text={this.i18n('header')}/>
        <Basic.PanelBody >
          <Advanced.EntityInfo entityType="identity" entityIdentifier={this.props.entityId} showLink style={{ marginBottom: 0 }}/>
        </Basic.PanelBody>
      </Basic.Panel>
    );
  }
}
