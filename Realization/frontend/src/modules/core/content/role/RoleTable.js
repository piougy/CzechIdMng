import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import _ from 'lodash';
//
import * as Basic from '../../../../components/basic';
import * as Advanced from '../../../../components/advanced';
import * as Utils from '../../utils';
import RoleTypeEnum from '../../enums/RoleTypeEnum';
//
import authorityHelp from './AuthoritiesPanel_cs.md';
import AuthoritiesPanel from './AuthoritiesPanel';
import {WorkflowProcessDefinitionManager} from '../../redux';

const workflowProcessDefinitionManager = new WorkflowProcessDefinitionManager();
/**
* Table of roles
*/
export class RoleTable extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      filterOpened: this.props.filterOpened,
      detail: {
        show: false,
        entity: {}
      }
    };
  }

  getContentKey() {
    return 'content.roles';
  }

  componentDidMount() {
  }

  componentDidUpdate() {
  }

  useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().useFilterForm(this.refs.filterForm);
  }

  cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().cancelFilter(this.refs.filterForm);
  }

  showDetail(entity) {
    const { roleManager, uiKey } = this.props;
    const { detail } = this.state;
    //
    this.getLogger().debug(`[RoleTable] load entity detail [id:${entity.name}]`);
    this.setState({
      detail: {
        ...detail,
        showLoading: true,
        show: true
      }
    });
    //
    if (Utils.Entity.isNew(entity)) {
      this._setSelectedEntity(entity);
    } else {
      this.context.store.dispatch(roleManager.fetchEntity(entity.name, `${uiKey}-${entity.name}`, (loadedEntity, error) => {
        if (error) {
          this.addError(error);
        } else {
          // transform subroles to array of identifiers
          loadedEntity.subRoles = loadedEntity.subRoles.map(subRole => {
            return subRole._embedded.sub.id;
          });
          // transform superiorRoles
          loadedEntity.superiorRoles = loadedEntity.superiorRoles.map(superiorRole => {
            return superiorRole._embedded.superior.id;
          });
          this._setSelectedEntity(loadedEntity);
        }
      }));
    }
  }

  _setSelectedEntity(entity) {
    this.getLogger().debug(`[RoleTable] loaded entity detail [id:${entity.name}]`, entity);
    this.setState({
      detail: {
        show: true,
        showLoading: false,
        entity
      }
    }, () => {
      this.refs.form.setData(entity);
      this.refs.name.focus();
    });
  }

  closeDetail() {
    this.setState({
      detail: {
        show: false,
        entity: {}
      }
    });
  }

  save(event) {
    const { roleManager, uiKey } = this.props;
    //
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }
    const entity = this.refs.form.getData();
    // append selected authorities
    entity.authorities = this.refs.authorities.getWrappedInstance().getSelectedAuthorities();
    // append subroles
    if (entity.subRoles) {
      entity.subRoles = entity.subRoles.map(subRoleId => {
        return {
          sub: roleManager.getSelfLink(subRoleId)
        };
      });
    }
    // delete superior roles - we dont want to save them (they are ignored on BE anyway)
    delete entity.superiorRoles;
    //
    this.getLogger().debug('[RoleTable] save entity', entity);
    //
    if (entity.id === undefined) {
      this.context.store.dispatch(roleManager.createEntity(entity, `${uiKey}-detail`, (createdEntity, error) => {
        this._afterSave(createdEntity, error);
        if (!error) {
          this.refs.table.getWrappedInstance().reload();
        }
      }));
    } else {
      this.context.store.dispatch(roleManager.patchEntity(entity, `${uiKey}-detail`, this._afterSave.bind(this)));
    }
  }

  _afterSave(entity, error) {
    if (error) {
      this.refs.form.processEnded();
      this.addError(error);
      return;
    }
    this.addMessage({ message: this.i18n('save.success', { name: entity.name }) });
    this.closeDetail();
  }

  onDelete(bulkActionValue, selectedRows) {
    const { roleManager, uiKey } = this.props;
    const selectedEntities = roleManager.getEntitiesByIds(this.context.store.getState(), selectedRows);
    //
    this.refs['confirm-' + bulkActionValue].show(
      this.i18n(`action.${bulkActionValue}.message`, { count: selectedEntities.length, record: roleManager.getNiceLabel(selectedEntities[0]), records: roleManager.getNiceLabels(selectedEntities).join(', ') }),
      this.i18n(`action.${bulkActionValue}.header`, { count: selectedEntities.length, records: roleManager.getNiceLabels(selectedEntities).join(', ') })
    ).then(() => {
      this.context.store.dispatch(roleManager.deleteEntities(selectedEntities, uiKey, () => {
        this.refs.table.getWrappedInstance().reload();
      }));
    }, () => {
      // nothing
    });
  }

  render() {
    const { uiKey, roleManager, columns, _showLoading } = this.props;
    const { filterOpened, detail } = this.state;

    return (
      <div>
        <Basic.Confirm ref="confirm-delete" level="danger"/>

        <Advanced.Table
          ref="table"
          uiKey={uiKey}
          manager={roleManager}
          rowClass={({rowIndex, data}) => { return Utils.Ui.getRowClass(data[rowIndex]); }}
          filterOpened={filterOpened}
          showRowSelection
          filter={
            <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
              <Basic.AbstractForm ref="filterForm" className="form-horizontal">
                <Basic.Row className="last">
                  <div className="col-lg-4">
                    <Advanced.Filter.TextField
                      ref="text"
                      placeholder={this.i18n('entity.Role.name')}
                      label={this.i18n('entity.Role.name')}/>
                  </div>
                  <div className="col-lg-4">
                    <Basic.EnumSelectBox
                      ref="roleType"
                      label={this.i18n('entity.Role.roleType')}
                      enum={RoleTypeEnum}/>
                  </div>
                  <div className="col-lg-4 text-right">
                    <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
                  </div>
                </Basic.Row>
              </Basic.AbstractForm>
            </Advanced.Filter>
          }
          actions={
            [
              { value: 'delete', niceLabel: this.i18n('action.delete.action'), action: this.onDelete.bind(this), disabled: false }
            ]
          }
          buttons={
            [
              <Basic.Button level="success" key="add_button" className="btn-xs" onClick={this.showDetail.bind(this, { roleType: RoleTypeEnum.findKeyBySymbol(RoleTypeEnum.TECHNICAL) })}>
                <Basic.Icon type="fa" icon="plus"/>
                {' '}
                {this.i18n('button.add')}
              </Basic.Button>
            ]
          }
          >

          <Advanced.Column
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
            }
            sort={false}/>
          <Advanced.Column property="name" sort face="text" rendered={_.includes(columns, 'name')}/>
          <Advanced.Column property="roleType" sort face="enum" enumClass={RoleTypeEnum} rendered={_.includes(columns, 'roleType')}/>
          <Advanced.Column
            header={this.i18n('entity.Role.approvable')}
            className="detail-button"
            cell={
              ({ rowIndex, data }) => {
                return (
                  <input type="checkbox" disabled checked={data[rowIndex].approveAddWorkflow || data[rowIndex].approveRemoveWorkflow} />
                );
              }
            }
            sort={false}/>
          <Advanced.Column property="disabled" sort face="bool" rendered={_.includes(columns, 'disabled')}/>
        </Advanced.Table>

        <Basic.Modal
          bsSize="large"
          show={detail.show}
          showLoading={detail.showLoading}
          onHide={this.closeDetail.bind(this)}
          backdrop="static"
          keyboard={!_showLoading}>

          <form onSubmit={this.save.bind(this)}>
            <Basic.Modal.Header closeButton={!_showLoading} text={this.i18n('create.header')} rendered={ Utils.Entity.isNew(detail.entity) }/>
            <Basic.Modal.Header closeButton={!_showLoading} text={this.i18n('edit.header', { name: detail.entity.name })} rendered={detail.entity.id !== undefined }/>
            <Basic.Modal.Body>
              <Basic.Loading showLoading={_showLoading}>
                <Basic.AbstractForm ref="form">
                  <Basic.Row>
                    <div className="col-lg-8">
                      <h3 style={{ margin: '0 0 10px 0', padding: 0, borderBottom: '1px solid #ddd' }}>{this.i18n('setting.basic.header')}</h3>
                      <div className="form-horizontal">
                        <Basic.TextField
                          ref="name"
                          label={this.i18n('entity.Role.name')}
                          required/>
                        <Basic.EnumSelectBox
                          ref="roleType"
                          label={this.i18n('entity.Role.roleType')}
                          enum={RoleTypeEnum}
                          required
                          readOnly={!Utils.Entity.isNew(detail.entity)}/>
                        <Basic.SelectBox
                          ref="superiorRoles"
                          label={this.i18n('entity.Role.superiorRoles')}
                          manager={roleManager}
                          multiSelect
                          readOnly
                          placeholder=""/>
                        <Basic.SelectBox
                          ref="subRoles"
                          label={this.i18n('entity.Role.subRoles')}
                          manager={roleManager}
                          multiSelect/>
                        <Basic.TextArea
                          ref="description"
                          label={this.i18n('entity.Role.description')}/>
                        <Basic.Checkbox
                          ref="disabled"
                          label={this.i18n('entity.Role.disabled')}/>
                      </div>
                    </div>
                    <div className="col-lg-4">
                      <h3 style={{ margin: '0 0 10px 0', padding: 0, borderBottom: '1px solid #ddd' }}>
                        <span dangerouslySetInnerHTML={{ __html: this.i18n('setting.authority.header') }} className="pull-left"/>
                        <Basic.HelpIcon content={authorityHelp} className="pull-right"/>
                        <div className="clearfix"/>
                      </h3>
                      <AuthoritiesPanel
                        ref="authorities"
                        roleManager={roleManager}
                        authorities={detail.entity.authorities}/>

                      <h3 style={{ margin: '20px 0 10px 0', padding: 0, borderBottom: '1px solid #ddd' }}>
                        { this.i18n('setting.approval.header') }
                      </h3>
                      <Basic.SelectBox
                        labelSpan=""
                        componentSpan=""
                        ref="approveAddWorkflow"
                        label={this.i18n('entity.Role.approveAddWorkflow')}
                        forceSearchParameters={ workflowProcessDefinitionManager.getDefaultSearchParameters().setFilter('category', 'eu.bcvsolutions.role.approve.add') }
                        multiSelect={false}
                        manager={workflowProcessDefinitionManager}/>
                      <Basic.SelectBox
                        labelSpan=""
                        componentSpan=""
                        ref="approveRemoveWorkflow"
                        label={this.i18n('entity.Role.approveRemoveWorkflow')}
                        forceSearchParameters={ workflowProcessDefinitionManager.getDefaultSearchParameters().setFilter('category', 'eu.bcvsolutions.role.approve.remove') }
                        multiSelect={false}
                        manager={workflowProcessDefinitionManager}/>
                    </div>
                  </Basic.Row>
                </Basic.AbstractForm>
              </Basic.Loading>
            </Basic.Modal.Body>

            <Basic.Modal.Footer>
              <Basic.Button
                level="link"
                onClick={this.closeDetail.bind(this)}
                showLoading={_showLoading}>
                {this.i18n('button.close')}
              </Basic.Button>
              <Basic.Button
                type="submit"
                level="success"
                showLoading={_showLoading}
                showLoadingIcon
                showLoadingText={this.i18n('button.saving')}>
                {this.i18n('button.save')}
              </Basic.Button>
            </Basic.Modal.Footer>
          </form>

        </Basic.Modal>
      </div>
    );
  }
}

RoleTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  roleManager: PropTypes.object.isRequired,
  columns: PropTypes.arrayOf(PropTypes.string),
  filterOpened: PropTypes.bool
};

RoleTable.defaultProps = {
  columns: ['name', 'roleType', 'disabled', 'approvable'],
  filterOpened: false,
  _showLoading: false
};

function select(state, component) {
  return {
    _searchParameters: state.data.ui[component.uiKey] ? state.data.ui[component.uiKey].searchParameters : {},
    _showLoading: component.roleManager.isShowLoading(state, `${component.uiKey}-detail`)
  };
}

export default connect(select, null, null, { withRef: true })(RoleTable);
