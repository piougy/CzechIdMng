import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import _ from 'lodash';
//
import * as Basic from '../../basic';
import ProgressBar from '../ProgressBar/ProgressBar';
import * as Utils from '../../../utils';
import OperationStateEnum from '../../../enums/OperationStateEnum';
import { LongRunningTaskManager, ConfigurationManager, SecurityManager } from '../../../redux';

const manager = new LongRunningTaskManager();

/**
 * Long running task detail - progress bar
 *
 * @author Radek Tomiška
 */
class LongRunningTask extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      running: false
    };
  }

  getContentKey() {
    return 'content.scheduler.running-tasks';
  }

  componentDidMount() {
    const { entity, _entity } = this.props;
    if (entity || _entity) {
      this.setRefresh(entity ? entity : _entity);
    }
    this._loadEntity();
  }

  updateLrtState() {
    const { entityIdentifier, entity, _entity } = this.props;

    let identifier = entityIdentifier;
    if (entity || _entity) {
      identifier = entity ? entity.id : _entity.id;
    }

    const { updateIntervalId } = this.state;
    this.context.store.dispatch(manager.fetchEntity(identifier, null, (task) => {
      if (task && OperationStateEnum.findSymbolByKey(task.resultState) !== OperationStateEnum.RUNNING) {
        clearInterval(updateIntervalId);
        this.setState({
          updateIntervalId: null
        });
      }
    }));
  }

  componentDidUpdate() {
    this._loadEntity();
  }

  componentWillUnmount() {
    const { updateIntervalId } = this.state;
    clearInterval(updateIntervalId);
  }

  /**
   * if id is setted and entity is not - then load entity
   */
  _loadEntity() {
    const { entity, _entity, entityIdentifier } = this.props;
    if (entityIdentifier && !entity && !_entity) {
      const uiKey = manager.resolveUiKey(null, entityIdentifier);
      if (!Utils.Ui.isShowLoading(this.context.store.getState(), uiKey)
          && !Utils.Ui.getError(this.context.store.getState(), uiKey)) { // show loading check has to be here - new state is needed
        this.context.store.dispatch(manager.fetchEntity(entityIdentifier, null, (task) => {
          this.setRefresh(task);
        }));
      }
    }
  }

  onCancel(task) {
    // show confirm message for deleting entity or entities
    this.refs['confirm-cancel'].show(
      this.i18n(`action.task-cancel.message`, { record: manager.getNiceLabel(task) }),
      this.i18n(`action.task-cancel.header`)
    ).then(() => {
      const uiKey = manager.resolveUiKey(null, task.id);
      this.context.store.dispatch(manager.cancel(task, uiKey, () => {
        this.addMessage({ level: 'info', message: this.i18n(`action.task-cancel.success`, { record: manager.getNiceLabel(task) }) });
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
      const uiKey = manager.resolveUiKey(null, task.id);
      this.context.store.dispatch(manager.interrupt(task, uiKey, () => {
        this.addMessage({ level: 'info', message: this.i18n(`action.task-interrupt.success`, { record: manager.getNiceLabel(task) }) });
      }));
    }, () => {
      //
    });
  }

  /**
   * Shows modal detail with given entity
   */
   showDetail(entity) {
     this.context.router.push(`/scheduler/all-tasks/${encodeURIComponent(entity.id)}/detail`);
   }

   setRefresh(task) {
     const { updateInterval } = this.props;
     if (task && OperationStateEnum.findSymbolByKey(task.resultState) === OperationStateEnum.RUNNING) {
       const updateIntervalId = setInterval(this.updateLrtState.bind(this), updateInterval);
       this.setState({
         updateIntervalId
       });
     }
   }


  render() {
    const { rendered, instanceId, entityIdentifier, entity, _showLoading, header, footerButtons } = this.props;
    //
    let _entity = this.props._entity;
    if (entity) { // entity prop has higher priority
      _entity = entity;
    }
    //
    if (!rendered || (!_entity && !entityIdentifier)) {
      return null;
    }
    if (!_entity) {
      return (
        <Basic.Panel>
          <Basic.Loading isStatic show />
        </Basic.Panel>
      );
    }
    //
    const active = OperationStateEnum.findSymbolByKey(_entity.resultState) === OperationStateEnum.RUNNING;
    return (
      <Basic.Panel showLoading={_showLoading}>
        <Basic.Confirm ref="confirm-cancel" level="warning"/>
        <Basic.Confirm ref="confirm-interrupt" level="danger"/>
        <Basic.PanelHeader text={
            header
            ||
            <span>
              { Utils.Ui.getSimpleJavaType(_entity.taskType) }
              {' '}
              <small>{ _entity.taskDescription }</small>
            </span>
          } />
        <Basic.PanelBody>
          <div><strong>{ this.i18n('entity.LongRunningTask.taskProperties.label') }</strong></div>
          {
            _.keys(_entity.taskProperties).map(propertyName => {
              return `${ propertyName }: ${ _entity.taskProperties[propertyName] }`;
            })
            .join(', ')
          }
          <ProgressBar
            style={{ marginTop: 15, marginBottom: 0 }}
            active={active}
            max={_entity.count}
            now={_entity.counter}
            bars={[
              { now: _entity.failedItemCount, bsStyle: 'danger' },
              { now: _entity.warningItemCount, bsStyle: 'warning' },
              { now: _entity.successItemCount, bsStyle: 'success' }]
            }/>
        </Basic.PanelBody>
        <Basic.PanelFooter>
          { footerButtons }
          <Basic.Button
            onClick={this.onInterrupt.bind(this, _entity)}
            level="danger"
            style={{ marginRight: 5 }}
            rendered={_entity.instanceId === instanceId && SecurityManager.hasAnyAuthority(['SCHEDULER_UPDATE']) && active}
            disabled={_showLoading}
            icon="fa:bolt">
            {this.i18n('button.interrupt')}
          </Basic.Button>
          <Basic.Button
            level="warning"
            onClick={this.onCancel.bind(this, _entity)}
            style={{ marginRight: 5 }}
            rendered={SecurityManager.hasAnyAuthority(['SCHEDULER_UPDATE']) && active}
            disabled={_showLoading}>
            {this.i18n('button.cancel')}
          </Basic.Button>
          <Basic.Button
            title={this.i18n('button.detail')}
            onClick={this.showDetail.bind(this, _entity)}
            rendered={SecurityManager.hasAnyAuthority(['SCHEDULER_READ'])}>
            {this.i18n('button.detail')}
          </Basic.Button>
        </Basic.PanelFooter>
      </Basic.Panel>
    );
  }
}

LongRunningTask.propTypes = {
  rendered: PropTypes.bool,
  instanceId: PropTypes.string,
  entity: PropTypes.object,
  entityIdentifier: PropTypes.string,
  header: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.node
  ]),
  footerButtons: PropTypes.arrayOf(React.PropTypes.object),
  updateInterval: PropTypes.number,
  /**
   * Internal entity loaded by given identifier
   */
  _entity: PropTypes.object,
  _showLoading: PropTypes.bool
};
LongRunningTask.defaultProps = {
  rendered: true,
  instanceId: null,
  entity: null,
  entityIdentifier: null,
  header: null,
  footerButtons: null,
  updateInterval: 2500,
  //
  _entity: null,
  _showLoading: false
};

function select(state, component) {
  const _id = component.entityIdentifier || (!component.entity ? null : component.entity.id);
  //
  const _entity = manager.getEntity(state, _id);
  let _showLoading = manager.isShowLoading(state, null, _id);
  if (_showLoading && _entity && OperationStateEnum.findSymbolByKey(_entity.resultState) === OperationStateEnum.RUNNING) {
    _showLoading = false;
  }
  //
  return {
    instanceId: ConfigurationManager.getPublicValue(state, 'idm.pub.app.instanceId'),
    _entity,
    _showLoading
  };
}

export default connect(select)(LongRunningTask);
