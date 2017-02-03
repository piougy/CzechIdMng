import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
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

  componentDidMount() {
    this.selectNavigationItems(['system', 'scheduler', 'scheduler-running-tasks']);
    //
    this._fetchRunningTasks();
  }

  _fetchRunningTasks() {
    const forceSearchParameters = new SearchParameters().setFilter('running', true).setSort('created', 'desc');
    this.context.store.dispatch(manager.fetchEntities(forceSearchParameters, UIKEY));
  }

  onCancel(task) {
    // show confirm message for deleting entity or entities
    this.refs['confirm-cancel'].show(
      this.i18n(`action.task-cancel.message`, { record: manager.getNiceLabel(task) }),
      this.i18n(`action.task-cancel.header`)
    ).then(() => {
      this.context.store.dispatch(manager.cancel(task, UIKEY, () => {
        this.addMessage({ message: this.i18n(`action.task-cancel.success`, { record: manager.getNiceLabel(task) }) });
      }));
    }, () => {
      //
    });
  }

  onInterrupt(task) {
    // show confirm message for deleting entity or entities
    this.refs['confirm-interrupt'].show(
      this.i18n(`action.task-interrupt.message`, { record: manager.getNiceLabel(task) }),
      this.i18n(`action.task-interrupt.header`)
    ).then(() => {
      this.context.store.dispatch(manager.interrupt(task, UIKEY, () => {
        this.addMessage({ message: this.i18n(`action.task-interrupt.success`, { record: manager.getNiceLabel(task) }) });
        this._fetchRunningTasks();
      }));
    }, () => {
      //
    });
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
        <Helmet title={this.i18n('title')} />
        <Basic.Confirm ref="confirm-cancel" level="warning"/>
        <Basic.Confirm ref="confirm-interrupt" level="danger"/>
        {
          (!_entities || _entities.length === 0)
          ?
          <Basic.Alert text="Nenalezeny žádné běžící úlohy"/>
          :
          <div>
            {
              _entities.map(entity => {
                return (
                  <Basic.Panel>
                    <Basic.PanelHeader text={<span>{entity.taskType.split('.').pop(-1)} <small>{entity.taskDescription}</small></span>} />
                    <Basic.PanelBody>
                      <Basic.ProgressBar
                        min={0}
                        max={entity.count}
                        now={entity.counter}
                        label={`${this.i18n('component.basic.ProgressBar.processed')} ${entity.counter} / ${entity.count}`}
                        active
                        style={{ marginBottom: 0 }}/>
                    </Basic.PanelBody>
                    <Basic.PanelFooter>
                      <Basic.Button
                        onClick={this.onInterrupt.bind(this, entity)}
                        level="danger"
                        style={{ marginRight: 5 }}>
                        {this.i18n('button.interrupt')}
                      </Basic.Button>
                      <Basic.Button
                        level="warning"
                        onClick={this.onCancel.bind(this, entity)}>
                        {this.i18n('button.cancel')}
                      </Basic.Button>
                    </Basic.PanelFooter>
                  </Basic.Panel>
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
