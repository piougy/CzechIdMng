import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
//
import * as Basic from '../components/basic';
import { SecurityManager, DataManager, IdentityManager } from '../redux';
import ComponentService from '../services/ComponentService';
import IdentityDashboard from './identity/IdentityDashboard';

const componentService = new ComponentService();

const identityManager = new IdentityManager();

/**
 * Dashboard - personalized dashboard with quick buttons and overview
 *
 * @author Radek Tomiška
 * @since 9.4.0
 */

class Dashboard extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
    };
  }

  componentDidMount() {
    super.componentDidMount();
    //
    const { userContext } = this.props;
    if (userContext) {
      this.context.store.dispatch(identityManager.fetchAuthorities(userContext.username, `identity-authorities-${ userContext.username }`, (entity, error) => {
        this.handleError(error);
      }));
    }
  }

  getContentKey() {
    return 'content.dashboard';
  }

  getNavigationKey() {
    return 'dashboard';
  }

  render() {
    const { userContext, authorities, identity } = this.props;
    const _authorities = authorities ? authorities.map(authority => authority.authority) : null;
    //
    return (
      <div>
        <IdentityDashboard dashboard params={{ ...this.props.params, entityId: userContext.username }}/>

        {
          !identity
          ||
          componentService.getComponentDefinitions(ComponentService.DASHBOARD_COMPONENT_TYPE).map(component=> {
            const DashboardComponent = component.component;
            return (
              <DashboardComponent
                key={`${ComponentService.DASHBOARD_COMPONENT_TYPE}-${component.id}`}
                entityId={ userContext.username}/>
            );
          })
        }

        <div className="hidden">
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
      </div>
    );
  }
}

Dashboard.propTypes = {
  userContext: PropTypes.object
};
Dashboard.defaultProps = {
  userContext: null
};

function select(state) {
  const userContext = state.security.userContext;
  //
  return {
    userContext,
    authorities: userContext ? DataManager.getData(state, `identity-authorities-${ userContext.username }`) : null,
    identity: identityManager.getEntity(state, userContext.username),
  };
}

export default connect(select)(Dashboard);
