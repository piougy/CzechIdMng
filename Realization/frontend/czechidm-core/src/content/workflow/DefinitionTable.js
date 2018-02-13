import React, {PropTypes} from 'react';
import { connect } from 'react-redux';
import * as Utils from '../../utils';
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';

/**
 * @author Roman Kučera
 */
export class DefinitionTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.stat = {
      filterOpened: false
    };
  }

  getManager() {
    return this.props.definitionManger;
  }

  getContentKey() {
    return 'content.workflow.definitions';
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

  render() {
    const { showLoading, forceSearchParameters, uiKey, definitionManger } = this.props;
    const { filterOpened } = this.state;
    return (
      <div>
        <Basic.Panel>
          <Advanced.Table
            ref="table"
            uiKey={uiKey}
            manager={definitionManger}
            showLoading={showLoading}
            rowClass={({rowIndex, data}) => { return data[rowIndex].disabled ? 'disabled' : ''; }}
            noData={this.i18n('component.basic.Table.noData')}
            forceSearchParameters={forceSearchParameters}
            filter={
              <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
                <Basic.AbstractForm ref="filterForm">
                  <Basic.Row>
                    <div className="col-lg-6">
                      <Advanced.Filter.TextField
                        ref="processDefinitionKey"
                        placeholder={this.i18n('key')}/>
                    </div>
                    <div className="col-lg-6 text-right">
                      <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
                    </div>
                  </Basic.Row>
                  <Basic.Row className="last">
                    <div className="col-lg-6">
                      <Advanced.Filter.TextField
                        ref="name"
                        placeholder={this.i18n('name')}/>
                    </div>
                  </Basic.Row>
                </Basic.AbstractForm>
              </Advanced.Filter>
            }
            filterOpened={!filterOpened}
            _searchParameters={ this.getSearchParameters() }>
            <Advanced.Column property="key" header={this.i18n('key')} width="25%"
              cell={<Basic.LinkCell property="key" to="workflow/definitions/:key"/>} sort />
            <Advanced.Column property="name" header={this.i18n('name')} width="20%" sort />
            <Advanced.Column property="resourceName" header={this.i18n('resourceName')} width="15%" />
            <Advanced.Column property="description" header={this.i18n('description')} width="25%" />
            <Advanced.Column property="version" header={this.i18n('version')} width="5%" />
          </Advanced.Table>
        </Basic.Panel>
      </div>
    );
  }
}

DefinitionTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  definitionManger: PropTypes.object.isRequired,
  forceSearchParameters: PropTypes.object,
  showLoading: PropTypes.bool
};

DefinitionTable.defaultProps = {};

function select(state, component) {
  return {
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey)
  };
}

export default connect(select)(DefinitionTable);
