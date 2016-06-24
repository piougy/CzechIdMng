'use strict';

import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import Immutable from 'immutable';
import uuid from 'uuid';
import { connect } from 'react-redux';
import _ from 'lodash';
//
import * as Basic from '../../../../components/basic';
import * as Advanced from '../../../../components/advanced';

/**
* Table of tasks
*/
export class TaskInstanceTable extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      filterOpened: this.props.filterOpened,
      detail: {
        show: false,
        entity: {}
      }
    }
  }

  getContentKey() {
    return 'content.task.instances';
  }

  componentDidMount() {
  }

  componentDidUpdate() {
  }

  useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().useFilter(this.refs.filterForm);
  }

  cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().cancelFilter(this.refs.filterForm);
  }

  showDetail(entity) {
    this.setState({
      detail: {
        show: true,
        showLoading: false,
        entity: entity
      }
    }, () => {
      this.refs.form.setData(entity);
      this.refs.name.focus();
    });
  }

  closeDetail() {
    this.setState({
      detail: {
        show: false,
        entity: {}
      }
    });
  }

  save(event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }
    const entity = this.refs.form.getData();
    const { taskInstanceManager, uiKey } = this.props;
    //
    if (entity.id === undefined) {
      this.context.store.dispatch(taskInstanceManager.createEntity(entity, `${uiKey}-detail`, (entity, error) => {
        this._afterSave(entity, error);
        if (!error) {
          this.refs.table.getWrappedInstance().reload();
        }
      }));
    } else {
      this.context.store.dispatch(taskInstanceManager.patchEntity(entity, `${uiKey}-detail`, this._afterSave.bind(this)));
    }
  }

  _afterSave(entity, error) {
    if (error) {
      this.refs.form.processEnded();
      this.addError(error);
      return;
    }
    this.addMessage({ message: this.i18n('save.success', { name: entity.name }) });
    this.closeDetail();
  }

  render() {
    const { uiKey, taskInstanceManager, columns, _showLoading } = this.props;
    const { filterOpened, detail } = this.state;

    return (
      <div>
        <Advanced.Table
          ref="table"
          uiKey={uiKey}
          manager={taskInstanceManager}
          showRowSelection={false}
          filterOpened={filterOpened}>

          <Advanced.Column
            header=""
            className="detail-button"
            cell={
              ({ rowIndex, data }) => {
                return (
                  <Advanced.DetailButton
                    title={this.i18n('button.detail')}
                    onClick={this.showDetail.bind(this, data[rowIndex])}/>
                );
              }
            }
            sort={false}/>
          <Advanced.ColumnLink property="taskDescription" to="task/:id" sort={true} face="text" rendered={_.includes(columns, 'description')}/>
          <Advanced.Column property="taskCreated" sort={true} face="datetime" rendered={_.includes(columns, 'created')}/>
          <Advanced.Column property="id" sort={true} face="text" rendered={_.includes(columns, 'id')}/>
        </Advanced.Table>
      </div>
    );
  }
}

TaskInstanceTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  taskInstanceManager: PropTypes.object.isRequired,
  columns: PropTypes.arrayOf(PropTypes.string),
  filterOpened: PropTypes.bool
};

TaskInstanceTable.defaultProps = {
  columns: ['created', 'description','id'],
  filterOpened: false,
  _showLoading: false
};

function select(state, component) {
  return {
    _searchParameters: state.data.ui[component.uiKey] ? state.data.ui[component.uiKey].searchParameters : {},
    _showLoading: component.taskInstanceManager.isShowLoading(state, `${component.uiKey}-detail`)
  };
}

export default connect(select, null, null, { withRef: true })(TaskInstanceTable);
