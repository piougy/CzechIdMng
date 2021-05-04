import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import * as Basic from '../../../components/basic';
import { EntityEventManager, DataManager, SecurityManager } from '../../../redux';
import SearchParameters from '../../../domain/SearchParameters';
import EntityEventTable from './EntityEventTable';

const manager = new EntityEventManager();

/**
 * List of persisted entity events.
 *
 * @author Radek Tomiška
 */
class EntityEvents extends Basic.AbstractContent {

  getContentKey() {
    return 'content.entityEvents';
  }

  getNavigationKey() {
    return 'entity-events';
  }

  _onReload(data, error) {
    if (error) {
      return;
    }
    //
    // fetch counts
    if (SecurityManager.hasAuthority('ENTITYEVENT_COUNT')) {
      this.context.store.dispatch(
        manager.fetchEntitiesCount(
          new SearchParameters().setFilter('states', ['CREATED', 'RUNNING']),
          `entity-event-info-count`
        )
      );
      this.context.store.dispatch(
        manager.fetchEntitiesCount(
          new SearchParameters().setFilter('states', ['EXCEPTION']),
          `entity-event-error-count`
        )
      );
    }
  }

  render() {
    const { infoCounter, errorCounter } = this.props;
    //
    return (
      <Basic.Div>
        <Basic.PageHeader>
          <Helmet title={ this.i18n('title') } />
          <Basic.Div style={{ display: 'flex', alignItems: 'center' }}>
            { this.i18n('header') }
            <Basic.Badge
              level="info"
              text={ infoCounter }
              style={{ marginLeft: 5 }}
              title={ this.i18n('counter.info') }
              onClick={ () => this.refs.table.useFilterData({ states: ['CREATED', 'RUNNING'] }) }/>
            <Basic.Badge
              level="error"
              text={ errorCounter }
              style={{ marginLeft: 5 }}
              title={ this.i18n('counter.error') }
              onClick={ () => this.refs.table.useFilterData({ states: ['EXCEPTION'] }) }/>
          </Basic.Div>
        </Basic.PageHeader>

        <Basic.Panel>
          <EntityEventTable
            ref="table"
            uiKey="entity-event-table"
            filterOpened
            onReload={ this._onReload.bind(this) }/>
        </Basic.Panel>
      </Basic.Div>
    );
  }
}

EntityEvents.propTypes = {
};
EntityEvents.defaultProps = {
};

function select(state) {
  return {
    infoCounter: DataManager.getData(state, `entity-event-info-count`),
    errorCounter: DataManager.getData(state, `entity-event-error-count`)
  };
}

export default connect(select)(EntityEvents);
