import React from 'react';
import Helmet from 'react-helmet';
//
import { Basic, Advanced, Domain, Managers } from 'czechidm-core';
import { ProvisioningBreakConfigManager } from '../../redux';
import uuid from 'uuid';
import ProvisioningOperationTypeEnum from '../../domain/ProvisioningOperationTypeEnum';

const uiKey = 'provisioning-break-config-table';
const manager = new ProvisioningBreakConfigManager();

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

  render() {
    const { entityId } = this.props.params;
    const forceSearchParameters = new Domain.SearchParameters().setFilter('systemId', entityId);
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
                  return (
                    <Advanced.DetailButton
                      title={this.i18n('button.detail')}
                      onClick={this.showDetail.bind(this, data[rowIndex], false)}/>
                  );
                }
              }/>
            <Advanced.Column
              property="operationType"
              width="100px"
              face="enum"
              enumClass={ProvisioningOperationTypeEnum}
              header={this.i18n('acc:entity.ProvisioningBreakConfig.operationType.label')}
              sort/>
            <Advanced.Column
              property="period"
              face="text"
              header={this.i18n('acc:entity.ProvisioningBreakConfig.period.label')}
              sort/>
            <Advanced.Column
              property="_embedded.emailTemplateWarning.code"
              face="text"
              header={this.i18n('acc:entity.ProvisioningBreakConfig.emailTemplateWarning.label')}
              sort/>
            <Advanced.Column
              property="_embedded.emailTemplateDisabled.code"
              face="text"
              header={this.i18n('acc:entity.ProvisioningBreakConfig.emailTemplateDisabled.label')}
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
