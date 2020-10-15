import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import { IdentityManager, EntityEventManager, DataManager } from '../../redux';
import SearchParameters from '../../domain/SearchParameters';
import EntityEventTable from '../audit/event/EntityEventTable';
import EntityStateTable from '../audit/event/EntityStateTable';

const manager = new EntityEventManager();
const identityManager = new IdentityManager();

/**
 * Identity events
 * - super owner events are shown.
 *
 * @author Radek Tomiška
 */
class IdentityEvents extends Basic.AbstractContent {

  componentDidMount() {
    super.componentDidMount();
    //
    // fetch counts
    const forceSearchParameters = new SearchParameters()
      .setFilter('superOwnerId', this.props.match.params.entityId)
      .setFilter('superOwnerType', 'eu.bcvsolutions.idm.core.model.entity.IdmIdentity');
    this.context.store.dispatch(
      manager.fetchEntitiesCount(
        forceSearchParameters.setFilter('states', ['CREATED', 'RUNNING']),
        `entity-event-info-count-${ this.props.match.params.entityId }`
      )
    );
    this.context.store.dispatch(
      manager.fetchEntitiesCount(
        forceSearchParameters.setFilter('states', ['EXCEPTION']),
        `entity-event-error-count-${ this.props.match.params.entityId }`
      )
    );
  }

  getManager() {
    return identityManager;
  }

  getContentKey() {
    return 'content.entityEvents';
  }

  getNavigationKey() {
    return 'profile-events';
  }

  render() {
    const { infoCounter, errorCounter } = this.props;
    const forceSearchParameters = new SearchParameters()
      .setFilter('superOwnerId', this.props.match.params.entityId)
      .setFilter('superOwnerType', 'eu.bcvsolutions.idm.core.model.entity.IdmIdentity');
    return (

      <Basic.Div className="tab-pane-table-body">
        <Basic.ContentHeader style={{ marginBottom: 0 }}>
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
        </Basic.ContentHeader>

        <Basic.Panel className="no-border" rendered={ false }>
          <EntityStateTable
            uiKey="identity-entity-state-table"
            filterOpened={ false }
            forceSearchParameters={ forceSearchParameters }/>
        </Basic.Panel>

        <Basic.ContentHeader text={ this.i18n('event.header') } style={{ marginBottom: 0 }} rendered={ false }/>
        <Basic.Panel className="no-border last">
          <EntityEventTable
            ref="table"
            uiKey="identity-entity-event-table"
            filterOpened={false}
            forceSearchParameters={ forceSearchParameters }
            showDeleteAllButton={ false }
            className="no-margin"/>
        </Basic.Panel>
      </Basic.Div>
    );
  }
}

function select(state, component) {
  const { entityId } = component.match.params;
  //
  return {
    identity: identityManager.getEntity(state, entityId),
    infoCounter: DataManager.getData(state, `entity-event-info-count-${ entityId }`),
    errorCounter: DataManager.getData(state, `entity-event-error-count-${ entityId }`)
  };
}

export default connect(select)(IdentityEvents);
