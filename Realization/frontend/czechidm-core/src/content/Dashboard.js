import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
//
import * as Basic from '../components/basic';
import { IdentityManager } from '../redux';
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

  getContentKey() {
    return 'content.dashboard';
  }

  getNavigationKey() {
    return 'dashboard';
  }

  render() {
    const { userContext, identity } = this.props;
    this.props.match.params = { ...this.props.match.params, entityId: userContext.username };
    //
    return (
      <Basic.Div>
        <IdentityDashboard dashboard match={ this.props.match }/>
        <Basic.Div rendered={ !!identity }>
          {
            [...componentService.getComponentDefinitions(ComponentService.DASHBOARD_COMPONENT_TYPE).map(component => {
              const DashboardComponent = component.component;
              return (
                <DashboardComponent
                  key={ `${ ComponentService.DASHBOARD_COMPONENT_TYPE }-${ component.id }` }
                  entityId={ userContext.username }/>
              );
            }).values()]
          }
        </Basic.Div>
      </Basic.Div>
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
    identity: identityManager.getEntity(state, userContext.username),
  };
}

export default connect(select)(Dashboard);
