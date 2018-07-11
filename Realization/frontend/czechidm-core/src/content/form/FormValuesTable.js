import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import { FormDefinitionAttributesValuesManager, DataManager } from '../../redux';
import * as Utils from '../../utils';
import SearchParameters from '../../domain/SearchParameters';
import PersistentTypeEnum from '../../enums/PersistentTypeEnum';

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
    return 'content.formValues';
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

  render() {
    const { uiKey, forceSearchParameters } = this.props;
    const { filterOpened } = this.state;
    return (
      <Advanced.Table
        ref="table"
        uiKey={uiKey}
        // showRowSelection={SecurityManager.hasAuthority('FORMATTRIBUTE_DELETE')}
        manager={manager}
        forceSearchParameters={forceSearchParameters}
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
                <div className="col-lg-4">
                  <Basic.EnumSelectBox
                    ref="persistentType"
                    placeholder={this.i18n('filter.type.placeholder')}
                    multiSelect={false}
                    enum={PersistentTypeEnum}/>
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

        <Advanced.Column property="ownerId" header={this.i18n('columns.owner')}
          cell={
            ({ rowIndex, data }) => {
              return (
                <Advanced.EntityInfo
                  entityType={Utils.Ui.getSimpleJavaType((data[rowIndex].ownerType))}
                  entityIdentifier={data[rowIndex].ownerId}
                  showIcon
                  face="popover"
                  showEntityType />
              );
            }
          }/>
        <Advanced.Column property="persistentType" header={this.i18n('columns.type')} face="enum" enumClass={PersistentTypeEnum} />
        <Advanced.Column property="value" header={this.i18n('columns.value')} />
        <Advanced.Column property="_embedded.formAttribute.name" header={this.i18n('columns.name')} />
        <Advanced.Column property="_embedded.formAttribute.code" header={this.i18n('columns.code')} />
        <Advanced.Column property="_embedded.formAttribute.defaultValue" header={this.i18n('columns.default')} />
        <Advanced.Column property="_embedded.formAttribute.faceType" header={this.i18n('columns.default')} />

      </Advanced.Table>
    );
  }
}

FormValuesTable.propTypes = {
  filterOpened: PropTypes.bool,
  uiKey: PropTypes.string.isRequired,
  forceSearchParameters: PropTypes.object,
};

FormValuesTable.defaultProps = {
  filterOpened: true,
  forceSearchParameters: null
};

function select(state, component) {
  const { uiKey } = component;
  return {
    _searchParameters: Utils.Ui.getSearchParameters(state, uiKey),
  };
}

export default connect(select)(FormValuesTable);
