import React from 'react';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import SearchParameters from '../../domain/SearchParameters';
import { LongRunningTaskManager } from '../../redux';
import AuditTable from '../audit/AuditTable';

const longRunningTaskManager = new LongRunningTaskManager();

/**
 * LRT audit - modified entities by selected task.
 *
 * @author Radek Tomiška
 * @since 9.7.0
 */
class LongRunningTaskAudit extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.scheduler.audit';
  }

  getNavigationKey() {
    return 'long-running-task-audit';
  }

  render() {
    const { entity } = this.props;
    //
    if (!entity) {
      return null;
    }
    const forceSearchParameters = new SearchParameters().setFilter('transactionId', entity.transactionId);
    //
    return (
      <Basic.Div>
        { this.renderContentHeader({ style: { marginBottom: 0 }}) }
        <Basic.Panel className="no-border last">
          <AuditTable
            uiKey={ `scheduled-task-${entity.id}-audit-table` }
            forceSearchParameters={ forceSearchParameters }
            className="no-margin"/>
        </Basic.Panel>
      </Basic.Div>
    );
  }
}

function select(state, component) {
  const { entityId } = component.match.params;
  return {
    entity: longRunningTaskManager.getEntity(state, entityId)
  };
}

export default connect(select)(LongRunningTaskAudit);
