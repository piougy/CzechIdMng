import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import { Basic, Domain, Managers, Utils, Advanced } from 'czechidm-core';
import { SchemaObjectClassManager, SystemManager, SchemaAttributeManager } from '../../redux';

const uiKey = 'schema-object-classe';
const uiKeyAttributes = 'schema-attributes';
const manager = new SchemaObjectClassManager();
const systemManager = new SystemManager();
const schemaAttributeManager = new SchemaAttributeManager();

class SchemaObjectClass extends Basic.AbstractTableContent {

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
    return 'acc:content.schema.detail';
  }

  componentDidMount() {
    const { entityId} = this.props.params;
    this.context.store.dispatch(this.getManager().fetchEntity(entityId));
    this.selectNavigationItems(['sys-systems', 'system-object-classes']);
  }

  save(entity, event) {
    const formEntity = this.refs.form.getData();
    formEntity.system = systemManager.getSelfLink(formEntity.system);
    //
    super.save(formEntity, event);
  }

  afterSave(entity, error) {
    if (!error) {
      this.addMessage({ message: this.i18n('save.success', { name: entity.objectClassName }) });
    }
    super.afterSave();
  }

  closeDetail() {
    this.refs.form.processEnded();
  }

  render() {
    const { _showLoading, _schemaObjectClass} = this.props;
    const forceSearchParameters = new Domain.SearchParameters().setFilter('systemId', _schemaObjectClass ? _schemaObjectClass.system : '-1');
    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <Basic.Confirm ref="confirm-delete" level="danger"/>

        <Basic.ContentHeader>
          <Basic.Icon value="compressed"/>
          {' '}
          <span dangerouslySetInnerHTML={{ __html: this.i18n('objectClassHeader') }}/>
        </Basic.ContentHeader>

        <Basic.Panel>
          <Basic.AbstractForm ref="form" data={_schemaObjectClass} showLoading={_showLoading} className="form-horizontal">
            <Basic.SelectBox
              ref="system"
              manager={systemManager}
              label={this.i18n('acc:entity.SchemaObjectClass.system')}
              readOnly
              required/>
            <Basic.TextField
              ref="objectClassName"
              label={this.i18n('acc:entity.SchemaObjectClass.objectClassName')}
              required
              max={255}/>
            <Basic.Checkbox
              ref="container"
              label={this.i18n('acc:entity.SchemaObjectClass.container')}/>
            <Basic.Checkbox
              ref="auxiliary"
              label={this.i18n('acc:entity.SchemaObjectClass.auxiliary')}/>
          </Basic.AbstractForm>
          <Basic.PanelFooter>
            <Basic.Button type="button" level="link"
              onClick={this.context.router.goBack}
              showLoading={_showLoading}>
              {this.i18n('button.back')}
            </Basic.Button>
            <Basic.Button
              onClick={this.save.bind(this)}
              level="success" showLoading={_showLoading}>
              {this.i18n('button.save')}
            </Basic.Button>
          </Basic.PanelFooter>
        </Basic.Panel>
        <Basic.ContentHeader>
          <Basic.Icon value="list"/>
          {' '}
          <span dangerouslySetInnerHTML={{ __html: this.i18n('schemaAttributesHeader') }}/>
        </Basic.ContentHeader>
        <Basic.Panel>
          <Advanced.Table
            ref="table"
            uiKey={uiKeyAttributes}
            manager={schemaAttributeManager}
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
            }
            filter={
              <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
                <Basic.AbstractForm ref="filterForm" className="form-horizontal">
                  <Basic.Row className="last">
                    <div className="col-lg-6">
                      <Advanced.Filter.TextField
                        ref="name"
                        label={this.i18n('filter.name.label')}
                        placeholder={this.i18n('filter.name.placeholder')}/>
                    </div>
                    <div className="col-lg-2"/>
                    <div className="col-lg-4 text-right">
                      <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
                    </div>
                  </Basic.Row>
                </Basic.AbstractForm>
              </Advanced.Filter>
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
                      onClick={this.showDetail.bind(this, data[rowIndex])}/>
                  );
                }
              }/>
            <Advanced.ColumnLink
              to="schema-attributes/:id/detail"
              property="name"
              header={this.i18n('acc:entity.SchemaAttribute.name')}
              sort />
            <Advanced.Column property="classType" header={this.i18n('acc:entity.SchemaAttribute.classType')} sort/>
            <Advanced.Column property="required" face="boolean" header={this.i18n('acc:entity.SchemaAttribute.required')} sort/>
            <Advanced.Column property="multivalued" face="boolean" header={this.i18n('acc:entity.SchemaAttribute.multivalued')} sort/>
          </Advanced.Table>
        </Basic.Panel>
      </div>
    );
  }
}

SchemaObjectClass.propTypes = {
  system: PropTypes.object,
  _showLoading: PropTypes.bool,
};
SchemaObjectClass.defaultProps = {
  system: null,
  _showLoading: false,
};

function select(state, component) {
  const entity = Utils.Entity.getEntity(state, manager.getEntityType(), component.params.entityId);
  if (entity) {
    const system = entity._embedded && entity._embedded.system ? entity._embedded.system.id : null;
    entity.system = system;
  }
  return {
    _schemaObjectClass: entity,
    _showLoading: Utils.Ui.isShowLoading(state, `${uiKey}-detail`),
  };
}

export default connect(select)(SchemaObjectClass);
