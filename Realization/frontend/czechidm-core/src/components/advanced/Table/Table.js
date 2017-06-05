import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import invariant from 'invariant';
import _ from 'lodash';
//
import * as Basic from '../../basic';
import * as Utils from '../../../utils';
import Filter from '../Filter/Filter';
import SearchParameters from '../../../domain/SearchParameters';
import UuidInfo from '../UuidInfo/UuidInfo';

/**
 * Table component with header and columns.
 */
class AdvancedTable extends Basic.AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      filterOpened: this.props.filterOpened,
      selectedRows: this.props.selectedRows
    };
  }

  /**
   * Return component identifier, with can be used in localization etc.
   *
   * @return {string} component identifier
   */
  getComponentKey() {
    return 'component.advanced.Table';
  }

  componentDidMount() {
    this.reload();
  }

  componentWillReceiveProps(newProps) {
    if (!SearchParameters.is(newProps.forceSearchParameters, this.props.forceSearchParameters)) {
      this.reload(newProps);
    } else if (!SearchParameters.is(newProps.defaultSearchParameters, this.props.defaultSearchParameters)) {
      this.reload(newProps);
    }
  }

  reload(props = null) {
    const _props = props || this.props;
    const { rendered, _searchParameters } = _props;
    if (!rendered) {
      return;
    }
    this.fetchEntities(_searchParameters, _props);
  }

  /**
   * Clears row selection
   */
  clearSelectedRows() {
    this.setState({
      selectedRows: []
    });
  }

  /**
   * Merge hard, default and user deffined search parameters
   */
  _mergeSearchParameters(searchParameters, props = null) {
    const _props = props || this.props;
    const { defaultSearchParameters, forceSearchParameters, manager } = _props;
    //
    let _forceSearchParameters = null;
    if (forceSearchParameters) {
      _forceSearchParameters = forceSearchParameters.setSize(null).setPage(null); // we dont want override setted pagination
    }
    return manager.mergeSearchParameters(searchParameters || defaultSearchParameters || manager.getDefaultSearchParameters(), _forceSearchParameters);
  }

  fetchEntities(searchParameters, props = null) {
    const _props = props || this.props;
    const { uiKey, manager } = _props;
    searchParameters = this._mergeSearchParameters(searchParameters, _props);
    this.context.store.dispatch(manager.fetchEntities(searchParameters, uiKey, (json, error) => {
      if (error) {
        this.addErrorMessage({
          key: 'error-' + manager.getEntityType() + '-load'
        }, error);
      // remove selection for unpresent records
      } else if (json && json._embedded) {
        const { selectedRows } = this.state;
        const newSelectedRows = [];
        if (json._embedded[manager.getCollectionType()]) {
          json._embedded[manager.getCollectionType()].forEach(entity => {
            if (_.includes(selectedRows, entity.id)) { // TODO: custom identifier - move to manager
              newSelectedRows.push(entity.id);
            }
          });
        }
        this.setState({
          selectedRows: newSelectedRows
        });
      }
    }));
  }

  _handlePagination(page, size) {
    const { uiKey, manager } = this.props;
    this.context.store.dispatch(manager.handlePagination(page, size, uiKey));
  }

  _handleSort(property, order) {
    const { uiKey, manager } = this.props;
    this.context.store.dispatch(manager.handleSort(property, order, uiKey));
  }

  _resolveColumns() {
    const children = [];
    //
    React.Children.forEach(this.props.children, (child) => {
      if (child == null) {
        return;
      }
      invariant(
        // child.type.__TableColumnGroup__ ||
        child.type.__TableColumn__ ||
        child.type.__AdvancedColumn__ || child.type.__AdvancedColumnLink__,
        'child type should be <TableColumn /> or ' +
        'child type should be <AdvancedColumn /> or ' +
        '<AdvancedColumnGroup />'
      );
      children.push(child);
    });
    return children;
  }

  useFilterForm(filterForm) {
    const filters = {};
    const filterValues = filterForm.getData();
    for (const property in filterValues) {
      if (!filterValues.hasOwnProperty(property)) {
        continue;
      }
      const filterComponent = filterForm.getComponent(property);
      if (!filterComponent) {
        // filter is not rendered
        continue;
      }
      const field = filterComponent.props.field || property;
      // TODO: implement multi value filters
      /* if (filterComponent.props.multiSelect === true) { // multiselect returns array of selected values
        let filledValues = filterValues[property];
        if (filterComponent.props.enum) { // enumeration
          filledValues = filledValues.map(value => { return filterComponent.props.enum.findKeyBySymbol(value); })
        }
        filter.values = filledValues;
      } else {*/
      let filledValue = filterValues[property];
      if (filterComponent.props.enum) { // enumeration
        filledValue = filterComponent.props.enum.findKeyBySymbol(filledValue);
      }
      filters[field] = filledValue;
    }
    this.useFilterData(filters);
  }

  useFilterData(formData) {
    const { _searchParameters } = this.props;
    //
    let userSearchParameters = _searchParameters;
    userSearchParameters = userSearchParameters.setPage(0);
    for (const property in formData) {
      if (!formData.hasOwnProperty(property)) {
        continue;
      }
      if (!formData[property]) {
        userSearchParameters = userSearchParameters.clearFilter(property);
      } else {
        userSearchParameters = userSearchParameters.setFilter(property, formData[property]);
      }
    }
    this.fetchEntities(userSearchParameters);
  }

  cancelFilter(filterForm) {
    const { manager, _searchParameters } = this.props;
    //
    const filterValues = filterForm.getData();
    for (const property in filterValues) {
      if (!filterValues[property]) {
        continue;
      }
      const filterComponent = filterForm.getComponent(property);
      if (!filterComponent) {
        // filter is not rendered
        continue;
      }
      filterComponent.setValue(null);
    }
    // prevent sort and pagination
    let userSearchParameters = _searchParameters.setFilters(manager.getDefaultSearchParameters().getFilters());
    userSearchParameters = userSearchParameters.setPage(0);
    //
    this.fetchEntities(userSearchParameters);
  }

  _onRowSelect(rowIndex, selected, selection) {
    const { onRowSelect } = this.props;
    //
    this.setState({
      selectedRows: selection
    }, () => {
      if (onRowSelect) {
        onRowSelect(rowIndex, selected, selection);
      }
    });
  }

  onBulkAction(actionItem) {
    if (actionItem.action) {
      actionItem.action(actionItem.value, this.state.selectedRows);
    } else {
      this.addMessage({ level: 'info', message: this.i18n('bulk-action.notImplemented') });
    }
    return false;
  }

  getNoData(noData) {
    if (noData !== null && noData !== undefined) {
      return noData;
    }
    // default noData
    return this.i18n('noData', { defaultValue: 'No record found' });
  }

  _showId() {
    const { showId } = this.props;
    //
    if (showId === null || showId === undefined) {
      return this.isDevelopment();
    }
  }

  render() {
    const {
      _entities,
      _total,
      _showLoading,
      _error,
      manager,
      pagination,
      onRowClick,
      onRowDoubleClick,
      showRowSelection,
      showLoading,
      rowClass,
      rendered,
      filter,
      showFilter,
      filterCollapsible,
      filterViewportOffsetTop,
      actions,
      buttons,
      noData,
      style,
      showPageSize,
      showToolbar,
      condensed,
      header
    } = this.props;
    const {
      filterOpened,
      selectedRows
    } = this.state;
    //
    if (!rendered) {
      return null;
    }
    //
    const columns = this._resolveColumns();
    let range = null;
    const _searchParameters = this._mergeSearchParameters(this.props._searchParameters);
    if (_searchParameters) {
      range = {
        page: _searchParameters.getPage(),
        size: _searchParameters.getSize()
      };
    }
    const renderedColumns = [];
    for (let i = 0; i < columns.length; i++) {
      const column = columns[i];
      // basic column support
      if (column.type.__TableColumn__) {
        // add cloned elemet with data provided
        renderedColumns.push(column);
        continue;
      }
      // common props to all column faces
      const commonProps = {
        // title: column.props.title,
        className: `column-face-${column.props.face}`
      };
      // construct basic column from advanced column definition
      let columnHeader = column.props.header;
      if (!columnHeader && column.props.property) {
        columnHeader = this.i18n(`${manager.getModule()}:entity.${manager.getEntityType()}.${column.props.property}`);
      }
      if (column.props.sort) {
        columnHeader = (
          <Basic.BasicTable.SortHeaderCell
            header={columnHeader}
            sortHandler={this._handleSort.bind(this)}
            sortProperty={column.props.sortProperty || column.props.property}
            searchParameters={_searchParameters}
            className={commonProps.className}/>
        );
      }

      const key = 'column_' + i;
      let cell = null;
      if (column.props.cell) {
        cell = column.props.cell;
      } else if (column.type.__AdvancedColumnLink__) {
        cell = (
          <Basic.BasicTable.LinkCell to={column.props.to} target={column.props.target} access={column.props.access} {...commonProps}/>
        );
      } else {
        switch (column.props.face) {
          case 'date': {
            cell = (
              <Basic.BasicTable.DateCell format={this.i18n('format.date')} {...commonProps}/>
            );
            break;
          }
          case 'datetime': {
            cell = (
              <Basic.BasicTable.DateCell format={this.i18n('format.datetime')} {...commonProps}/>
            );
            break;
          }
          case 'bool':
          case 'boolean': {
            cell = (
              <Basic.BasicTable.BooleanCell {...commonProps}/>
            );
            break;
          }
          case 'enum': {
            cell = (
              <Basic.BasicTable.EnumCell enumClass={column.props.enumClass} {...commonProps}/>
            );
            break;
          }
          default: {
            this.getLogger().trace('[AdvancedTable] usind default for column face [' + column.props.face + ']');
          }
        }
      }
      // add target column with cell by data type
      renderedColumns.push(
        <Basic.BasicTable.Column
          key={key}
          property={column.props.property}
          rendered={column.props.rendered}
          className={column.props.className}
          width={column.props.width}
          header={ columnHeader }
          cell={cell}/>
      );
    }
    //
    let _rowClass = rowClass;
    if (!_rowClass) {
      // automatic rowClass by entity's "disabled" attribute
      _rowClass = ({rowIndex, data}) => { return Utils.Ui.getDisabledRowClass(data[rowIndex]); };
    }

    return (
      <div className="advanced-table" style={style}>
        {
          !filter && (actions === null || actions.length === 0 || !showRowSelection) && (buttons === null || buttons.length === 0)
          ||
          <Basic.Toolbar container={this} viewportOffsetTop={filterViewportOffsetTop} rendered={showToolbar}>
            <div className="advanced-table-heading">
              <div className="pull-left">
                <Basic.EnumSelectBox
                  onChange={this.onBulkAction.bind(this)}
                  ref="bulkActionSelect"
                  componentSpan=""
                  className={selectedRows.length <= 0 ? 'hidden' : 'bulk-action'}
                  multiSelect={false}
                  options={actions}
                  placeholder={this.i18n('bulk-action.selection' + (selectedRows.length === 0 ? '_empty' : ''), { count: selectedRows.length })}
                  rendered={actions !== null && actions.length > 0 && showRowSelection}
                  searchable={false}/>
              </div>
              <div className="pull-right">
                { buttons }

                <Filter.ToogleButton
                  filterOpen={ (open)=> this.setState({ filterOpened: open }) }
                  filterOpened={ filterOpened }
                  rendered={ showFilter && filter !== undefined && filterCollapsible }
                  style={{ marginLeft: 3 }}/>

                <Basic.Button
                  className="btn-xs"
                  title={ this.i18n('button.refresh') }
                  titlePlacement="bottom"
                  showLoading={ _showLoading }
                  onClick={ this.fetchEntities.bind(this, _searchParameters, this.props) }
                  style={{ marginLeft: 3 }}>
                  <Basic.Icon value="fa:refresh" showLoading={ _showLoading }/>
                </Basic.Button>
              </div>
              <div className="clearfix"></div>
            </div>
            <Basic.Collapse in={filterOpened} rendered={ showFilter }>
              <div>
                { filter }
              </div>
            </Basic.Collapse>
          </Basic.Toolbar>
        }
        {
          _error
          ?
          <Basic.Alert level="warning">
            <div style={{ textAlign: 'center', display: 'none'}}>
              <Basic.Icon value="fa:warning"/>
              {' '}
              { this.i18n('error.load') }
            </div>
            <div style={{ textAlign: 'center' }}>
              <Basic.Button onClick={this.reload.bind(this, this.props)}>
                <Basic.Icon value="fa:refresh"/>
                {' '}
                { this.i18n('button.refresh') }
              </Basic.Button>
            </div>
          </Basic.Alert>
          :
          <div>
            <Basic.BasicTable.Table
              ref="table"
              header={ header }
              data={_entities}
              showLoading={_showLoading || showLoading}
              onRowClick={onRowClick}
              onRowDoubleClick={onRowDoubleClick}
              showRowSelection={showRowSelection}
              selectedRows={selectedRows}
              onRowSelect={this._onRowSelect.bind(this)}
              rowClass={_rowClass}
              condensed={condensed}
              noData={this.getNoData(noData)}>

              {renderedColumns}

              <Basic.Column
                header={ this.i18n('entity.id.label') }
                property="id"
                rendered={ this._showId() }
                className="text-center"
                width={ 100 }
                cell={
                  ({rowIndex, data, property}) => {
                    return (
                      <UuidInfo value={data[rowIndex][property]}/>
                    );
                  }
                }/>
            </Basic.BasicTable.Table>
            <Basic.BasicTable.Pagination
              ref="pagination"
              showPageSize={showPageSize}
              paginationHandler={pagination ? this._handlePagination.bind(this) : null}
              total={ pagination ? _total : _entities.length } {...range} />
          </div>
        }
      </div>
    );
  }
}

AdvancedTable.propTypes = {
  ...Basic.AbstractContextComponent.propTypes,
  /**
   * Table identifier - it's used as key in store
   */
  uiKey: PropTypes.string,
  /**
   * Table Header
   */
  header: PropTypes.oneOfType([PropTypes.string, PropTypes.element]),
  /**
   * EntityManager subclass, which provides data fetching
   */
  manager: PropTypes.object.isRequired,
  /**
   * If pagination is shown
   */
  pagination: PropTypes.bool, // enable paginator action
  /**
   * "Hard filters"
   */
  forceSearchParameters: PropTypes.object,
  /**
   * "Default filters"
   */
  defaultSearchParameters: PropTypes.object,
  /**
   * Callback that is called when a row is clicked.
   */
  onRowClick: PropTypes.func,
  /**
   * Callback that is called when a row is double clicked.
   */
  onRowDoubleClick: PropTypes.func,
  /**
   * Callback that is called when a row is selected.
   */
  onRowSelect: PropTypes.func,
  /**
   * Enable row selection - checkbox in first cell
   */
  showRowSelection: PropTypes.oneOfType([PropTypes.bool, PropTypes.func]),
  /**
   * Shows column with id. Default is id shown in Development stage.
   */
  showId: PropTypes.bool,
  /**
   * selected row indexes as immutable set
   */
  selectedRows: PropTypes.arrayOf(PropTypes.oneOfType([PropTypes.string, PropTypes.number])),
  /**
   * css added to row
   */
  rowClass: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.func
  ]),
  /**
   * Filter definition
   * @type {Filter}
   */
  filter: PropTypes.element,
  /**
   * Show filter
   */
  showFilter: PropTypes.bool,
  /**
   * If filter is opened by default
   */
  filterOpened: PropTypes.bool,
  /**
   * If filter can be collapsed
   */
  filterCollapsible: PropTypes.bool,
  /**
   * When affixed, pixels to offset from top of viewport
   */
  filterViewportOffsetTop: PropTypes.number,
  /**
   * Bulk actions e.g. { value: 'activate', niceLabel: this.i18n('content.identities.action.activate.action'), action: this.onActivate.bind(this) }
   */
  actions: PropTypes.arrayOf(PropTypes.object),
  /**
   * Buttons are shown on the right of toogle filter button
   */
  buttons: PropTypes.arrayOf(PropTypes.element),
  /**
   * If table data is empty, then this text will be shown
   *
   * @type {string}
   */
  noData: PropTypes.oneOfType([PropTypes.string, PropTypes.element]),

  //
  // Private properties, which are used internally for async data fetching
  //

  /**
   * loadinig indicator
   */
  _showLoading: PropTypes.bool,
  _entities: PropTypes.arrayOf(React.PropTypes.object),
  _total: PropTypes.number,
  /**
   * Persisted / used search parameters in redux
   */
  _searchParameters: PropTypes.object,
  showPageSize: PropTypes.bool,
  showToolbar: PropTypes.bool
};
AdvancedTable.defaultProps = {
  ...Basic.AbstractContextComponent.defaultProps,
  _showLoading: true,
  _entities: [],
  _total: null,
  _searchParameters: null,
  _error: null,
  pagination: true,
  showRowSelection: false,
  showFilter: true,
  showId: null,
  selectedRows: [],
  filterCollapsible: true,
  actions: [],
  buttons: [],
  showPageSize: true,
  showToolbar: true
};

function select(state, component) {
  const uiKey = component.manager.resolveUiKey(component.uiKey);
  const ui = state.data.ui[uiKey];
  if (!ui) {
    return {};
  }
  return {
    _showLoading: ui.showLoading,
    _entities: component.manager.getEntities(state, uiKey),
    _total: ui.total,
    _searchParameters: ui.searchParameters,
    _error: ui.error,
  };
}

export default connect(select, null, null, { withRef: true})(AdvancedTable);
