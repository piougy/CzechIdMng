import React from 'react';
//
import { Basic } from 'czechidm-core';
import ProvisioningOperations from './ProvisioningOperations';

/**
 * Route only
 *
 * @author Radek Tomiška
 */
export default class AuditProvisioningOperations extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'acc:content.provisioningOperations';
  }

  componentDidMount() {
    this.selectNavigationItems(['audit', 'provisioning-operations']);
  }

  render() {
    return (
      <div>
        <Basic.PageHeader>
          <span dangerouslySetInnerHTML={{ __html: this.i18n('header') }}/>
        </Basic.PageHeader>

        <ProvisioningOperations uiKey="provisioning-operation-audit-table"/>
      </div>
    );
  }
}
