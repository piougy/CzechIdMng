import PropTypes from 'prop-types';
import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import SearchParameters from '../../domain/SearchParameters';
import { LongRunningTaskManager } from '../../redux';

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
    const { creatorId } = this.props;
    if (creatorId) {
      // FIXME: this content is reused on dashboard - refactor to two route contents
      return null;
    }
    return 'scheduler-running-tasks';
  }

  componentDidMount() {
    super.componentDidMount();
    //
    this._fetchRunningTasks();
  }

  getManager() {
    return this.props.manager;
  }

  _fetchRunningTasks() {
    const { creatorId, uiKey } = this.props;
    //
    const forceSearchParameters = new SearchParameters().setFilter('running', true).setFilter('stateful', true).setFilter('creatorId', creatorId).setSort('created', 'desc');
    this.context.store.dispatch(this.getManager().fetchEntities(forceSearchParameters, uiKey));
  }

  render() {
    const { _entities, _showLoading, creatorId } = this.props;
    //
    return (
      <div>
        {
          creatorId
          ?
          null
          :
          <Helmet title={ this.i18n('title') } />
        }
        <Basic.Toolbar>
          <div className="pull-right">
            <Advanced.RefreshButton
              onClick={ this._fetchRunningTasks.bind(this) }
              title={ this.i18n('refresh') }
              showLoading={ _showLoading }/>
          </div>
        </Basic.Toolbar>
        {
          _showLoading
          ?
          <Basic.Loading show isStatic />
          :
          <div style={{ padding: '15px 15px 0' }}>
            {
              (!_entities || _entities.length === 0)
              ?
              <Basic.Alert className="no-margin" text={ this.i18n('empty') }/>
              :
              <div>
                {
                  [..._entities.map(entity => {
                    return (
                      <Advanced.LongRunningTask entity={ entity } />
                    );
                  }).values()]
                }
              </div>
            }
          </div>
        }
      </div>
    );
  }
}

RunningTasks.propTypes = {
  creatorId: PropTypes.string,
  _showLoading: PropTypes.bool,
  _entities: PropTypes.arrayOf(PropTypes.object)
};
RunningTasks.defaultProps = {
  uiKey: UIKEY,
  manager,
  creatorId: null,
  _showLoading: true,
  _entities: []
};

function select(state, component) {
  const uiKey = component.uiKey || UIKEY;
  //
  return {
    _showLoading: Utils.Ui.isShowLoading(state, uiKey),
    _entities: Utils.Ui.getEntities(state, uiKey)
  };
}

export default connect(select)(RunningTasks);
