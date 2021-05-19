import React from 'react';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';

/**
 * Default content (routes diff) for audits.
 *
 * @author Ondřej Kopr
 * @author Radek Tomiška
 */
export default class AuditRoutes extends Basic.AbstractContent {

  getContentKey() {
    return 'content.audit';
  }

  render() {
    return (
      <Basic.Div>
        { this.renderPageHeader() }

        <Advanced.TabPanel position="top" parentId="audits" match={ this.props.match }>
          { this.getRoutes() }
        </Advanced.TabPanel>
      </Basic.Div>
    );
  }
}

AuditRoutes.propTypes = {
};
AuditRoutes.defaultProps = {
};
