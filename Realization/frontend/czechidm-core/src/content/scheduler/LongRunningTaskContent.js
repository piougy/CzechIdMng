import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import { LongRunningTaskManager, SchedulerManager, DataManager } from '../../redux';
import LongRunningTaskDetail from './LongRunningTaskDetail';

const manager = new LongRunningTaskManager();
const schedulerManager = new SchedulerManager();

/**
 * Detail of the LRT. Iniciate entity by ID and than forms table.
 *
 * @author Marek Klement
 * @author Radek Tomiška
 */
class LongRunningTaskContent extends Basic.AbstractContent {

  componentDidMount() {
    const { entityId } = this.props.match.params;
    this.selectNavigationItem('long-running-task-detail');
    this.context.store.dispatch(manager.fetchEntity(entityId));
    this.context.store.dispatch(schedulerManager.fetchSupportedTasks());
  }

  render() {
    const { entity, showLoading, supportedTasks } = this.props;
    //
    if (showLoading && !entity) {
      return (
        <Basic.Loading isStatic showLoading />
      );
    }
    //
    return entity && (
      <LongRunningTaskDetail entity={ entity } supportedTasks={ supportedTasks }/>
    );
  }
}

LongRunningTaskContent.propTypes = {
  /**
   * Loaded entity
   */
  entity: PropTypes.object,
  /**
   * Entity is currently loaded from BE
   */
  showLoading: PropTypes.bool
};

LongRunningTaskContent.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.match.params;
  return {
    entity: manager.getEntity(state, entityId),
    showLoading: manager.isShowLoading(state, null, entityId),
    supportedTasks: DataManager.getData(state, SchedulerManager.UI_KEY_SUPPORTED_TASKS)
  };
}

export default connect(select)(LongRunningTaskContent);
