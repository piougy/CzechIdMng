import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import invariant from 'invariant';
import _ from 'lodash';
//
import * as Basic from '../../basic';
import Filter from '../Filter/Filter';

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

  componentDidMount() {
    this.reload();
  }

  reload() {
    const { rendered, _searchParameters } = this.props;
    if (!rendered) {
      return;
    }
    // we have to se first page
    // TODO: we have to se first page only if fitlers changes - handle in next
    let _sp = _searchParameters;
    if (_sp) {
      _sp = _sp.setPage(0);
    }
    this.fetchEntities(_sp);
  }

  /**
   * Merge hard, default and user deffined search parameters
   */
  _mergeSearchParameters(searchParameters) {
    const { defaultSearchParameters, forceSearchParameters, manager } = this.props;
    let _forceSearchParameters = null;
    if (forceSearchParameters) {
      _forceSearchParameters = forceSearchParameters.setSize(null).setPage(null); // we dont want override setted pagination
    }
    return manager.mergeSearchParameters(searchParameters || defaultSearchParameters || manager.getDefaultSearchParameters(), _forceSearchParameters);
  }

  fetchEntities(searchParameters) {
    const { uiKey, manager } = this.props;
    searchParameters = this._mergeSearchParameters(searchParameters);
    this.context.store.dispatch(manager.fetchEntities(searchParameters, uiKey, (json, error) => {
      if (error) {
        this.addErrorMessage({
          level: 'error',
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

  useFilterData(formData) {
    const { _searchParameters } = this.props;
    let userSearchParameters = _searchParameters;
    userSearchParameters = userSearchParameters.setPage(0);
    for (const value in formData) {
      if (!formData.hasOwnProperty(value)) {
        continue;
      }
      if (!formData[value]) {
        userSearchParameters = userSearchParameters.clearFilter(value);
      } else {
        userSearchParameters = userSearchParameters.setFilter(value, formData[value]);
      }
      this.fetchEntities(userSearchParameters);
    }
  }

  useFilterForm(filterForm) {
    const { _searchParameters } = this.props;
    //
    const filters = [];
    const filterValues = filterForm.getData();
    for (const property in filterValues) {
      if (!filterValues.hasOwnProperty(property)) {
        continue;
      }
      const filterComponent = filterForm.getComponent(property);
      /*
      let relation = filterComponent.props.relation;
      if (!relation) {
        if (filterComponent.props.enum) { // enumeration
          relation = Filter.DEFAUT_ENUM_RELATION;
        }
        relation = Filter.DEFAUT_RELATION;
      }*/
      const field = filterComponent.props.field || property;
      //
      const filter = {
        field,
        // relation: relation
      };
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
      filter.value = filledValue;
      // }
      filters.push(filter);
    }
    let userSearchParameters = _searchParameters;
    userSearchParameters = userSearchParameters.setPage(0);
    filters.forEach(filter => {
      if (!filter.value) {
        userSearchParameters = userSearchParameters.clearFilter(filter.field);
      } else {
        userSearchParameters = userSearchParameters.setFilter(filter.field, filter.value);
      }
    });
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
      filterForm.getComponent(property).setState(
        { value: null }
      );
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
      this.addMessage({ level: 'info', message: 'Omlouváme se, tato operace nebyla prozatím naimplementována.' });
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
      filterCollapsible,
      filterViewportOffsetTop,
      actions,
      buttons
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
      let header = column.props.header;
      if (!header && column.props.property) {
        header = this.i18n('entity.' + manager.getEntityType() + '.' + column.props.property);
      }
      if (column.props.sort) {
        header = (
          <Basic.BasicTable.SortHeaderCell
            header={header}
            sortHandler={this._handleSort.bind(this)}
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
          <Basic.BasicTable.LinkCell to={column.props.to} {...commonProps}/>
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
          header={header}
          cell={cell}/>
      );
    }

    return (
      <div className="advanced-table">
        {
          !filter && (actions === null || actions.length === 0 || !showRowSelection)
          ||
          <Basic.Toolbar container={this} viewportOffsetTop={filterViewportOffsetTop}>
            <div className="pull-left">
              <Basic.EnumSelectBox
                onChange={this.onBulkAction.bind(this)}
                ref="anySelectBoxMulti"
                componentSpan=""
                className={selectedRows <= 0 ? 'hidden' : 'bulk-action'}
                multiSelect={false}
                options={actions}
                placeholder={this.i18n('component.advanced.Table.bulk-action.selection' + (selectedRows.length === 0 ? '_empty' : ''), { count: selectedRows.length })}
                readOnly={selectedRows <= 0}
                rendered={actions !== null && actions.length > 0 && showRowSelection}/>
            </div>
            <div className="pull-right">
              { buttons }
              {' '}
              <Filter.ToogleButton filterOpen={ (open)=> this.setState({ filterOpened: open }) } filterOpened={filterOpened} rendered={filter !== undefined && filterCollapsible} />
            </div>
            <div className="clearfix"></div>
            <Basic.Collapse in={filterOpened}>
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
              { this.i18n('component.advanced.Table.error.load') }
            </div>
            <div style={{ textAlign: 'center' }}>
              <Basic.Button onClick={this.reload.bind(this)}>
                <Basic.Icon value="fa:refresh"/>
                {' '}
                { this.i18n('component.advanced.Table.button.refresh') }
              </Basic.Button>
            </div>
          </Basic.Alert>
          :
          <div>
            <Basic.BasicTable.Table
              ref="table"
              data={_entities}
              showLoading={_showLoading || showLoading}
              onRowClick={onRowClick}
              onRowDoubleClick={onRowDoubleClick}
              showRowSelection={showRowSelection}
              selectedRows={selectedRows}
              onRowSelect={this._onRowSelect.bind(this)}
              rowClass={rowClass}>
              {renderedColumns}
            </Basic.BasicTable.Table>
            <Basic.BasicTable.Pagination
              ref="pagination"
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
  showRowSelection: PropTypes.bool,
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
   * Bulk actions e.g. { value: 'activate', niceLabel: this.i18n('content.users.action.activate.action'), action: this.onActivate.bind(this) }
   */
  actions: PropTypes.arrayOf(PropTypes.object),
  /**
   * Buttons are shown on the right of toogle filter button
   */
  buttons: PropTypes.arrayOf(PropTypes.element),

  //
  // Private properties, which are used internally for async data fetching
  //

  /**
   * loadinig indicator
   */
  _showLoading: PropTypes.bool,
  _entities: PropTypes.arrayOf(React.PropTypes.object),
  _total: PropTypes.number,
  _searchParameters: PropTypes.object
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
  selectedRows: [],
  filterCollapsible: true,
  actions: []
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
