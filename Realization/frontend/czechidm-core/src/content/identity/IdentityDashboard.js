import React from 'react';
import { connect } from 'react-redux';
import { Link } from 'react-router';
import _ from 'lodash';
import uuid from 'uuid';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import SearchParameters from '../../domain/SearchParameters';
import { IdentityManager, IdentityContractManager, WorkflowTaskInstanceManager, DataManager, SecurityManager } from '../../redux';
import OrganizationPosition from './OrganizationPosition';
import IdentityStateEnum from '../../enums/IdentityStateEnum';
import IdentityRoleTableComponent, { IdentityRoleTable } from './IdentityRoleTable';
import ContractStateEnum from '../../enums/ContractStateEnum';
import TaskInstanceTable from '../task/TaskInstanceTable';
import RunningTasks from '../scheduler/RunningTasks';

const identityManager = new IdentityManager();
const identityContractManager = new IdentityContractManager();
const workflowTaskInstanceManager = new WorkflowTaskInstanceManager();

/**
 * Identity dashboard - personalized dashboard with quick buttons and overview
 *
 * TODO:
 * - profile component (- depends on identity) - usage on dashboard (depends on system)
 * - implement all buttons
 * - registration for profile and dashboard
 *
 * @beta PoC - design only
 * @author Radek Tomiška
 * @since 9.4.0
 */
class IdentityDashboard extends Basic.AbstractContent {

  constructor(props) {
    super(props);
    this.state = {
    };
  }

  componentDidMount() {
    super.componentDidMount();
    //
    const identityIdentifier = this.getIdentityIdentifier();
    //
    this.context.store.dispatch(identityManager.fetchEntity(identityIdentifier, null, (entity, error) => {
      this.handleError(error);
    }));
    this.context.store.dispatch(identityManager.fetchAuthorities(identityIdentifier, `identity-authorities-${identityIdentifier}`, (entity, error) => {
      this.handleError(error);
    }));
    this.context.store.dispatch(identityManager.downloadProfileImage(identityIdentifier));
  }

  getContentKey() {
    return 'content.identity.dashboard';
  }

  getNavigationKey() {
    if (this.isDashboard()) {
      return 'dashboard';
    }
    return null;
  }

  isDashboard() {
    const identityIdentifier = this.getIdentityIdentifier();
    const { userContext } = this.props;
    // FIXME: dashboard parameter
    if (identityIdentifier === userContext.username || identityIdentifier === userContext.id) {
      return true;
    }
    return false;
  }

  getIdentityIdentifier() {
    const { entityId } = this.props.params;
    const { userContext } = this.props;
    //
    if (entityId) {
      return entityId;
    }
    if (userContext) {
      // TODO: username or id?
      return userContext.username;
    }
    return null;
  }

  onPasswordChange() {
    this.context.router.push(`/identity/${encodeURIComponent(this.getIdentityIdentifier())}/password`);
  }

  onChangePermissions() {
    const identity = identityManager.getEntity(this.context.store.getState(), this.getIdentityIdentifier());
    //
    const uuidId = uuid.v1();
    this.context.router.push(`/role-requests/${uuidId}/new?new=1&applicantId=${identity.id}`);
  }

  render() {
    const {
      identity,
      _imageUrl,
      _permissions,
      authorities
    } = this.props;
    const identityIdentifier = this.getIdentityIdentifier();
    const _authorities = authorities ? authorities.map(authority => authority.authority) : null;
    //
    // FIXME: showloading / 403 / 404
    if (!identity) {
      return (
        <Basic.Loading isStatic show/>
      );
    }
    //
    return (
      <div>
        <Basic.PageHeader>
          {
            _imageUrl
            ?
            <img src={ _imageUrl } className="img-circle img-thumbnail" style={{ height: 40, padding: 0 }} />
            :
            <Basic.Icon icon="user"/>
          }
          {' '}
          { identityManager.getNiceLabel(identity) } <small> { this.isDashboard() ? this.i18n('content.identity.dashboard.header') : this.i18n('navigation.menu.profile.label') }</small>
        </Basic.PageHeader>

        <OrganizationPosition identity={ identityIdentifier } showLink={ false }/>


        <Basic.Panel>
          <Basic.PanelBody>
            <Basic.Row>
              <Basic.Col lg={ 4 }>
                <Basic.Panel style={{ marginBottom: 0 }}>
                  <Basic.PanelHeader
                    text={ this.i18n('content.identity.profile.header') }
                    buttons={[
                      <Link to={ `/identity/${encodeURIComponent(identityIdentifier)}/profile` }>
                        <Basic.Icon value="fa:angle-double-right"/>
                        {' '}
                        { this.i18n('button.edit') }
                      </Link>
                    ]}/>

                  <div className="basic-table">
                    <table className="table table-condensed">
                      <tbody>
                        <tr>
                          <td style={{ borderTop: 'none', width: 150 }}>{ this.i18n('entity.Identity.username')} </td>
                          <th style={{ borderTop: 'none' }}>{ identity.username }</th>
                        </tr>
                        <tr>
                          <td>{ this.i18n('entity.Identity.fullName')} </td>
                          <th>{ identityManager.getNiceLabel(identity) }</th>
                        </tr>
                        <tr>
                          <td>{ this.i18n('entity.Identity.state.label')} </td>
                          <th><Basic.EnumValue value={ identity.state } enum={ IdentityStateEnum} /></th>
                        </tr>
                        <tr>
                          <td>{ this.i18n('entity.Identity.externalCode')} </td>
                          <th>{ identity.externalCode }</th>
                        </tr>
                        <tr>
                          <td>{ this.i18n('entity.Identity.email')} </td>
                          <th>{ identity.email }</th>
                        </tr>
                        <tr>
                          <td>{ this.i18n('entity.Identity.phone')} </td>
                          <th>{ identity.phone }</th>
                        </tr>
                        <tr>
                          <td>{ this.i18n('entity.Identity.description')} </td>
                          <td>{ identity.description }</td>
                        </tr>
                      </tbody>
                    </table>
                  </div>

                  <Basic.ContentHeader
                    text="EAV1"
                    style={{ padding: '0 15px', marginBottom: 0, marginTop: 15 }}
                    buttons={[
                      <a href="#" onClick={ (e) => { e.preventDefault(); alert('not-implementerd'); return false; }}>
                        <Basic.Icon value="fa:angle-double-right"/>
                        {' '}
                        { this.i18n('button.edit') }
                      </a>
                    ]}/>
                  <div className="basic-table">
                    <table className="table table-condensed">
                      <tbody>
                        <tr>
                          <td style={{ borderTop: 'none', width: 150 }}>property</td>
                          <th style={{ borderTop: 'none' }}>value</th>
                        </tr>
                        <tr>
                          <td>property</td>
                          <th>value</th>
                        </tr>
                        <tr>
                          <td>property</td>
                          <th>value</th>
                        </tr>
                        <tr>
                          <td>property</td>
                          <th>value</th>
                        </tr>
                        <tr>
                          <td>property</td>
                          <th>value</th>
                        </tr>
                        <tr>
                          <td>property</td>
                          <th>value</th>
                        </tr>
                      </tbody>
                    </table>
                  </div>

                  <Basic.ContentHeader
                    text="EAV2"
                    style={{ padding: '0 15px', marginBottom: 0, marginTop: 15 }}
                    buttons={[
                      <a href="#" onClick={ (e) => { e.preventDefault(); alert('not-implementerd'); return false; }}>
                        <Basic.Icon value="fa:angle-double-right"/>
                        {' '}
                        { this.i18n('button.edit') }
                      </a>
                    ]}/>
                  <div className="basic-table">
                    <table className="table table-condensed">
                      <tbody>
                        <tr>
                          <td style={{ borderTop: 'none', width: 150 }}>property</td>
                          <th style={{ borderTop: 'none' }}>value</th>
                        </tr>
                        <tr>
                          <td>property</td>
                          <th>value</th>
                        </tr>
                        <tr>
                          <td>property</td>
                          <th>value</th>
                        </tr>
                        <tr>
                          <td>property</td>
                          <th>value</th>
                        </tr>
                        <tr>
                          <td>property</td>
                          <th>value</th>
                        </tr>
                        <tr>
                          <td>property</td>
                          <th>value</th>
                        </tr>
                        <tr>
                          <td>property</td>
                          <th>value</th>
                        </tr>
                        <tr>
                          <td>property</td>
                          <th>value</th>
                        </tr>
                        <tr>
                          <td>property</td>
                          <th>value</th>
                        </tr>
                      </tbody>
                    </table>
                  </div>

                  <Basic.PanelFooter>
                    <Link to={ `/identity/${encodeURIComponent(identityIdentifier)}/profile` }>
                      <Basic.Icon value="fa:angle-double-right"/>
                      {' '}
                      { this.i18n('component.advanced.IdentityInfo.link.detail.label') }
                    </Link>
                  </Basic.PanelFooter>
                </Basic.Panel>
              </Basic.Col>
              <Basic.Col lg={ 8 }>
                <Basic.Button
                  level="primary"
                  icon="ok"
                  className="btn-large"
                  text={ this.i18n('content.password.change.header') }
                  onClick={ this.onPasswordChange.bind(this) }
                  style={{ height: 50, marginRight: 3, minWidth: 150 }}/>
                <Basic.Button
                  level="warning"
                  icon="fa:key"
                  className="btn-large"
                  text={ this.i18n('content.identity.roles.changePermissions') }
                  onClick={ this.onChangePermissions.bind(this) }
                  style={{ height: 50, marginRight: 3, minWidth: 150 }}/>
                <Basic.Button
                  level="danger"
                  icon="fa:square-o"
                  className="btn-large"
                  style={{ height: 50, marginRight: 3, minWidth: 150 }}
                  onClick={ () => alert('not implemented') }
                  text="Disable identity"/>
                <Basic.Button
                  level="success"
                  icon="fa:plus"
                  className="btn-large"
                  onClick={ () => alert('not implemented') }
                  style={{ height: 50, marginRight: 3, minWidth: 150 }}
                  text="Create user"/>
                <br /><br />

                <Basic.ContentHeader
                  icon="fa:universal-access"
                  text={ this.i18n('content.identity.roles.directRoles.header') }
                  buttons={[
                    <Link to={ `/identity/${encodeURIComponent(identityIdentifier)}/roles` }>
                      <Basic.Icon value="fa:angle-double-right"/>
                      {' '}
                      { this.i18n('component.advanced.IdentityInfo.link.detail.label') }
                    </Link>
                  ]}/>
                <Basic.Panel>
                  <IdentityRoleTableComponent
                    uiKey={ `dashboard-${identityIdentifier}` }
                    forceSearchParameters={ new SearchParameters().setFilter('identityId', identityIdentifier).setFilter('directRole', true) }
                    showAddButton
                    params={ this.props.params }
                    columns={ _.difference(IdentityRoleTable.defaultProps.columns, ['directRole']) }
                    _permissions={ _permissions }/>
                </Basic.Panel>

                <Basic.ContentHeader
                  icon="fa:building"
                  text={ this.i18n('content.identity.identityContracts.header') }
                  buttons={[
                    <Link to={ `/identity/${encodeURIComponent(identityIdentifier)}/contracts` }>
                      <Basic.Icon value="fa:angle-double-right"/>
                      {' '}
                      { this.i18n('component.advanced.IdentityInfo.link.detail.label') }
                    </Link>
                  ]}/>
                <Basic.Panel>
                  {/* FIXME: active operations */}
                  {/* FIXME: contract table component */}
                  <Advanced.Table
                    ref="contract-table"
                    uiKey={ 'todo-identity-contracts-key' }
                    manager={ identityContractManager }
                    forceSearchParameters={ new SearchParameters().setFilter('identity', identityIdentifier) }
                    rowClass={({rowIndex, data}) => { return data[rowIndex].state ? 'disabled' : Utils.Ui.getRowClass(data[rowIndex]); }}>
                    <Advanced.Column
                      header={'H'}
                      title={ this.i18n('entity.IdentityContract.main.help') }
                      property="main"
                      face="bool"/>
                    <Advanced.Column
                      property="position"
                      header={this.i18n('entity.IdentityContract.position')}
                      width={ 200 }
                      sort/>
                    <Basic.Column
                      property="workPosition"
                      header={this.i18n('entity.IdentityContract.workPosition')}
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
                                null
                              }
                            </span>
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
                      property="state"
                      header={this.i18n('entity.IdentityContract.state.label')}
                      face="enum"
                      enumClass={ ContractStateEnum }
                      width={100}
                      sort/>
                    <Advanced.Column
                      property="externe"
                      header={this.i18n('entity.IdentityContract.externe')}
                      face="bool"
                      width={100}
                      sort/>
                  </Advanced.Table>
                </Basic.Panel>

                <Basic.Alert level="info" className="no-margin">
                  Account table (TODO: registration - accmodule)
                </Basic.Alert>
              </Basic.Col>
            </Basic.Row>

            <Basic.ContentHeader
              icon="tasks"
              text={ this.i18n('content.tasks-assigned.assigned') }
              rendered={ SecurityManager.hasAuthority('WORKFLOWTASK_READ', { authorities: _authorities, isAuthenticated: true }) }/>
            <Basic.Panel rendered={ SecurityManager.hasAuthority('WORKFLOWTASK_READ', { authorities: _authorities, isAuthenticated: true }) }>
              {/* TODO: default workflowTaskInstanceManager inside of component */}
              <TaskInstanceTable uiKey="task_instance_dashboard_table" taskInstanceManager={ workflowTaskInstanceManager } filterOpened={false}/>
            </Basic.Panel>

            <Basic.ContentHeader
              icon="fa:calendar-times-o"
              text={ this.i18n('dashboard.longRunningTaskDashboard.header') }
              rendered={ SecurityManager.hasAuthority('SCHEDULER_READ', { authorities: _authorities, isAuthenticated: true }) } />
            <Basic.Panel rendered={ SecurityManager.hasAuthority('SCHEDULER_READ', { authorities: _authorities, isAuthenticated: true }) }>
              <RunningTasks creatorId={ identity.id } />
            </Basic.Panel>
          </Basic.PanelBody>
        </Basic.Panel>
        {
          !this.isDashboard()
          ||
          <div>
            <Basic.Alert level="info">
              Super Admin: { SecurityManager.hasAuthority('APP_ADMIN', { authorities: _authorities, isAuthenticated: true }) ? 'yes' : 'no' } (monitoring, event queue)
              <br />
              Loaded authorities: { _authorities ? _authorities.join(', ') : '[N/A]' }
            </Basic.Alert>
            <Basic.Alert level="info">
              System admin: { SecurityManager.hasAuthority('SYSTEM_ADMIN', { authorities: _authorities, isAuthenticated: true }) ? 'yes' : 'no' } (provisioning queue)
            </Basic.Alert>
            <Basic.Alert level="info">
              System VS: { SecurityManager.hasAuthority('VSREQUEST_READ', { authorities: _authorities, isAuthenticated: true }) ? 'yes' : 'no' } (vs tasks)
            </Basic.Alert>
          </div>
        }
      </div>
    );
  }
}

IdentityDashboard.propTypes = {
};

IdentityDashboard.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.params;
  const profileUiKey = identityManager.resolveProfileUiKey(entityId);
  const profile = DataManager.getData(state, profileUiKey);
  //
  return {
    userContext: state.security.userContext,
    identity: identityManager.getEntity(state, entityId),
    authorities: DataManager.getData(state, `identity-authorities-${entityId}`),
    _imageUrl: profile ? profile.imageUrl : null,
    _permissions: identityManager.getPermissions(state, null, entityId)
  };
}

export default connect(select)(IdentityDashboard);
