import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
//
import { Basic, Advanced } from 'czechidm-core';
import SystemEntityTypeEnum from '../../domain/SystemEntityTypeEnum';
import ProvisioningOperationTypeEnum from '../../domain/ProvisioningOperationTypeEnum';
import ProvisioningResultStateEnum from '../../domain/ProvisioningResultStateEnum';
import EntityInfo from '../../components/EntityInfo';

export class ProvisioningOperationTable extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'acc:content.provisioningOperations';
  }

  clearSelectedRows() {
    this.refs.table.getWrappedInstance().clearSelectedRows();
  }

  reload() {
    this.refs.table.getWrappedInstance().reload();
  }

  render() {
    const { uiKey, manager, showRowSelection, actions, showDetail } = this.props;
    //
    return (
      <Advanced.Table
        ref="table"
        uiKey={uiKey}
        manager={manager}
        showRowSelection={showRowSelection}
        actions={actions}>
        {
          !showDetail
          ||
          <Advanced.Column
            property=""
            header=""
            className="detail-button"
            cell={
              ({ rowIndex, data }) => {
                return (
                  <Advanced.DetailButton
                    title={this.i18n('button.detail')}
                    onClick={() => showDetail(data[rowIndex])}/>
                );
              }
            }/>
        }
        <Advanced.Column
          property="resultState"
          width="75px"
          header={this.i18n('acc:entity.ProvisioningOperation.resultState')}
          face="text"
          cell={
            ({ rowIndex, data }) => {
              const entity = data[rowIndex];
              const content = (<Basic.EnumValue value={entity.resultState} enum={ProvisioningResultStateEnum}/>);
              if (!entity.result || !entity.result.code) {
                return content;
              }
              return (
                <Basic.Tooltip placement="bottom" value={`${this.i18n('detail.resultCode')}: ${entity.result.code}`}>
                  { <span>{content}</span> }
                </Basic.Tooltip>
              );
            }
          }/>
        <Advanced.Column property="created" width="125px" header={this.i18n('entity.created')} sort face="datetime" />
        <Advanced.Column property="operationType" width="75px" header={this.i18n('acc:entity.ProvisioningOperation.operationType')} sort face="enum" enumClass={ProvisioningOperationTypeEnum}/>
        <Advanced.Column property="entityType" width="75px" header={this.i18n('acc:entity.SystemEntity.entityType')} sort face="enum" enumClass={SystemEntityTypeEnum} />
        <Advanced.Column
          property="entityIdentifier"
          header={this.i18n('acc:entity.ProvisioningOperation.entity')}
          face="text"
          cell={
            /* eslint-disable react/no-multi-comp */
            ({ rowIndex, data }) => {
              const entity = data[rowIndex];
              return (
                <EntityInfo entityType={entity.entityType} entityIdentifier={entity.entityIdentifier} face="link"/>
              );
            }
          }/>
        <Advanced.ColumnLink
          to="/system/:_target/detail"
          target="_embedded.system.id"
          access={{ 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['SYSTEM_READ']}}
          property="_embedded.system.name"
          header={this.i18n('acc:entity.System.name')} />
        <Advanced.Column property="systemEntityUid" header={this.i18n('acc:entity.SystemEntity.uid')} sort face="text" />
      </Advanced.Table>
    );
  }
}

ProvisioningOperationTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  manager: PropTypes.object.isRequired,
  /**
   * Enable row selection - checkbox in first cell
   */
  showRowSelection: PropTypes.oneOfType([PropTypes.bool, PropTypes.func]),
  /**
   * Bulk actions e.g. { value: 'activate', niceLabel: this.i18n('content.identities.action.activate.action'), action: this.onActivate.bind(this) }
   */
  actions: PropTypes.arrayOf(PropTypes.object),
  /**
   * Detail  callback
   */
  showDetail: PropTypes.func
};
ProvisioningOperationTable.defaultProps = {
  showRowSelection: false,
  actions: []
};

function select() {
  return {
  };
}


export default connect(select, null, null, { withRef: true })(ProvisioningOperationTable);
