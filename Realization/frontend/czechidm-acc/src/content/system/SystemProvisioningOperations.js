import React from 'react';
import _ from 'lodash';
//
import { Basic, Domain } from 'czechidm-core';
import ProvisioningOperations from '../provisioning/ProvisioningOperations';
import ProvisioningOperationTable from '../provisioning/ProvisioningOperationTable';

export default class SystemProvisioningOparationContent extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'acc:content.provisioningOperations';
  }

  componentDidMount() {
    this.selectNavigationItems(['sys-systems', 'system-provisioning-operations']);
  }

  render() {
    const forceSearchParameters = new Domain.SearchParameters().setFilter('systemId', this.props.params.entityId);
    let columns = ProvisioningOperationTable.defaultProps.columns;
    columns = _.difference(columns, ['system']);
    //
    return (
      <div>
        <Basic.ContentHeader>
          { this.i18n('header', { escape: false }) }
        </Basic.ContentHeader>

        <ProvisioningOperations
          uiKey="system-provisioning-operation-table"
          forceSearchParameters={ forceSearchParameters }
          columns={ columns }/>
      </div>
    );
  }
}
