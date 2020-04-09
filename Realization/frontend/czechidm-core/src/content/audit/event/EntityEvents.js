import React from 'react';
//
import * as Basic from '../../../components/basic';
import EntityEventTable from './EntityEventTable';

/**
 * List of persisted entity events
 *
 * @author Radek Tomiška
 */
export default class EntityEvents extends Basic.AbstractContent {

  getContentKey() {
    return 'content.entityEvents';
  }

  getNavigationKey() {
    return 'entity-events';
  }

  render() {
    return (
      <Basic.Div>
        { this.renderPageHeader() }

        <Basic.Panel>
          <EntityEventTable uiKey="entity-event-table" filterOpened />
        </Basic.Panel>
      </Basic.Div>
    );
  }
}

EntityEvents.propTypes = {
};
EntityEvents.defaultProps = {
};
