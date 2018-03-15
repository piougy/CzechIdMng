import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import _ from 'lodash';
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import { IdentityManager } from '../../redux';
/**
* Table of tasks
* Without given props search parameter show this table only tasks for logged identity.
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
    this.identityManager = new IdentityManager();
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

  _getWfTaskCell({ rowIndex, data}) {
    const entity = data[rowIndex];
    if (!entity || !entity.id) {
      return '';
    }
    return (
      <Advanced.WorkflowTaskInfo entity={entity}/>
    );
  }

  render() {
    const { uiKey, taskInstanceManager, columns, searchParameters, showFilter, showToolbar, username } = this.props;
    const { filterOpened} = this.state;
    let _searchParameters = null;
    if (searchParameters == null) {
      _searchParameters = taskInstanceManager.getDefaultSearchParameters().setFilter('candidateOrAssigned', username);
    } else {
      _searchParameters = searchParameters;
    }
    return (
        <Advanced.Table
          ref="table"
          uiKey={uiKey}
          manager={taskInstanceManager}
          forceSearchParameters={_searchParameters}
          showRowSelection={false}
          filterOpened={filterOpened}
          showFilter={showFilter}
          showToolbar={showToolbar}
        >
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
          <Advanced.Column
            header=""
            property="taskDescription"
            cell={this._getWfTaskCell}
            sort={false}
            rendered={_.includes(columns, 'description')}/>
          <Advanced.Column property="taskCreated" sort face="datetime" rendered={_.includes(columns, 'created')}/>
          <Advanced.Column property="taskAssignee" sort={false} face="text" rendered={_.includes(columns, 'taskAssignee')}
            cell={({rowIndex, data}) => {
              const identityIds = [];
              for (const index in data[rowIndex].identityLinks) {
                if (data[rowIndex].identityLinks.hasOwnProperty(index)) {
                  identityIds.push(data[rowIndex].identityLinks[index].userId);
                }
              }
              return (
                <Advanced.DetailButton
                  title={this.i18n('button.detail')}
                  onClick={this.showDetail.bind(this, data[rowIndex])}/>
              );
            }
          }/>
      </Advanced.Table>
    );
  }
}

TaskInstanceTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  taskInstanceManager: PropTypes.object.isRequired,
  columns: PropTypes.arrayOf(PropTypes.string),
  filterOpened: PropTypes.bool,
  searchParameters: PropTypes.object,
  showFilter: PropTypes.bool,
  showToolbar: PropTypes.bool
};

TaskInstanceTable.defaultProps = {
  columns: ['created', 'description', 'id'],
  filterOpened: false,
  _showLoading: false,
  searchParameters: null,
  showFilter: false,
  showToolbar: false
};

function select(state, component) {
  return {
    _searchParameters: state.data.ui[component.uiKey] ? state.data.ui[component.uiKey].searchParameters : {},
    _showLoading: component.taskInstanceManager.isShowLoading(state, `${component.uiKey}-detail`),
    username: state.security.userContext.username
  };
}

export default connect(select, null, null, { withRef: true })(TaskInstanceTable);
