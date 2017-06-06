import React from 'react';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';

/**
 * Default content (routes diff) for audits
 */

export default class AuditRoutes extends Basic.AbstractContent {

  getContentKey() {
    return 'content.audit';
  }

  componentDidMount() {
  }

  render() {
    return (
      <div>
        <Basic.PageHeader>
          <Basic.Icon value="eye-open"/>
          {' '}
          {this.i18n('header')}
        </Basic.PageHeader>

        <Advanced.TabPanel position="top" parentId="audits" params={this.props.params}>
          {this.props.children}
        </Advanced.TabPanel>
      </div>
    );
  }
}

AuditRoutes.propTypes = {
};
AuditRoutes.defaultProps = {
};
