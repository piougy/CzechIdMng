import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import { Link } from 'react-router';
import uuid from 'uuid';
//
import * as Basic from '../../components/basic';
import * as Utils from '../../utils';
import { IdentityManager, DataManager, ConfigurationManager } from '../../redux';
import ComponentService from '../../services/ComponentService';
import OrganizationPosition from './OrganizationPosition';
import IdentityStateEnum from '../../enums/IdentityStateEnum';

const identityManager = new IdentityManager();
const componentService = new ComponentService();

/**
 * Identity dashboard - personalized dashboard with quick buttons and overview
 *
 * TODO:
 * - implement all buttons + registrate buttons
 * - extract css styles
 * - dashboard component super class
 *
 * @author Radek Tomiška
 * @since 9.4.0
 */
class IdentityDashboard extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
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
    this.context.store.dispatch(identityManager.downloadProfileImage(identityIdentifier));
  }

  getContentKey() {
    return 'content.identity.dashboard';
  }

  getNavigationKey() {
    if (!this.isDashboard()) {
      return 'identities';
    }
    return null;
  }

  isDashboard() {
    return this.props.dashboard;
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

  onIdentityDetail() {
    this.context.router.push(`/identity/${encodeURIComponent(this.getIdentityIdentifier())}/profile`);
  }

  /**
   * Return true when currently logged user can change password
   *
   */
  _canPasswordChange() {
    const { passwordChangeType, _permissions } = this.props;
    //
    return identityManager.canChangePassword(passwordChangeType, _permissions);
  }

  onPasswordChange() {
    this.context.router.push(`/identity/${encodeURIComponent(this.getIdentityIdentifier())}/password`);
  }

  /**
   * Can change identity permission
   *
   * @return {[type]} [description]
   */
  _canChangePermissions() {
    const { _permissions } = this.props;
    //
    return Utils.Permission.hasPermission(_permissions, 'CHANGEPERMISSION');
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
      _permissions
    } = this.props;
    const identityIdentifier = this.getIdentityIdentifier();
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

        <div style={{ paddingBottom: 15 }}>
          <Basic.Button
            level="success"
            icon="fa:angle-double-right"
            className="btn-large"
            onClick={ this.onIdentityDetail.bind(this) }
            style={{ height: 50, marginRight: 3, minWidth: 150 }}
            text={ this.i18n('component.advanced.IdentityInfo.link.detail.label') }
            rendered={ identityManager.canRead(identity, _permissions) } />
          <Basic.Button
            level="primary"
            icon="ok"
            className="btn-large"
            text={ this.i18n('content.password.change.header') }
            onClick={ this.onPasswordChange.bind(this) }
            style={{ height: 50, marginRight: 3, minWidth: 150 }}
            rendered={ this._canPasswordChange() }/>
          <Basic.Button
            level="warning"
            icon="fa:key"
            className="btn-large"
            text={ this.i18n('content.identity.roles.changePermissions') }
            onClick={ this.onChangePermissions.bind(this) }
            style={{ height: 50, marginRight: 3, minWidth: 150 }}
            rendered={ this._canChangePermissions() }/>

          <Basic.Button
            level="danger"
            icon="fa:square-o"
            className="btn-large hidden"
            style={{ height: 50, marginRight: 3, minWidth: 150 }}
            onClick={ () => alert('not implemented') }
            text="Disable identity"/>
          <Basic.Button
            level="success"
            icon="fa:plus"
            className="btn-large hidden"
            onClick={ () => alert('not implemented') }
            style={{ height: 50, marginRight: 3, minWidth: 150 }}
            text="Create user"/>
          <Basic.Button
            level="info"
            icon="link"
            className="btn-large hidden"
            onClick={ () => alert('not implemented') }
            style={{ height: 50, marginRight: 3, minWidth: 150 }}
            text="Přepočet účtů a provisioning"/>
        </div>

        <Basic.Row>
          <Basic.Col lg={ 3 } rendered={ !this.isDashboard() }>
            <Basic.ContentHeader
              icon="user"
              text={ this.i18n('content.identity.profile.header') }
              buttons={
                identityManager.canSave(identity, _permissions)
                ?
                  [
                    <Link to={ `/identity/${encodeURIComponent(identityIdentifier)}/profile` }>
                      <Basic.Icon value="fa:angle-double-right"/>
                      {' '}
                      { this.i18n('button.edit') }
                    </Link>
                  ]
                :
                null
            }/>
            <Basic.Panel style={{ marginBottom: 0 }}>
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
                style={{ padding: '0 15px', marginBottom: 0, marginTop: 15, display: 'none' }}
                buttons={[
                  <a href="#" onClick={ (e) => { e.preventDefault(); alert('not-implementerd'); return false; }}>
                    <Basic.Icon value="fa:angle-double-right"/>
                    {' '}
                    { this.i18n('button.edit') }
                  </a>
                ]}/>
              <div className="basic-table hidden">
                <table className="table table-condensed">
                  <tbody>
                    <tr>
                      <td style={{ borderTop: 'none', width: 150 }}>property</td>
                      <th style={{ borderTop: 'none' }}>value</th>
                    </tr>
                  </tbody>
                </table>
              </div>

              <Basic.PanelFooter rendered={ identityManager.canRead(identity, _permissions) }>
                <Link to={ `/identity/${encodeURIComponent(identityIdentifier)}/profile` }>
                  <Basic.Icon value="fa:angle-double-right"/>
                  {' '}
                  { this.i18n('component.advanced.IdentityInfo.link.detail.label') }
                </Link>
              </Basic.PanelFooter>
            </Basic.Panel>
          </Basic.Col>
          <Basic.Col lg={ !this.isDashboard() ? 9 : 12 }>
            {
              componentService
                .getComponentDefinitions(ComponentService.IDENTITY_DASHBOARD_COMPONENT_TYPE)
                .filter(component => !this.isDashboard() || component.dashboard !== false)
                .map(component => {
                  const DashboardComponent = component.component;
                  return (
                    <DashboardComponent
                      key={`${ComponentService.IDENTITY_DASHBOARD_COMPONENT_TYPE}-${component.id}`}
                      entityId={ identity.username }
                      identity={ identity }
                      permissions={ _permissions }/>
                  );
                })
            }
          </Basic.Col>
        </Basic.Row>
      </div>
    );
  }
}

IdentityDashboard.propTypes = {
  dashboard: PropTypes.bool
};

IdentityDashboard.defaultProps = {
  dashboard: false
};

function select(state, component) {
  const { entityId } = component.params;
  const profileUiKey = identityManager.resolveProfileUiKey(entityId);
  const profile = DataManager.getData(state, profileUiKey);
  //
  return {
    userContext: state.security.userContext,
    identity: identityManager.getEntity(state, entityId),
    passwordChangeType: ConfigurationManager.getPublicValue(state, 'idm.pub.core.identity.passwordChange'),
    _imageUrl: profile ? profile.imageUrl : null,
    _permissions: identityManager.getPermissions(state, null, entityId)
  };
}

export default connect(select)(IdentityDashboard);
