import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import { Basic, Advanced, Domain, Managers, Utils } from 'czechidm-core';
import { SystemEntityHandlingManager, SystemManager } from '../../redux';
import uuid from 'uuid';
import SystemEntityTypeEnum from '../../domain/SystemEntityTypeEnum';
import SystemOperationTypeEnum from '../../domain/SystemOperationTypeEnum';

const uiKey = 'system-entities-handling-table';
const manager = new SystemEntityHandlingManager();
const systemManager = new SystemManager();

class SystemEntitiesHandling extends Basic.AbstractTableContent {

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
    return 'acc:content.system.systemEntitiesHandling';
  }

  componentDidMount() {
    this.selectNavigationItems(['sys-systems', 'system-entities-handling']);
  }

  showDetail(entity, add) {
    if (add) {
      // When we add new object class, then we need id of system as parametr and use "new" url
      const uuidId = uuid.v1();
      const system = entity._embedded && entity._embedded.system ? entity._embedded.system.id : this.props.params.entityId;
      this.context.router.push(`/system-entities-handling/${uuidId}/new?new=1&systemId=${system}`);
    } else {
      this.context.router.push(`/system-entities-handling/${entity.id}/detail`);
    }
  }

  save(entity, event) {
    const formEntity = this.refs.form.getData();
    formEntity.system = systemManager.getSelfLink(formEntity.system);
    //
    super.save(formEntity, event);
  }

  afterSave(entity, error) {
    if (!error) {
      this.addMessage({ message: this.i18n('save.success', { name: entity.entityType }) });
    }
    super.afterSave();
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
            showRowSelection={Managers.SecurityManager.hasAnyAuthority(['SYSTEM_WRITE'])}
            actions={
              Managers.SecurityManager.hasAnyAuthority(['SYSTEM_WRITE'])
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
                  rendered={Managers.SecurityManager.hasAnyAuthority(['ROLE_WRITE'])}>
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
              property="entityType"
              face="enum"
              enumClass={SystemEntityTypeEnum}
              header={this.i18n('acc:entity.SystemEntityHandling.entityType')}
              sort/>
            <Advanced.Column
              property="operationType"
              face="enum"
              enumClass={SystemOperationTypeEnum}
              header={this.i18n('acc:entity.SystemEntityHandling.operationType')}
              sort/>
          </Advanced.Table>
        </Basic.Panel>
      </div>
    );
  }
}

SystemEntitiesHandling.propTypes = {
  system: PropTypes.object,
  _showLoading: PropTypes.bool,
};
SystemEntitiesHandling.defaultProps = {
  system: null,
  _showLoading: false,
};

function select(state, component) {
  return {
    system: Utils.Entity.getEntity(state, systemManager.getEntityType(), component.params.entityId),
    _showLoading: Utils.Ui.isShowLoading(state, `${uiKey}-detail`),
  };
}

export default connect(select)(SystemEntitiesHandling);
