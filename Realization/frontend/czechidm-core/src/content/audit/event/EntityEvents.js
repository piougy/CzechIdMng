import React from 'react';
import Helmet from 'react-helmet';
//
import * as Basic from '../../../components/basic';
import EntityEventTable from './EntityEventTable';

/**
 * List of persisted entity events
 *
 * @author Radek Tomiška
 */
export default class EntityEvents extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.entityEvents';
  }

  getNavigationKey() {
    return 'entity-events';
  }

  render() {
    return (
      <div>
        <Helmet title={ this.i18n('title') } />
        <EntityEventTable uiKey="entity-event-table" filterOpened />
      </div>
    );
  }
}

EntityEvents.propTypes = {
};
EntityEvents.defaultProps = {
};
