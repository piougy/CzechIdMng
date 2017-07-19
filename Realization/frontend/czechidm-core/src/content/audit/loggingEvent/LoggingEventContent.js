import React from 'react';
import { connect } from 'react-redux';
import * as Basic from '../../../components/basic';
import LoggingEventTable from './LoggingEventTable';
import Helmet from 'react-helmet';

class EventContent extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.audit.logging-event';
  }

  componentDidMount() {
    this.selectNavigationItems(['audit', 'audits', 'audit-logging-events']);
  }

  render() {
    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <LoggingEventTable uiKey="audit-event-table"/>
      </div>
    );
  }
}

EventContent.propTypes = {
};

EventContent.defaultProps = {
};

function select() {
  return {
  };
}

export default connect(select)(EventContent);
