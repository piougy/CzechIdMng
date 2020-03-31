import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import uuid from 'uuid';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import { FormProjectionManager } from '../../redux';

const manager = new FormProjectionManager();

/**
* Form projection table.
*
* @author Radek Tomiška
*/
export class FormProjectionTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      filterOpened: props.filterOpened,
      showLoading: true
    };
  }

  componentDidMount() {
    super.componentDidMount();
    //
    this.refs.text.focus();
  }

  getContentKey() {
    return 'content.form-projections';
  }

  getManager() {
    return manager;
  }

  useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.useFilterForm(this.refs.filterForm);
  }

  cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.cancelFilter(this.refs.filterForm);
  }

  showDetail(entity) {
    if (entity.id === undefined) {
      this.context.history.push(`/form-projections/${ uuid.v1() }/detail?new=1`);
    } else {
      this.context.history.push(`/form-projections/${ entity.id }/detail`);
    }
  }

  render() {
    const { filterOpened } = this.state;
    //
    return (
      <Basic.Div>
        <Advanced.Table
          ref="table"
          uiKey={ this.getUiKey() }
          manager={ this.getManager() }
          showRowSelection
          filter={
            <Advanced.Filter onSubmit={ this.useFilter.bind(this) }>
              <Basic.AbstractForm ref="filterForm">
                <Basic.Row className="last">
                  <Basic.Col lg={ 6 }>
                    <Advanced.Filter.TextField
                      ref="text"
                      placeholder={ this.i18n('filter.text.placeholder') }/>
                  </Basic.Col>
                  <Basic.Col lg={ 6 } className="text-right">
                    <Advanced.Filter.FilterButtons cancelFilter={ this.cancelFilter.bind(this) }/>
                  </Basic.Col>
                </Basic.Row>
              </Basic.AbstractForm>
            </Advanced.Filter>
          }
          buttons={
            [
              <Basic.Button
                level="success"
                key="add_button"
                className="btn-xs"
                onClick={this.showDetail.bind(this, { })}
                rendered={ this.getManager().canSave() }
                icon="fa:plus">
                { this.i18n('button.add') }
              </Basic.Button>
            ]
          }
          filterOpened={ filterOpened }
          _searchParameters={ this.getSearchParameters() }>
          <Advanced.Column
            header=""
            className="detail-button"
            cell={
              ({ rowIndex, data }) => {
                return (
                  <Advanced.DetailButton
                    title={ this.i18n('button.detail') }
                    onClick={ this.showDetail.bind(this, data[rowIndex]) }/>
                );
              }
            }
            sort={false}/>
          <Advanced.Column property="ownerType" sort rendered={ false }/>
          <Advanced.Column property="code" sort/>
          <Advanced.Column property="route" sort/>
          <Advanced.Column property="module" sort/>
          <Advanced.Column property="description" sort/>
        </Advanced.Table>
      </Basic.Div>
    );
  }
}

FormProjectionTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  filterOpened: PropTypes.bool
};

FormProjectionTable.defaultProps = {
  filterOpened: true
};

function select(state, component) {
  return {
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey)
  };
}

export default connect(select, null, null, { forwardRef: true })(FormProjectionTable);
