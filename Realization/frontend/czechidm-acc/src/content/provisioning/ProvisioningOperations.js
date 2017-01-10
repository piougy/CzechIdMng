import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import { Basic, Advanced, Managers } from 'czechidm-core';
import { ProvisioningOperationManager } from '../../redux';
import SystemEntityTypeEnum from '../../domain/SystemEntityTypeEnum';
import ProvisioningOperationTypeEnum from '../../domain/ProvisioningOperationTypeEnum';
import ProvisioningResultStateEnum from '../../domain/ProvisioningResultStateEnum';

const uiKey = 'provisioning-operations-table';
const manager = new ProvisioningOperationManager();

class SystemEntitiesContent extends Basic.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
  }

  getManager() {
    return manager;
  }

  getUiKey() {
    return uiKey;
  }

  getContentKey() {
    return 'acc:content.provisioningOperations';
  }

  componentDidMount() {
    this.selectNavigationItems(['audit', 'provisioning-operations']);
  }

  onRetry(bulkActionValue, ids) {
    this.refs['confirm-' + bulkActionValue].show(
      this.i18n(`action.${bulkActionValue}.message`, { count: ids.length, name: manager.getNiceLabel(manager.getEntity(this.context.store.getState(), ids[0])) }),
      this.i18n(`action.${bulkActionValue}.header`, { count: ids.length})
    ).then(() => {
      this.context.store.dispatch(manager.retry(ids, bulkActionValue));
    }, () => {
      // nothing
    });
  }

  _showRowSelection({ rowIndex, data }) {
    return Managers.SecurityManager.hasAnyAuthority(['APP_ADMIN'])
      && (rowIndex === -1 || (data[rowIndex].resultState !== 'CANCELED' && data[rowIndex].resultState !== 'EXECUTED'));
  }

  render() {
    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <Basic.Confirm ref="confirm-cancel" level="danger"/>
        <Basic.Confirm ref="confirm-retry"/>

        <Basic.ContentHeader>
          <span dangerouslySetInnerHTML={{ __html: this.i18n('header') }}/>
        </Basic.ContentHeader>

        <Basic.Panel className="last">
          <Advanced.Table
            ref="table"
            uiKey={uiKey}
            manager={this.getManager()}
            showRowSelection={this._showRowSelection.bind(this)}
            actions={
              [
                { value: 'retry', niceLabel: this.i18n('action.retry.action'), action: this.onRetry.bind(this) },
                { value: 'cancel', niceLabel: this.i18n('action.cancel.action'), action: this.onRetry.bind(this) }
              ]
            }>
            <Advanced.Column property="resultState" width="75px" header={this.i18n('acc:entity.ProvisioningOperation.resultState')} face="enum" enumClass={ProvisioningResultStateEnum} />
            <Advanced.Column property="created" width="125px" header={this.i18n('entity.created')} sort face="datetime" />
            <Advanced.Column property="operationType" width="75px" header={this.i18n('acc:entity.ProvisioningOperation.operationType')} sort face="enum" enumClass={ProvisioningOperationTypeEnum}/>
            <Advanced.ColumnLink
              to="/system/:_target/detail"
              target="_embedded.system.id"
              access={{ 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SYSTEM_READ']}}
              property="_embedded.system.name"
              header={this.i18n('acc:entity.System.name')} />
            <Advanced.Column property="entityType" width="75px" header={this.i18n('acc:entity.SystemEntity.entityType')} sort face="enum" enumClass={SystemEntityTypeEnum} />
            <Advanced.Column property="systemEntityUid" header={this.i18n('acc:entity.SystemEntity.uid')} sort face="text" />
            <Advanced.Column property="entityIdentifier" header={this.i18n('acc:entity.ProvisioningOperation.entityIdentifier')} sort face="text" />
            <Advanced.Column property="creator" header={this.i18n('acc:entity.ProvisioningOperation.creator')} sort face="text" rendered={false}/>
          </Advanced.Table>
        </Basic.Panel>
      </div>
    );
  }
}

SystemEntitiesContent.propTypes = {
};
SystemEntitiesContent.defaultProps = {
};

function select() {
  return {
  };
}

export default connect(select)(SystemEntitiesContent);
