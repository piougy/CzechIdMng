import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import * as Utils from '../../utils';
import SearchParameters from '../../domain/SearchParameters';
import { LongRunningTaskManager } from '../../redux';
import LongRunningTask from './LongRunningTask';

const UIKEY = 'active-long-running-task-table';
const manager = new LongRunningTaskManager();

/**
 * Running tasks overview
 *
 * @author Radek Tomiška
 */
class RunningTasks extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.scheduler.running-tasks';
  }

  getNavigationKey() {
    return 'scheduler-running-tasks';
  }

  componentDidMount() {
    super.componentDidMount();
    //
    this._fetchRunningTasks();
  }

  _fetchRunningTasks() {
    const forceSearchParameters = new SearchParameters().setFilter('running', true).setFilter('stateful', true).setSort('created', 'desc');
    this.context.store.dispatch(manager.fetchEntities(forceSearchParameters, UIKEY));
  }

  render() {
    const { _entities, _showLoading } = this.props;
    //
    if (_showLoading) {
      return (
        <Basic.Loading show isStatic />
      );
    }
    //
    return (
      <div style={{ padding: '15px 15px 0' }}>
        <Helmet title={ this.i18n('title') } />
        {
          (!_entities || _entities.length === 0)
          ?
          <Basic.Alert text={ this.i18n('empty') }/>
          :
          <div>
            {
              _entities.map(entity => {
                return (
                  <LongRunningTask entity={ entity } />
                );
              })
            }
          </div>
        }
      </div>
    );
  }
}

RunningTasks.propTypes = {
  _showLoading: PropTypes.bool,
  _entities: PropTypes.arrayOf(React.PropTypes.object)
};
RunningTasks.defaultProps = {
  _showLoading: true,
  _entities: []
};

function select(state) {
  return {
    _showLoading: Utils.Ui.isShowLoading(state, UIKEY),
    _entities: Utils.Ui.getEntities(state, UIKEY)
  };
}

export default connect(select)(RunningTasks);
