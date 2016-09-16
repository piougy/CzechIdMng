import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import _ from 'lodash';
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';

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
    };
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
    this.refs.table.getWrappedInstance().useFilterForm(this.refs.filterForm);
  }

  cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().cancelFilter(this.refs.filterForm);
  }

  showDetail(entity) {
    this.context.router.push('task/' + entity.id);
  }

  closeDetail() {
    this.setState({
      detail: {
        show: false,
        entity: {}
      }
    });
  }

  render() {
    const { uiKey, taskInstanceManager, columns } = this.props;
    const { filterOpened} = this.state;

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
          <Advanced.ColumnLink property="taskDescription" to="task/:id" sort face="text" rendered={_.includes(columns, 'description')}/>
          <Advanced.Column property="taskCreated" sort face="datetime" rendered={_.includes(columns, 'created')}/>
          <Advanced.Column property="id" sort face="text" rendered={_.includes(columns, 'id')}/>
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
  columns: ['created', 'description', 'id'],
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
