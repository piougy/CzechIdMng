import React from 'react';
import Helmet from 'react-helmet';
//
import { Basic, Advanced, Domain, Managers } from 'czechidm-core';
import { ProvisioningBreakConfigManager } from '../../redux';
import uuid from 'uuid';
import ProvisioningOperationTypeEnum from '../../domain/ProvisioningOperationTypeEnum';

const uiKey = 'provisioning-break-config-table';
const manager = new ProvisioningBreakConfigManager();

/**
* @author Ondrej Kopr
*/
export default class SystemProvisioningBreakConfigs extends Advanced.AbstractTableContent {

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
    return 'acc:content.provisioningBreakConfig';
  }

  componentDidMount() {
    this.selectNavigationItems(['sys-systems', 'system-provisioning-break-config']);
  }

  showDetail(entity, add) {
    const systemId = entity._embedded && entity._embedded.system ? entity._embedded.system.id : this.props.params.entityId;
    if (add) {
      // When we add new provisiong break configuration use random uuid
      const uuidId = uuid.v1();
      this.context.router.push(`system/${systemId}/break-configs/${uuidId}/new?new=1`);
    } else {
      this.context.router.push(`system/${systemId}/break-configs/${entity.id}/detail`);
    }
  }

  /**
   * Bulk delete operation for
   */
  onDelete(bulkActionValue, selectedRows) {
    const selectedEntities = this.getManager().getEntitiesByIds(this.context.store.getState(), selectedRows);
    const savedEntities = [];
    for (const index in selectedEntities) {
      if (selectedEntities.hasOwnProperty(index)) {
        // delete only entity that isn't set globalConfiguration
        const entity = selectedEntities[index];
        if (entity && !entity.globalConfiguration) {
          savedEntities.push(entity);
        } else {
          // NOTE: info about global configuration is in message
          // notSavedEntities.push(entity);
          // this.addMessage({
          //   message: this.i18n(`acc:error.PROVISIONING_BREAK_GLOBAL_CONFIG_DELETE.message`, { name: this.getManager().getNiceLabel(entity) }),
          //   title: this.i18n(`acc:error.PROVISIONING_BREAK_GLOBAL_CONFIG_DELETE.title`),
          //   level: 'warning'
          // });
        }
      }
    }
    //
    if (savedEntities.length > 0) {
      this.refs['confirm-' + bulkActionValue].show(
        this.i18n(`action.${bulkActionValue}.message`, { count: savedEntities.length, record: this.getManager().getNiceLabel(savedEntities[0]), records: this.getManager().getNiceLabels(savedEntities).join(', ') }),
        this.i18n(`action.${bulkActionValue}.header`, { count: savedEntities.length, records: this.getManager().getNiceLabels(savedEntities).join(', ') })
      ).then(() => {
        this.context.store.dispatch(this.getManager().deleteEntities(savedEntities, this.getUiKey(), (entity, error) => {
          if (entity && error) {
            if (error.statusCode !== 202) {
              this.addErrorMessage({ title: this.i18n(`action.delete.error`, { record: this.getManager().getNiceLabel(entity) }) }, error);
            } else {
              this.addError(error);
            }
          } else {
            this.refs.table.getWrappedInstance().reload();
          }
        }));
      }, () => {
        // nothing
      });
    } else {
      this.addMessage({
        title: this.i18n('removeFailture'),
        level: 'warning'
      });
    }
    //
  }

  render() {
    const { entityId } = this.props.params;
    const forceSearchParameters = new Domain.SearchParameters().setFilter('systemId', entityId).setFilter('includeGlobalConfig', true);
    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <Basic.Confirm ref="confirm-delete" level="danger"/>

        <Basic.ContentHeader style={{ marginBottom: 0 }}>
          <span dangerouslySetInnerHTML={{ __html: this.i18n('header') }}/>
        </Basic.ContentHeader>

        <Basic.Panel className="no-border last">
          <Advanced.Table
            ref="table"
            uiKey={uiKey}
            manager={this.getManager()}
            forceSearchParameters={forceSearchParameters}
            showRowSelection={Managers.SecurityManager.hasAnyAuthority(['SYSTEM_UPDATE'])}
            actions={
              Managers.SecurityManager.hasAnyAuthority(['SYSTEM_UPDATE'])
              ?
              [{ value: 'delete', niceLabel: this.i18n('action.delete.action'), action: this.onDelete.bind(this), disabled: false }]
              :
              null
            }
            buttons={
              [
                <Basic.Button
                  level="success"
                  key="add_button"
                  className="btn-xs"
                  onClick={this.showDetail.bind(this, { }, true)}
                  rendered={Managers.SecurityManager.hasAnyAuthority(['SYSTEM_UPDATE'])}>
                  <Basic.Icon type="fa" icon="plus"/>
                  {' '}
                  {this.i18n('button.add')}
                </Basic.Button>
              ]
            }>
            <Advanced.Column
              property=""
              header=""
              className="detail-button"
              cell={
                ({ rowIndex, data }) => {
                  if (!data[rowIndex].globalConfiguration) {
                    return (
                      <Advanced.DetailButton
                        title={this.i18n('button.detail')}
                        onClick={this.showDetail.bind(this, data[rowIndex], false)}/>
                    );
                  }
                }
              }/>
            <Advanced.Column
              property="operationType"
              face="enum"
              enumClass={ProvisioningOperationTypeEnum}
              header={this.i18n('acc:entity.ProvisioningBreakConfig.operationType.label')}
              sort
              cell={
                ({ rowIndex, data }) => {
                  if (data[rowIndex].globalConfiguration) {
                    return (
                      <div>
                        <Basic.Label
                          level={ProvisioningOperationTypeEnum.getLevel(data[rowIndex].operationType)}
                          text={ProvisioningOperationTypeEnum.getNiceLabel(data[rowIndex].operationType)}/>
                        {' '}
                        <Basic.Label text={this.i18n('acc:entity.ProvisioningBreakConfig.globalConfiguration')} />
                      </div>
                    );
                  }
                  return (
                    <Basic.Label
                      level={ProvisioningOperationTypeEnum.getLevel(data[rowIndex].operationType)}
                      text={ProvisioningOperationTypeEnum.getNiceLabel(data[rowIndex].operationType)}/>
                  );
                }
              }/>
            <Advanced.Column
              property="actualOperationCount"
              face="text"
              header={this.i18n('acc:entity.ProvisioningBreakConfig.actualOperationCount.label')}
              sort/>
            <Advanced.Column
              property="period"
              face="text"
              header={this.i18n('acc:entity.ProvisioningBreakConfig.period.label')}
              sort/>
            <Advanced.Column
              property="disableLimit"
              face="text"
              header={this.i18n('acc:entity.ProvisioningBreakConfig.disableLimit.label')}
              sort/>
            <Advanced.Column
              property="warningLimit"
              face="text"
              header={this.i18n('acc:entity.ProvisioningBreakConfig.warningLimit.label')}
              sort/>
          </Advanced.Table>
        </Basic.Panel>
      </div>
    );
  }
}

SystemProvisioningBreakConfigs.propTypes = {
};

SystemProvisioningBreakConfigs.defaultProps = {
};
