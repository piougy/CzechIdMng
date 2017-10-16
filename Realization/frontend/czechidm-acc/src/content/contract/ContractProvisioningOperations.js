import React from 'react';
import _ from 'lodash';
import { connect } from 'react-redux';
//
import { Basic, Advanced, Domain, Managers } from 'czechidm-core';
import ProvisioningOperations from '../provisioning/ProvisioningOperations';
import { ProvisioningOperationTable } from '../provisioning/ProvisioningOperationTable';
import SystemEntityTypeEnum from '../../domain/SystemEntityTypeEnum';

const contractManager = new Managers.IdentityContractManager();

/**
 * Contract's provisioning operations (active and archived)
 *
 * @author Radek Tomiška
 */
class ContractProvisioningOperations extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'acc:content.provisioningOperations';
  }

  getNavigationKey() {
    return 'contract-provisioning-operations';
  }

  render() {
    const { entity } = this.props;
    if (!entity) {
      return (
        <Basic.Loading isStatic show />
      );
    }
    const forceSearchParameters = new Domain.SearchParameters()
      .setFilter('entityIdentifier', entity.id)
      .setFilter('entityType', SystemEntityTypeEnum.findKeyBySymbol(SystemEntityTypeEnum.CONTRACT));
    let columns = ProvisioningOperationTable.defaultProps.columns;
    columns = _.difference(columns, ['entityType', 'entityIdentifier']);
    //
    return (
      <div>
        <Basic.ContentHeader>
          { this.i18n('header', { escape: false }) }
        </Basic.ContentHeader>

        <ProvisioningOperations
          uiKey="contract-provisioning-operation-table"
          forceSearchParameters={ forceSearchParameters }
          columns={ columns }/>
      </div>
    );
  }
}

function select(state, component) {
  return {
    entity: contractManager.getEntity(state, component.params.entityId)
  };
}

export default connect(select)(ContractProvisioningOperations);
