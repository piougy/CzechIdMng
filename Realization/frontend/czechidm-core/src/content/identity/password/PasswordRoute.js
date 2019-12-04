import React from 'react';
//
import * as Basic from '../../../components/basic';
import * as Advanced from '../../../components/advanced';

/**
 * Identity password tab with tabs like password change and infromation about
 * password metadata
 *
 * @author Ondrej Kopr
 */
export default class PasswordRoute extends Basic.AbstractContent {

  render() {
    return (
      <Basic.Div style={{ paddingTop: 15 }}>
        <Advanced.TabPanel
          position="top"
          parentId="profile-password"
          match={ this.props.match }>
          { this.getRoutes() }
        </Advanced.TabPanel>
      </Basic.Div>
    );
  }
}
