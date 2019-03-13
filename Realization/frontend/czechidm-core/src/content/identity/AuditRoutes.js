import React from 'react';
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';

/**
 * Audit routes
 *
 * @author Ondrej Kopr
 * @since 9.5.0
 */

export default class AuditRoutes extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.audit';
  }

  render() {
    return (
      <Basic.Div style={{ paddingTop: 15 }}>
        <Advanced.TabPanel
          position="top"
          parentId="profile-audit"
          params={this.props.params}>
          {this.props.children}
        </Advanced.TabPanel>
      </Basic.Div>
    );
  }
}
