import React from 'react';
import _ from 'lodash';
//
import { Basic, Domain } from 'czechidm-core';
import ProvisioningOperations from '../provisioning/ProvisioningOperations';
import { ProvisioningOperationTable } from '../provisioning/ProvisioningOperationTable';

/**
 * Provisioning opretions route.
 *
 * @author Radek Tomiška
 */
export default class SystemProvisioningOparationContent extends Basic.AbstractContent {


  getContentKey() {
    return 'acc:content.provisioningOperations';
  }

  getNavigationKey() {
    return 'system-provisioning-operations';
  }

  render() {
    const forceSearchParameters = new Domain.SearchParameters().setFilter('systemId', this.props.match.params.entityId);
    let columns = ProvisioningOperationTable.defaultProps.columns;
    columns = _.difference(columns, ['system']);
    //
    return (
      <Basic.Div>
        <Basic.ContentHeader>
          { this.i18n('header', { escape: false }) }
        </Basic.ContentHeader>

        <ProvisioningOperations
          uiKey="system-provisioning-operation-table"
          forceSearchParameters={ forceSearchParameters }
          columns={ columns }/>
      </Basic.Div>
    );
  }
}
