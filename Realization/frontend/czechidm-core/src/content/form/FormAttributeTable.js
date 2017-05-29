import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import uuid from 'uuid';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import { SecurityManager, FormAttributeManager } from '../../redux';
import SearchParameters from '../../domain/SearchParameters';

const attributeManager = new FormAttributeManager();

/**
* Table of forms attributes
*
* @author Ondřej Kopr
* @author Radek Tomiška
*/
class FormAttributeTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      filterOpened: props.filterOpened,
      showLoading: true
    };
  }

  getContentKey() {
    return 'content.formAttributes';
  }

  getManager() {
    return attributeManager;
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
    const { formDefinitionId } = this.props;
    if (entity.id === undefined) {
      const uuidId = uuid.v1();
      this.context.router.push(`/forms/attribute/${uuidId}?new=1&formDefinition=${formDefinitionId}`);
    } else {
      this.context.router.push('/forms/attribute/' + entity.id);
    }
  }

  render() {
    const { uiKey, formDefinitionId } = this.props;
    const { filterOpened } = this.state;

    return (
      <div>
        <Basic.Confirm ref="confirm-delete" level="danger"/>
        <Advanced.Table
          ref="table"
          uiKey={ uiKey }
          showRowSelection={ SecurityManager.hasAuthority('EAVFORMATTRIBUTES_DELETE') }
          manager={ attributeManager }
          forceSearchParameters={ new SearchParameters().setFilter('formDefinitionId', formDefinitionId) }
          rowClass={({rowIndex, data}) => { return data[rowIndex].disabled ? 'disabled' : ''; }}
          filter={
            <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
              <Basic.AbstractForm ref="filterForm">
                <Basic.Row>
                  <div className="col-lg-6">
                    <Advanced.Filter.TextField
                      ref="code"
                      placeholder={this.i18n('filter.code')}/>
                  </div>
                  <div className="col-lg-6 text-right">
                    <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
                  </div>
                </Basic.Row>
                <Basic.Row>
                  <div className="col-lg-6">

                  </div>
                </Basic.Row>
              </Basic.AbstractForm>
            </Advanced.Filter>
          }
          actions={
            [
              { value: 'delete', niceLabel: this.i18n('action.delete.action'), action: this.onDelete.bind(this), disabled: false }
            ]
          }
          buttons={
            [
              <Basic.Button
                level="success"
                key="add_button"
                className="btn-xs"
                onClick={this.showDetail.bind(this, { })}
                rendered={SecurityManager.hasAuthority('EAVFORMATTRIBUTES_CREATE')}>
                <Basic.Icon type="fa" icon="plus"/>
                {' '}
                {this.i18n('button.add')}
              </Basic.Button>
            ]
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
                    onClick={this.showDetail.bind(this, data[rowIndex])}/>
                );
              }
            }
            sort={false}
            _searchParameters={ this.getSearchParameters() }/>
          <Advanced.Column property="seq" sort width="5%"/>
          <Advanced.Column property="code" sort/>
          <Advanced.Column property="name" sort/>
          <Advanced.Column property="persistentType" sort />
          <Advanced.Column property="unmodifiable" header={this.i18n('entity.FormAttribute.unmodifiable.label')} face="bool" sort />
        </Advanced.Table>
      </div>
      );
  }
}

FormAttributeTable.propTypes = {
  filterOpened: PropTypes.bool,
  uiKey: PropTypes.string.isRequired,
  formDefinitionId: PropTypes.string.isRequired
};

FormAttributeTable.defaultProps = {
  filterOpened: true,
};

function select(state, component) {
  return {
    _searchParameters: state.data.ui[component.uiKey] ? state.data.ui[component.uiKey].searchParameters : null
  };
}

export default connect(select)(FormAttributeTable);
