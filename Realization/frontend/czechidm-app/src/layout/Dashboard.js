

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
    const dashboards = [];
    let rowDashboards = [];
    let spanCounter = 0;
    this.componentService.getComponentDefinitions(DASHBOARD_COMPONENT_TYPE).forEach(component=> {
      const DashboardComponent = component.component;
      const _span = component.span ? component.span : DEFAULT_SPAN;
      const spanDecorator = (
        <div className={`col-lg-${_span}`}>
          <DashboardComponent key={`${DASHBOARD_COMPONENT_TYPE}-${component.id}`} entityId={userContext.username}/>
        </div>
      );

      rowDashboards.push(spanDecorator);
      spanCounter = spanCounter + _span;
      if (spanCounter > 12) {
        spanCounter = 0;
        dashboards.push(
          <Basic.Row>
            {rowDashboards}
          </Basic.Row>
        );
        rowDashboards = [];
      }
    });
    if (rowDashboards.length > 0) {
      dashboards.push(
        <Basic.Row>
          {rowDashboards}
        </Basic.Row>
      );
    }
    //
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
