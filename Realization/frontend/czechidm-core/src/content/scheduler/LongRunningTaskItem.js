import React from 'react';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import { LongRunningTaskManager} from '../../redux';
import LongRunningTaskItemTable from './LongRunningTaskItemTable';

const UIKEY = 'long-running-task-item-table';
const manager = new LongRunningTaskManager();

/**
 * Detail of the LRT. Iniciate entity by ID and than forms table.
 *
 * @author Marek Klement
 */
class LongRunningTaskItem extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  componentDidMount() {
    const { entityId } = this.props.params;
    this.context.store.dispatch(manager.fetchEntity(entityId));
    this.selectNavigationItems(['system', 'scheduler', 'scheduler-all-tasks']);
  }

  render() {
    const { entity } = this.props;
    //
    return entity && (
      <div>
        <LongRunningTaskItemTable entity={entity} longRunningTaskManager={manager} uiKey={UIKEY}/>
      </div>
    );
  }
}

function select(state, component) {
  const { entityId } = component.params;
  return {
    entity: manager.getEntity(state, entityId)
  };
}

export default connect(select)(LongRunningTaskItem);
