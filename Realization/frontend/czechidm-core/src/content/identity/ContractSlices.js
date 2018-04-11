import React from 'react';
import uuid from 'uuid';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import { ContractSliceManager, TreeTypeManager, TreeNodeManager, SecurityManager } from '../../redux';
import SearchParameters from '../../domain/SearchParameters';
import ManagersInfo from './ManagersInfo';
import ContractStateEnum from '../../enums/ContractStateEnum';

const uiKey = 'contract-slices';

/**
 * Contract slice
 *
 * @author Vít Švanda
 */
export default class ContractSlices extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.contractSliceManager = new ContractSliceManager();
    this.treeTypeManager = new TreeTypeManager();
    this.treeNodeManager = new TreeNodeManager();
  }

  getUiKey() {
    const { entityId } = this.props.params;
    //
    return `${uiKey}-${entityId}`;
  }

  getManager() {
    return this.contractSliceManager;
  }

  getContentKey() {
    return 'content.identity.contractSlices';
  }

  getNavigationKey() {
    return 'profile-contract-slices';
  }

  showDetail(entity, event) {
    if (event) {
      event.preventDefault();
    }
    //
    const { entityId } = this.props.params;
    if (entity.id === undefined) {
      const uuidId = uuid.v1();
      this.context.router.push(`/identity/${encodeURIComponent(entityId)}/contract-slice/${uuidId}/new?new=1`);
    } else {
      this.context.router.push(`/identity/${encodeURIComponent(entityId)}/contract-slice/${entity.id}/detail`);
    }
  }

  render() {
    const { entityId } = this.props.params;
    //
    return (
      <div>
        <Basic.Confirm ref="confirm-delete" level="danger"/>

        { this.renderContentHeader({ style: { marginBottom: 0 } }) }

        <Basic.Panel className="no-border">
          <Advanced.Table
            ref="table"
            uiKey={ this.getUiKey() }
            manager={ this.contractSliceManager }
            forceSearchParameters={ new SearchParameters().setFilter('identity', entityId) }
            rowClass={({rowIndex, data}) => { return data[rowIndex].state ? 'disabled' : Utils.Ui.getRowClass(data[rowIndex]); }}
            showRowSelection={ SecurityManager.hasAuthority('IDENTITYCONTRACT_DELETE') }
            actions={
              [
                { value: 'delete', niceLabel: this.i18n('action.delete.action'), action: this.onDelete.bind(this) },
              ]
            }
            buttons={
              [
                <Basic.Button level="success" className="btn-xs" onClick={this.showDetail.bind(this, {})} rendered={ SecurityManager.hasAuthority('IDENTITYCONTRACT_CREATE') }>
                  <Basic.Icon value="fa:plus"/>
                  {' '}
                  {this.i18n('button.add')}
                </Basic.Button>
              ]
            }>
            <Basic.Column
              className="detail-button"
              cell={
                ({rowIndex, data}) => {
                  return (
                    <Advanced.DetailButton onClick={this.showDetail.bind(this, data[rowIndex])}/>
                  );
                }
              }/>
            <Advanced.Column
              property="main"
              header={this.i18n('entity.ContractSlice.main.label')}
              face="bool"
              width={75}
              sort/>
            <Basic.Column
              property="workPosition"
              header={this.i18n('entity.ContractSlice.workPosition')}
              width={ 350 }
              cell={
                ({ rowIndex, data }) => {
                  return (
                    <span>
                      {
                        data[rowIndex]._embedded && data[rowIndex]._embedded.workPosition
                        ?
                        <Advanced.EntityInfo
                          entity={ data[rowIndex]._embedded.workPosition }
                          entityType="treeNode"
                          entityIdentifier={ data[rowIndex].workPosition }
                          face="popover" />
                        :
                        data[rowIndex].position
                      }
                    </span>
                  );
                }
              }
            />
            <Basic.Column
              property="guarantee"
              header={<span title={this.i18n('entity.ContractSlice.managers.title')}>{this.i18n('entity.ContractSlice.managers.label')}</span>}
              cell={
                ({ rowIndex, data }) => {
                  return (
                    <ManagersInfo managersFor={entityId} contractSliceId={data[rowIndex].id}/>
                  );
                }
              }
            />
            <Advanced.Column
              property="validFrom"
              header={this.i18n('entity.ContractSlice.validFrom')}
              face="date"
              sort
            />
            <Advanced.Column
              property="validTill"
              header={this.i18n('entity.ContractSlice.validTill')}
              face="date"
              sort/>
            <Advanced.Column
              property="disabled"
              header={this.i18n('entity.ContractSlice.disabled.label')}
              face="bool"
              width={100}
              sort/>
            <Advanced.Column
              property="state"
              header={this.i18n('entity.ContractSlice.state.label')}
              face="enum"
              enumClass={ ContractStateEnum }
              width={100}
              sort/>
            <Advanced.Column
              property="externe"
              header={this.i18n('entity.ContractSlice.externe')}
              face="bool"
              width={100}
              sort/>
          </Advanced.Table>
        </Basic.Panel>
      </div>
    );
  }
}
