import React from 'react';
import uuid from 'uuid';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import { IdentityContractManager, TreeTypeManager, TreeNodeManager, SecurityManager } from '../../redux';
import SearchParameters from '../../domain/SearchParameters';
import ManagersInfo from './ManagersInfo';

const uiKey = 'identity-contracts';

export default class IdentityContracts extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.identityContractManager = new IdentityContractManager();
    this.treeTypeManager = new TreeTypeManager();
    this.treeNodeManager = new TreeNodeManager();
  }

  getManager() {
    return this.identityContractManager;
  }

  getContentKey() {
    return 'content.identity.identityContracts';
  }

  getNavigationKey() {
    return 'profile-contracts';
  }

  showDetail(entity, event) {
    if (event) {
      event.preventDefault();
    }
    //
    const { entityId } = this.props.params;
    if (entity.id === undefined) {
      const uuidId = uuid.v1();
      this.context.router.push(`/identity/${entityId}/identity-contract/${uuidId}/new?new=1`);
    } else {
      this.context.router.push(`/identity/${entityId}/identity-contract/${entity.id}/detail`);
    }
  }

  onDelete(entity, event) {
    if (event) {
      event.preventDefault();
    }
    //
    const { entityId } = this.props.params;
    this.refs['confirm-delete'].show(
      this.i18n(`action.delete.message`, { count: 1, record: this.getManager().getNiceLabel(entity) }),
      this.i18n(`action.delete.header`, { count: 1 })
    ).then(() => {
      this.context.store.dispatch(this.getManager().deleteEntity(entity, `${uiKey}-${entityId}`, (deletedEntity, error) => {
        if (!error) {
          this.addMessage({ message: this.i18n('delete.success', { position: this.getManager().getNiceLabel(entity), username: entityId }) });
        } else {
          this.addError(error);
        }
      }));
    }, () => {
      // Rejected
    });
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
            uiKey={uiKey}
            manager={ this.identityContractManager }
            forceSearchParameters={ new SearchParameters().setFilter('identity', entityId) }
            rowClass={({rowIndex, data}) => { return Utils.Ui.getRowClass(data[rowIndex]); }}
            buttons={
              [
                <Basic.Button level="success" className="btn-xs" onClick={this.showDetail.bind(this, {})} rendered={SecurityManager.isAdmin()}>
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
              }
              rendered={SecurityManager.isAdmin()}/>
            <Advanced.Column
              property="main"
              header={this.i18n('entity.IdentityContract.main.label')}
              face="bool"
              width={75}
              sort/>
            <Basic.Column
              property="workingPosition"
              header={this.i18n('entity.IdentityContract.workingPosition')}
              width="175px"
              cell={
                ({ rowIndex, data }) => {
                  return (
                    <span>
                      {
                        data[rowIndex]._embedded && data[rowIndex]._embedded.workingPosition
                        ?
                        this.treeNodeManager.getNiceLabel(data[rowIndex]._embedded.workingPosition)
                        :
                        data[rowIndex].position
                      }
                    </span>
                  );
                }
              }
            />
            <Basic.Column
              property="treeType"
              header={this.i18n('entity.IdentityContract.treeType')}
              width="175px"
              cell={
                ({ rowIndex, data }) => {
                  return (
                    <span>
                      {
                        !data[rowIndex]._embedded || !data[rowIndex]._embedded.workingPosition
                        ||
                        this.treeTypeManager.getNiceLabel(data[rowIndex]._embedded.workingPosition.treeType)
                      }
                    </span>
                  );
                }
              }
            />
            <Basic.Column
              property="guarantee"
              header={<span title={this.i18n('entity.IdentityContract.managers.title')}>{this.i18n('entity.IdentityContract.managers.label')}</span>}
              cell={
                ({ rowIndex, data }) => {
                  return (
                    <ManagersInfo identityContract={data[rowIndex]}/>
                  );
                }
              }
            />
            <Advanced.Column
              property="validFrom"
              header={this.i18n('entity.IdentityContract.validFrom')}
              face="date"
              sort
            />
            <Advanced.Column
              property="validTill"
              header={this.i18n('entity.IdentityContract.validTill')}
              face="date"
              sort/>
            <Advanced.Column
              property="externe"
              header={this.i18n('entity.IdentityContract.externe')}
              face="bool"
              width={100}
              sort/>
            <Basic.Column
              header={this.i18n('label.action')}
              className="action"
              cell={
                ({rowIndex, data}) => {
                  return (
                    <Basic.Button
                      level="danger"
                      onClick={this.onDelete.bind(this, data[rowIndex])}
                      className="btn-xs"
                      title={this.i18n('button.delete')}
                      titlePlacement="bottom">
                      <Basic.Icon icon="trash"/>
                    </Basic.Button>
                  );
                }
              }
              rendered={SecurityManager.isAdmin()}/>
          </Advanced.Table>
        </Basic.Panel>
      </div>
    );
  }
}
