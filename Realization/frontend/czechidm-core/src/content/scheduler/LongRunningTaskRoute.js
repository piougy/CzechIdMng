import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
//
import * as Basic from '../../components/basic';
import { LongRunningTaskManager, SchedulerManager, DataManager } from '../../redux';
import * as Advanced from '../../components/advanced';

const manager = new LongRunningTaskManager();
const schedulerManager = new SchedulerManager();

/**
 * Long running task tab panel.
 *
 * @author Radek Tomiška
 */
class LongRunningTaskRoute extends Basic.AbstractContent {

  componentDidMount() {
    const { entityId } = this.props.match.params;
    //
    this.context.store.dispatch(schedulerManager.fetchSupportedTasks());
    this.context.store.dispatch(manager.fetchEntityIfNeeded(entityId, null, (entity, error) => {
      this.handleError(error);
    }));
  }

  getContentKey() {
    return 'content.scheduler.all-tasks';
  }

  render() {
    const { entity, showLoading, supportedTasks } = this.props;
    //
    return (
      <Basic.Div>
        <Helmet title={ this.i18n('title') } />

        <Advanced.DetailHeader
          showLoading={ showLoading && !entity }
          entity={ entity }
          back="/scheduler/all-tasks">
          {
            entity
            ?
            <Advanced.LongRunningTaskName entity={ entity } supportedTasks={ supportedTasks } showTaskType={ false }/>
            :
            null
          }
          <small> { this.i18n('detail.header') }</small>
        </Advanced.DetailHeader>

        <Advanced.TabPanel parentId="scheduler-all-tasks" match={ this.props.match }>
          { this.getRoutes() }
        </Advanced.TabPanel>
      </Basic.Div>
    );
  }
}

LongRunningTaskRoute.propTypes = {
  entity: PropTypes.object,
  showLoading: PropTypes.bool
};
LongRunningTaskRoute.defaultProps = {
  entity: null,
  showLoading: false
};

function select(state, component) {
  const { entityId } = component.match.params;
  return {
    entity: manager.getEntity(state, entityId),
    showLoading: manager.isShowLoading(state, null, entityId),
    supportedTasks: DataManager.getData(state, SchedulerManager.UI_KEY_SUPPORTED_TASKS)
  };
}

export default connect(select)(LongRunningTaskRoute);
