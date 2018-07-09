import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import { FormDefinitionAttributesValuesManager, DataManager } from '../../redux';
import * as Utils from '../../utils';
import SearchParameters from '../../domain/SearchParameters';

const manager = new FormDefinitionAttributesValuesManager();

/**
 * Table of attribute values
 * @author Roman Kučera
 */
class FormValuesTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      filterOpened: props.filterOpened,
      showLoading: true
    };
  }

  componentDidMount() {
    super.componentDidMount();
    const { uiKey } = this.props;
    this.context.store.dispatch(manager.fetchAttributesValues(uiKey));
  }

  getContentKey() {
    return 'content.formDefinitions.values';
  }

  getManager() {
    return manager;
  }

  getUiKey() {
    return this.props.uiKey;
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
    // TODO get username from uuid
    console.log('entity ' + JSON.stringify(entity));
    if (entity.id !== undefined) {
      this.context.router.push('/identity/test/eav' + entity.id);
    }
  }

  render() {
    const { uiKey, definitionId, attributeValues } = this.props;
    const { filterOpened } = this.state;
    return (
      <Advanced.Table
        ref="table"
        uiKey={uiKey}
        // showRowSelection={SecurityManager.hasAuthority('FORMATTRIBUTE_DELETE')}
        manager={manager}
        forceSearchParameters={new SearchParameters().setFilter('definitionId', definitionId)}
        // rowClass={({ rowIndex, data }) => { return data[rowIndex].disabled ? 'disabled' : ''; }}
        filter={
          <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
            <Basic.AbstractForm ref="filterForm">
              <Basic.Row>
                <div className="col-lg-6">
                  <Advanced.Filter.TextField
                    ref="text"
                    placeholder={this.i18n('filter.text.placeholder')} />
                </div>
                <div className="col-lg-6 text-right">
                  <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)} />
                </div>
              </Basic.Row>
              <Basic.Row>
                <div className="col-lg-6">

                </div>
              </Basic.Row>
            </Basic.AbstractForm>
          </Advanced.Filter>
        }
        filterOpened={!filterOpened}>
        <Advanced.Column
          header=""
          className="detail-button"
          cell={
            ({ rowIndex, data }) => {
              return (
                <Advanced.DetailButton
                  title={this.i18n('button.detail')}
                  onClick={this.showDetail.bind(this, data[rowIndex])} />
              );
            }
          }
          sort={false}
          _searchParameters={this.getSearchParameters()} />

        <Advanced.Column property="ownerId" header={'ownerId'} sort />
        <Advanced.Column property="persistentType" header={'persistentType'} sort />
        <Advanced.Column property="value" header={'value'} sort />
        <Advanced.Column property="_embedded.formAttribute.name" header={'attr'} sort />

      </Advanced.Table>
    );
  }
}

FormValuesTable.propTypes = {
  filterOpened: PropTypes.bool,
  uiKey: PropTypes.string.isRequired,
  definitionId: PropTypes.string.isRequired,
  attributeValues: PropTypes.object,
};

FormValuesTable.defaultProps = {
  filterOpened: true,
  attributeValues: null
};

function select(state, component) {
  const { uiKey } = component;
  return {
    _searchParameters: Utils.Ui.getSearchParameters(state, uiKey),
    attributeValues: DataManager.getData(state, uiKey),
  };
}

export default connect(select)(FormValuesTable);
