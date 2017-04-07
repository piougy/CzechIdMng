import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import _ from 'lodash';
import uuid from 'uuid';
import { Link } from 'react-router';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import SearchParameters from '../../domain/SearchParameters';
import { IdentityRoleManager, IdentityContractManager, IdentityManager, RoleManager, RoleTreeNodeManager, WorkflowProcessInstanceManager, DataManager, SecurityManager, RoleRequestManager } from '../../redux';
import RoleRequestTable from '../requestrole/RoleRequestTable';

const uiKey = 'identity-roles';
const uiKeyContracts = 'identity-contracts';
const uiKeyAuthorities = 'identity-roles';
const roleManager = new RoleManager();
const roleTreeNodeManager = new RoleTreeNodeManager();
const identityRoleManager = new IdentityRoleManager();
const identityManager = new IdentityManager();
const identityContractManager = new IdentityContractManager();
const workflowProcessInstanceManager = new WorkflowProcessInstanceManager();
const roleRequestManager = new RoleRequestManager();

const TEST_ADD_ROLE_DIRECTLY = false;

class Roles extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      detail: {
        show: false,
        entity: {}
      }
    };
  }

  getContentKey() {
    return 'content.identity.roles';
  }

  componentDidMount() {
    this.selectSidebarItem('profile-roles');
    const { entityId } = this.props.params;
    this.context.store.dispatch(identityRoleManager.fetchRoles(entityId, `${uiKey}-${entityId}`));
    this.context.store.dispatch(identityManager.fetchAuthorities(entityId, `${uiKeyAuthorities}-${entityId}`));
    this.context.store.dispatch(identityContractManager.fetchContracts(entityId, `${uiKeyContracts}-${entityId}`));
  }

  componentWillReceiveProps(nextProps) {
    const { _addRoleProcessIds } = nextProps;
    if (_addRoleProcessIds && _addRoleProcessIds !== this.props._addRoleProcessIds) {
      for (const idProcess of _addRoleProcessIds) {
        const processEntity = workflowProcessInstanceManager.getEntity(this.context.store.getState(), idProcess);
        if (processEntity && processEntity.processVariables.conceptRole.role && !roleManager.isShowLoading(this.context.store.getState(), `role-${processEntity.processVariables.conceptRole.role}`)) {
          this.context.store.dispatch(roleManager.fetchEntityIfNeeded(processEntity.processVariables.conceptRole.role, `role-${processEntity.processVariables.conceptRole.role}`));
        }
      }
    }
  }

  showDetail(entity) {
    const entityFormData = _.merge({}, entity, {
      role: entity.id ? entity._embedded.role : null
    });

    this.setState({
      detail: {
        show: true,
        showLoading: false,
        entity: entityFormData
      }
    }, () => {
      this.refs.form.setData(entityFormData);
      this.refs.role.focus();
    });
  }

  closeDetail() {
    this.setState({
      detail: {
        ... this.state.detail,
        show: false
      }
    });
  }

  showProcessDetail(entity) {
    this.context.router.push('workflow/history/processes/' + entity.id);
  }

  /**
   * Compute background color row (added, removed, changed)
   */
  _rowClass({rowIndex, data}) {
    if (data[rowIndex].processVariables.operationType === 'add') {
      return 'bg-success';
    }
    if (data[rowIndex].processVariables.operationType === 'remove') {
      return 'bg-danger';
    }
    if (data[rowIndex].processVariables.operationType === 'change') {
      return 'bg-warning';
    }
    return null;
  }


  save(event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }
    const entity = this.refs.form.getData();
    const { entityId } = this.props.params;
    const role = roleManager.getEntity(this.context.store.getState(), entity.role);
    entity.identity = identityManager.getSelfLink(entityId);
    entity.role = role._links.self.href;
    //
    if (entity.id === undefined) {
      this.context.store.dispatch(identityRoleManager.createEntity(entity, `${uiKey}-${entityId}`, (savedEntity, error) => {
        if (!error) {
          this.addMessage({ message: this.i18n('create.success', { role: role.name, username: entityId }) });
          this._afterSave(error);
        } else if (error.statusCode === 202) {
          this.addMessage({ level: 'info', message: this.i18n('create.accepted', { role: role.name, username: entityId }) });
          this.refs.tableProcesses.getWrappedInstance().reload();
          this.closeDetail();
        } else {
          this._afterSave(error);
        }
      }));
    } else {
      this.context.store.dispatch(identityRoleManager.patchEntity(entity, `${uiKey}-${entityId}`, (savedEntity, error) => {
        this._afterSave(error);
        if (!error) {
          this.addMessage({ message: this.i18n('edit.success', { role: role.name, username: entityId }) });
        }
      }));
    }
  }

  _afterSave(error) {
    if (error) {
      this.refs.form.processEnded();
      this.addError(error);
      return;
    }
    const { entityId } = this.props.params;
    this.context.store.dispatch(identityManager.fetchAuthorities(entityId, `${uiKeyAuthorities}-${entityId}`));
    this.closeDetail();
  }

  _onDelete(entity, event) {
    if (event) {
      event.preventDefault();
    }
    const { entityId } = this.props.params;
    this.refs['confirm-delete'].show(
      this.i18n(`action.delete.message`, { count: 1, record: entity._embedded.role.name }),
      this.i18n(`action.delete.header`, { count: 1 })
    ).then(() => {
      this.context.store.dispatch(identityRoleManager.deleteEntity(entity, `${uiKey}-${entityId}`, (deletedEntity, error) => {
        if (!error) {
          this.addMessage({ message: this.i18n('delete.success', { role: deletedEntity._embedded.role.name, username: entityId }) });
          this.context.store.dispatch(identityManager.fetchAuthorities(entityId, `${uiKeyAuthorities}-${entityId}`));
        } else {
          this.addError(error);
        }
      }));
    }, () => {
      // Rejected
    });
  }

  _onDeleteAddRoleProcessInstance(entity, event) {
    if (event) {
      event.preventDefault();
    }
    this.refs['confirm-delete'].show(
      this.i18n('content.identity.roles.changeRoleProcesses.deleteConfirm', {'processId': entity.id}),
      this.i18n(`action.delete.header`, { count: 1 })
    ).then(() => {
      this.context.store.dispatch(workflowProcessInstanceManager.deleteEntity(entity, null, (deletedEntity, error) => {
        if (!error) {
          this.addMessage({ message: this.i18n('content.identity.roles.changeRoleProcesses.deleteSuccess', {'processId': entity.id})});
        } else {
          this.addError(error);
        }
        this.refs.tableProcesses.getWrappedInstance().reload();
        this.refs.tablePermissionProcesses.getWrappedInstance().reload();
      }));
    }, () => {
      // Rejected
    });
  }

  _roleNameCell({ rowIndex, data }) {
    const role = roleManager.getEntity(this.context.store.getState(), data[rowIndex].processVariables.conceptRole.role);
    if (role) {
      return role.name;
    }
    return null;
  }

  _changePermissions() {
    const { entityId } = this.props.params;

    this.setState({
      showLoading: true
    });
    const promise = identityManager.getService().getById(entityId);
    promise.then((json) => {
      this.setState({
        showLoading: false
      });
      const uuidId = uuid.v1();
      this.context.router.push({pathname: `/role-requests/${uuidId}/new?new=1&applicantId=${json.id}`, state: {adminMode: false}});
    }).catch(ex => {
      this.setState({
        showLoading: false
      });
      this.addError(ex);
    });
  }

  /**
   * TODO: move to manager
   *
   * @return {[type]} [description]
   */
  _canChangePermissions() {
    const { userContext } = this.props;
    const { entityId } = this.props.params;
    return (entityId === userContext.username) || SecurityManager.isAdmin(userContext);
  }

  /**
   * Redirects to tab with identity contracts
   *
   * @param  {string} identityId
   */
  showContracts(identityId) {
    this.context.router.push(`/identity/${identityId}/contracts`);
  }

  _getWfProcessCell({ rowIndex, data}) {
    const entity = data[rowIndex];
    if (!entity || !entity.id) {
      return '';
    }
    return (
      <Link to={`/workflow/history/processes/${entity.id}`}>{entity.id}</Link>
    );
  }

  render() {
    const { entityId } = this.props.params;
    const { _entities, _showLoading, _showLoadingContracts, _contracts } = this.props;
    const { detail } = this.state;
    let force = new SearchParameters();
    force = force.setFilter('identity', entityId);
    force = force.setFilter('category', 'eu.bcvsolutions.role.approve');
    let roleRequestsForceSearch = new SearchParameters();
    roleRequestsForceSearch = roleRequestsForceSearch.setFilter('applicant', entityId);
    roleRequestsForceSearch = roleRequestsForceSearch.setFilter('states', 'IN_PROGRESS, DUPLICATED, EXCEPTION');
    let conceptsForceSearch = new SearchParameters();
    conceptsForceSearch = conceptsForceSearch.setFilter('applicant', entityId);
    conceptsForceSearch = conceptsForceSearch.setFilter('state', 'CONCEPT');
    let hasRoleConcepts = true;
    const uiKeyConceptTable = `table-applicant-concepts-${entityId}`;

    if (this.context.store.getState().data.ui[uiKeyConceptTable]
      && !(this.context.store.getState().data.ui[uiKeyConceptTable].total > 0)) {
      hasRoleConcepts = false;
    }

    //
    // sort entities by role name
    // TODO: add sort by validFrom?
    const entities = _.slice(_entities).sort((a, b) => {
      return a._embedded.role.name > b._embedded.role.name;
    });
    //
    let content;
    if (_showLoadingContracts) {
      content = (
        <div>
          { this.renderContentHeader() }
          <Basic.Loading isStatic show/>
        </div>
      );
    } else if (_contracts.length === 0) {
      content = (
        <div>
          { this.renderContentHeader() }
          <Basic.Alert
            className="no-margin"
            text={this.i18n('contracts.empty.message')}
            buttons={[
              <Basic.Button
                level="info"
                rendered={ SecurityManager.hasAuthority('APP_ADMIN') }
                onClick={ this.showContracts.bind(this, entityId) }>
                {this.i18n('contracts.empty.button')}
              </Basic.Button>
            ]}/>
        </div>
      );
    } else {
      content = (
        <div>
          <Basic.Panel style={{ marginTop: 15 }}>
            <Basic.PanelHeader text={this.i18n('navigation.menu.roles.title')}/>
            {
              _showLoading
              ?
              <Basic.Loading showLoading className="static"/>
              :
              <div>
                <Basic.Toolbar>
                  <div className="pull-right">
                    <Basic.Button level="success" className="btn-xs" onClick={this.showDetail.bind(this, {})} rendered={TEST_ADD_ROLE_DIRECTLY}>
                      <Basic.Icon value="fa:plus"/>
                      {' '}
                      {this.i18n('button.add')}
                    </Basic.Button>
                    <Basic.Button
                      style={{display: 'block'}}
                      level="warning"
                      onClick={this._changePermissions.bind(this)}
                      rendered={this._canChangePermissions()}>
                      <Basic.Icon type="fa" icon="key"/>
                      {' '}
                      { this.i18n('changePermissions') }
                    </Basic.Button>
                  </div>
                  <div className="clearfix"></div>
                </Basic.Toolbar>
                <Basic.Table
                  data={entities}
                  showRowSelection={false}
                  className="vertical-scroll"
                  noData={this.i18n('component.basic.Table.noData')}
                  rowClass={({rowIndex, data}) => { return Utils.Ui.getRowClass(data[rowIndex]); }}>
                  <Basic.Column
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
                  <Basic.Column
                    header={this.i18n('entity.IdentityRole.identityContract.title')}
                    property="identityContract"
                    cell={
                      /* eslint-disable react/no-multi-comp */
                      ({rowIndex, data, property}) => {
                        return (
                          <span>{ identityContractManager.getNiceLabel(data[rowIndex][property], false) }</span>
                        );
                      }
                    }/>
                  <Basic.Column
                    header={this.i18n('entity.IdentityRole.role')}
                    property="_embedded.role.name"
                    />
                  <Basic.Column
                    property="validFrom"
                    header={this.i18n('label.validFrom')}
                    cell={<Basic.DateCell format={this.i18n('format.date')}/>}
                    />
                  <Basic.Column
                    property="validTill"
                    header={this.i18n('label.validTill')}
                    cell={<Basic.DateCell format={this.i18n('format.date')}/>}/>

                  <Basic.Column
                    header={<Basic.Cell className="column-face-bool">{this.i18n('entity.IdentityRole.roleTreeNode.label')}</Basic.Cell>}
                    cell={
                      /* eslint-disable react/no-multi-comp */
                      ({ rowIndex, data }) => {
                        return (
                          <Basic.BooleanCell propertyValue={ data[rowIndex].roleTreeNode !== null } className="column-face-bool"/>
                        );
                      }
                    }
                    width="150px"/>
                  <Basic.Column
                    header={ this.i18n('entity.id.label') }
                    property="id"
                    rendered={ this.isDevelopment() }
                    className="text-center"
                    cell={
                      ({rowIndex, data, property}) => {
                        return (
                          <Advanced.UuidInfo value={data[rowIndex][property]}/>
                        );
                      }
                    }/>
                </Basic.Table>
              </div>
            }
          </Basic.Panel>

          <Basic.Panel style={{display: hasRoleConcepts ? 'block' : 'none'}}>
            <Basic.PanelHeader text={this.i18n('conceptPermissionRequests.header')}/>
              <RoleRequestTable
                ref="conceptTable"
                uiKey={uiKeyConceptTable}
                showFilter={false}
                adminMode={false}
                showLoading={_showLoading}
                forceSearchParameters={conceptsForceSearch}
                columns={['state', 'created', 'modified', 'detail']}
                manager={roleRequestManager}/>
          </Basic.Panel>

          <Basic.Panel>
            <Basic.PanelHeader text={this.i18n('changePermissionRequests.header')}/>
              <RoleRequestTable
                ref="requestTable"
                uiKey={'table-applicant-requests'}
                showFilter={false}
                adminMode={false}
                showLoading={_showLoading}
                forceSearchParameters={roleRequestsForceSearch}
                columns={['state', 'created', 'modified', 'wf', 'detail']}
                manager={roleRequestManager}/>
          </Basic.Panel>

          <Basic.Panel>
            <Basic.PanelHeader text={this.i18n('changeRoleProcesses.header')}/>
            <Advanced.Table
              ref="tableProcesses"
              uiKey="table-processes"
              rowClass={this._rowClass}
              forceSearchParameters={force}
              manager={workflowProcessInstanceManager}
              pagination={false}>
              <Advanced.Column
                property="detail"
                cell={
                  ({ rowIndex, data }) => {
                    return (
                      <Advanced.DetailButton
                        title={this.i18n('button.detail')}
                        onClick={this.showProcessDetail.bind(this, data[rowIndex])}/>
                    );
                  }
                }
                header={' '}
                sort={false}
                face="text"/>
              <Advanced.Column
                property="currentActivityName"
                header={this.i18n('content.roles.processRoleChange.currentActivity')}
                sort={false}
                face="text"/>
              <Advanced.Column
                property="processVariables.conceptRole.role"
                cell={this._roleNameCell.bind(this)}
                header={this.i18n('content.roles.processRoleChange.roleName')}
                sort={false}
                face="text"/>
              <Advanced.Column
                property="processVariables.conceptRole.validFrom"
                header={this.i18n('content.roles.processRoleChange.roleValidFrom')}
                sort={false}
                face="date"/>
              <Advanced.Column
                property="processVariables.conceptRole.validTill"
                header={this.i18n('content.roles.processRoleChange.roleValidTill')}
                sort={false}
                face="date"/>
              <Advanced.Column
                property="id"
                cell={this._getWfProcessCell}
                header={this.i18n('label.id')}
                sort={false}
                face="text"/>
            </Advanced.Table>
          </Basic.Panel>
          <Basic.Modal
            bsSize="default"
            show={detail.show}
            onHide={this.closeDetail.bind(this)}
            backdrop="static"
            keyboard={!_showLoading}>

            <form onSubmit={this.save.bind(this)}>
              <Basic.Modal.Header closeButton={!_showLoading} text={this.i18n('create.header')} rendered={detail.entity.id === undefined}/>
              <Basic.Modal.Header closeButton={!_showLoading} text={this.i18n('edit.header', { role: roleManager.getNiceLabel(detail.entity.role) })} rendered={detail.entity.id !== undefined}/>
              <Basic.Modal.Body>
                <Basic.AbstractForm ref="form" showLoading={_showLoading} readOnly={!TEST_ADD_ROLE_DIRECTLY}>
                  <Basic.TextField
                    label={this.i18n('entity.IdentityRole.identityContract.label')}
                    helpBlock={this.i18n('entity.IdentityRole.identityContract.help')}
                    value={ identityContractManager.getNiceLabel(detail.entity.identityContract) }
                    readOnly={!TEST_ADD_ROLE_DIRECTLY}
                    required/>
                  <Basic.SelectBox
                    ref="role"
                    manager={roleManager}
                    label={this.i18n('entity.IdentityRole.role')}
                    required/>
                  <Basic.LabelWrapper
                    label={this.i18n('entity.IdentityRole.roleTreeNode.label')}
                    helpBlock={this.i18n('entity.IdentityRole.roleTreeNode.help')}>
                    { roleTreeNodeManager.getNiceLabel(detail.entity.roleTreeNode) }
                  </Basic.LabelWrapper>
                  <Basic.Row>
                    <div className="col-md-6">
                      <Basic.DateTimePicker
                        mode="date"
                        ref="validFrom"
                        label={this.i18n('label.validFrom')}/>
                    </div>
                    <div className="col-md-6">
                      <Basic.DateTimePicker
                        mode="date"
                        ref="validTill"
                        label={this.i18n('label.validTill')}/>
                    </div>
                  </Basic.Row>
                </Basic.AbstractForm>
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
                  showLoadingText={this.i18n('button.saving')}
                  rendered={TEST_ADD_ROLE_DIRECTLY}>
                  {this.i18n('button.save')}
                </Basic.Button>
              </Basic.Modal.Footer>
            </form>
          </Basic.Modal>
        </div>
      );
    }
    //
    return (
      <div>
        <Basic.Confirm ref="confirm-delete" level="danger"/>
        <Helmet title={this.i18n('title')} />

        { content }
      </div>
    );
  }
}

Roles.propTypes = {
  _showLoading: PropTypes.bool,
  _showLoadingContracts: PropTypes.bool,
  _entities: PropTypes.arrayOf(React.PropTypes.object),
  _contracts: PropTypes.arrayOf(React.PropTypes.object),
  authorities: PropTypes.arrayOf(React.PropTypes.object),
  userContext: PropTypes.object,
};
Roles.defaultProps = {
  _showLoading: true,
  _showLoadingContracts: true,
  _entities: [],
  _contracts: [],
  authorities: [],
  userContext: null
};

function select(state, component) {
  let addRoleProcessIds;
  if (state.data.ui['table-processes'] && state.data.ui['table-processes'].items) {
    addRoleProcessIds = state.data.ui['table-processes'].items;
  }

  return {
    _showLoading: identityRoleManager.isShowLoading(state, `${uiKey}-${component.params.entityId}`),
    _entities: identityRoleManager.getEntities(state, `${uiKey}-${component.params.entityId}`),
    _showLoadingContracts: identityContractManager.isShowLoading(state, `${uiKeyContracts}-${component.params.entityId}`),
    _contracts: identityContractManager.getEntities(state, `${uiKeyContracts}-${component.params.entityId}`),
    _addRoleProcessIds: addRoleProcessIds,
    authorities: DataManager.getData(state, `${uiKeyAuthorities}-${component.params.entityId}`),
    userContext: state.security.userContext
  };
}

export default connect(select)(Roles);
