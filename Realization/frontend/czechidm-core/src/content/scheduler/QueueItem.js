import React from 'react';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import { LongRunningTaskManager} from '../../redux';
import QueueItemTable from './QueueItemTable';

const UIKEY = 'queue-item-table';
const manager = new LongRunningTaskManager();

/**
 * Queue(list) of processed items
 *
 * @author Marek Klement
 */
class QueueItem extends Basic.AbstractContent {

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
        <QueueItemTable entity={entity} uiKey={UIKEY}/>
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

export default connect(select)(QueueItem);
