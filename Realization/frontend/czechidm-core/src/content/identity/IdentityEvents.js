import React from 'react';
import _ from 'lodash';
//
import * as Basic from '../../components/basic';
import { IdentityManager } from '../../redux';
import SearchParameters from '../../domain/SearchParameters';
import EntityEventTableComponent, { EntityEventTable } from '../audit/event/EntityEventTable';
import EntityStateTableComponent, { EntityStateTable } from '../audit/event/EntityStateTable';

/**
 * Identity events
 *
 * @author Radek Tomiška
 */
export default class IdentityEvents extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.identityManager = new IdentityManager();
  }

  getManager() {
    return this.identityManager;
  }

  getContentKey() {
    return 'content.entityEvents';
  }

  getNavigationKey() {
    return 'profile-events';
  }

  render() {
    const forceSearchParameters = new SearchParameters()
      .setFilter('ownerId', this.props.params.entityId)
      .setFilter('ownerType', 'eu.bcvsolutions.idm.core.model.entity.IdmIdentity');
    return (
      <div className="tab-pane-table-body">

        { this.renderContentHeader({ style: { marginBottom: 0 }}) }

        <Basic.Panel className="no-border" rendered={ false }>
          <EntityStateTableComponent
            uiKey="identity-entity-state-table"
            filterOpened={false}
            forceSearchParameters={ forceSearchParameters }
            columns= { _.difference(EntityStateTable.defaultProps.columns, ['ownerType', 'ownerId']) }/>
        </Basic.Panel>

        <Basic.ContentHeader text={ this.i18n('event.header') } style={{ marginBottom: 0 }} rendered={ false }/>
        <Basic.Panel className="no-border last">
          <EntityEventTableComponent
            uiKey="identity-entity-event-table"
            filterOpened={false}
            forceSearchParameters={ forceSearchParameters }
            columns= { _.difference(EntityEventTable.defaultProps.columns, ['ownerType', 'ownerId']) }/>
        </Basic.Panel>
      </div>
    );
  }
}
