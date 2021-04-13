import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import uuid from 'uuid';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import { IdentityManager, DataManager, ConfigurationManager } from '../../redux';
import ComponentService from '../../services/ComponentService';
import OrganizationPosition from './OrganizationPosition';

const identityManager = new IdentityManager();
const componentService = new ComponentService();

/**
 * Identity dashboard - personalized dashboard with quick buttons and overview
 *
 * TODO:
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
    return 'dashboard';
  }

  isDashboard() {
    return this.props.dashboard;
  }

  getIdentityIdentifier() {
    const { entityId } = this.props.match.params;
    const { userContext } = this.props;
    //
    if (entityId) {
      return entityId;
    }
    if (userContext) {
      return userContext.id;
    }
    return null;
  }

  onIdentityDetail() {
    this.context.history.push(`/identity/${ encodeURIComponent(this.getIdentityIdentifier()) }/profile`);
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
    this.context.history.push(`/identity/${encodeURIComponent(this.getIdentityIdentifier())}/password/change`);
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
    this.context.history.push(`/role-requests/${uuidId}/new?new=1&applicantId=${identity.id}`);
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
    const buttonsMap = componentService
      .getComponentDefinitions(ComponentService.IDENTITY_DASHBOARD_BUTTON_COMPONENT_TYPE)
      .map(component => {
        const DashboardButtonComponent = component.component;
        return (
          <DashboardButtonComponent
            key={`${ComponentService.IDENTITY_DASHBOARD_BUTTON_COMPONENT_TYPE}-${component.id}`}
            entityId={ identity.username }
            identity={ identity }
            permissions={ _permissions }/>
        );
      });

    const dashboardsMap = componentService
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
      });
    //
    return (
      <Basic.Div>
        <Basic.PageHeader>
          <Basic.Div style={{ display: 'flex', alignItems: 'center' }}>
            <Basic.Div style={{ flex: 1 }}>
              {
                _imageUrl
                ?
                <img src={ _imageUrl } alt="profile" className="img-circle img-thumbnail" style={{ height: 40, padding: 0 }} />
                :
                <Basic.Icon icon="component:identity" identity={ identity } />
              }
              {' '}
              { identityManager.getNiceLabel(identity) }
              <small>
                {' '}
                {
                  this.isDashboard()
                  ?
                  this.i18n('content.identity.dashboard.header')
                  :
                  this.i18n('navigation.menu.profile.label')
                }
              </small>
            </Basic.Div>
            <Basic.Div style={{ fontSize: '0.85em' }}>
              <Advanced.AuditableInfo entity={ identity }/>
            </Basic.Div>
          </Basic.Div>
        </Basic.PageHeader>

        <OrganizationPosition identity={ identityIdentifier } showLink={ false }/>

        <Basic.Div style={{ paddingBottom: 15 }}>
          {
            [...buttonsMap.values()]
          }
        </Basic.Div>
        {
          [...dashboardsMap.values()]
        }
      </Basic.Div>
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
  const { entityId } = component.match.params;
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
