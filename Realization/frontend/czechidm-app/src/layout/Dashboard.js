

import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
//
import {Basic, ComponentService} from 'czechidm-core';

const DEFAULT_SPAN = 6;
const DASHBOARD_COMPONENT_TYPE = 'dashboard';

class Dashboard extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
    };
    this.componentService = new ComponentService();
  }

  componentDidMount() {
    this.selectNavigationItem('home');
  }

  render() {
    const { userContext } = this.props;
    //
    let dashboards = [];
    dashboards = this.componentService.getComponentDefinitions(DASHBOARD_COMPONENT_TYPE).map(component=> {
      const DashboardComponent = component.component;

      return (
        <div className={'col-lg-' + (component.span ? component.span : DEFAULT_SPAN)}>
          <DashboardComponent key={`${DASHBOARD_COMPONENT_TYPE}-${component.id}`} userID={userContext.username}/>
        </div>
      );
    });
    return (
      <Basic.Row>
        {dashboards}
      </Basic.Row>
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
  return {
    userContext: state.security.userContext
  };
}

export default connect(select)(Dashboard);
