import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import _ from 'lodash';
import uuid from 'uuid';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import { LongRunningTaskManager, ConfigurationManager, SecurityManager } from '../../redux';

const manager = new LongRunningTaskManager();

/**
 * Long running task detail - progress bar
 *
 * @author Radek Tomiška
 */
class LongRunningTask extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.scheduler.running-tasks';
  }

  componentDidMount() {
    this._loadEntity();
  }

  componentDidUpdate() {
    this._loadEntity();
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
        this.context.store.dispatch(manager.fetchEntity(entityIdentifier, null, () => {}));
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
     if (entity.id === undefined) {
       const uuidId = uuid.v1();
       this.context.router.push(`long-running-task/new?id=${uuidId}`);
     } else {
       this.context.router.push(`long-running-task/${encodeURIComponent(entity.id)}`);
     }
   }

  render() {
    const { rendered, instanceId, entityIdentifier, entity, _showLoading, header } = this.props;
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
    return (
      <Basic.Panel showLoading={_showLoading}>
        <Basic.Confirm ref="confirm-cancel" level="warning"/>
        <Basic.Confirm ref="confirm-interrupt" level="danger"/>
        <Basic.PanelHeader text={ header || <span><Advanced.DetailButton
            title={this.i18n('button.detail')}
            onClick={this.showDetail.bind(this, _entity)}/>
            <span> </span>{ Utils.Ui.getSimpleJavaType(_entity.taskType) } <small>{ _entity.taskDescription }</small></span> } />
        <Basic.PanelBody>
          <div><strong>{ this.i18n('entity.LongRunningTask.taskProperties.label') }</strong></div>
          {
            _.keys(_entity.taskProperties).map(propertyName => {
              return `${ propertyName }: ${ _entity.taskProperties[propertyName] }`;
            })
            .join(', ')
          }
          <Advanced.ProgressBar
            max={_entity.count}
            now={_entity.counter}
            style={{ marginTop: 15, marginBottom: 0 }}/>
        </Basic.PanelBody>
        <Basic.PanelFooter>
          <Basic.Button
            onClick={this.onInterrupt.bind(this, _entity)}
            level="danger"
            style={{ marginRight: 5 }}
            rendered={_entity.instanceId === instanceId && SecurityManager.hasAnyAuthority(['SCHEDULER_UPDATE'])}
            disabled={_showLoading}
            icon="fa:bolt">
            {this.i18n('button.interrupt')}
          </Basic.Button>
          <Basic.Button
            level="warning"
            onClick={this.onCancel.bind(this, _entity)}
            rendered={SecurityManager.hasAnyAuthority(['SCHEDULER_UPDATE'])}
            disabled={_showLoading}>
            {this.i18n('button.cancel')}
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
  //
  _entity: null,
  _showLoading: false
};

function select(state, component) {
  const _id = component.entityIdentifier || (!component.entity ? null : component.entity.id);
  //
  return {
    instanceId: ConfigurationManager.getPublicValue(state, 'idm.pub.app.instanceId'),
    _entity: manager.getEntity(state, _id),
    _showLoading: manager.isShowLoading(state, null, _id)
  };
}

export default connect(select)(LongRunningTask);
