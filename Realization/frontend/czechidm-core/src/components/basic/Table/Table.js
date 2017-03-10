import React, { PropTypes } from 'react';
import invariant from 'invariant';
import Immutable from 'immutable';
import _ from 'lodash';
//
import AbstractComponent from '../AbstractComponent/AbstractComponent';
import Loading from '../Loading/Loading';
import Alert from '../Alert/Alert';
import Row from './Row';
import DefaultCell from './DefaultCell';
import classNames from 'classnames';

const HEADER = 'header';
// const FOOTER = 'footer';
const CELL = 'cell';

/**
 * Table component with header and columns.
 *
 * @author Radek Tomiška
 */
class Table extends AbstractComponent {

  constructor(props) {
    super(props);
    this.state = {
      selectedRows: this.props.selectedRows ? new Immutable.Set(this.props.selectedRows) : new Immutable.Set()
    };
  }

  componentWillReceiveProps(nextProps) {
    this.setState({
      selectedRows: nextProps.selectedRows ? new Immutable.Set(nextProps.selectedRows) : new Immutable.Set()
    });
  }

  _resolveColumns() {
    const children = [];
    //
    if (this.props.children) {
      React.Children.forEach(this.props.children, (child) => {
        if (child == null) {
          return;
        }
        invariant(
          // child.type.__TableColumnGroup__ ||
          !child.type || child.type.__TableColumn__,
          'child type should be <TableColumn /> or ' +
          '<TableColumnGroup />'
        );
        // rendered columns only
        if (child.props.rendered) {
          children.push(child);
        }
      });
      return children;
    }
    //
    // if columns aren't specified, then resolve columns from given data object
    if (children.length === 0) {
      let properties = [];
      const { data } = this.props;
      if (data && data.length !== 0) {
        // we can not use just first object, because first row could not contain all properties filled
        data.map(row => {
          for (const property in row) {
            if (!row.hasOwnProperty(property)) {
              continue;
            }
            properties = this._appendProperty(properties, property, row[property], '');
          }
        });
      }
      properties.map(property => {
        children.push(
          <DefaultCell property={property} data={data}/>
        );
      });
    }
    return children;
  }

  /**
   * Appends property to given properties array - supports nested properties
   *
   * @param  {array[string]} properties
   * @param  {string property
   * @param  {object} propertyValue
   * @param  {string} propertyPrefix nested property prefix
   * @return {array} new properties
   */
  _appendProperty(properties, property, propertyValue, propertyPrefix) {
    if (properties.indexOf(propertyPrefix + property) === -1) {
      if (propertyValue && _.isObject(propertyValue)) { // nested property
        // remove nested property prefix - is nested object
        if (propertyPrefix) {
          properties = properties.filter(p => { return p !== propertyPrefix; });
        }
        // recursion
        for (const nestedProperty in propertyValue) {
          if (!propertyValue.hasOwnProperty(nestedProperty)) {
            continue;
          }
          properties = this._appendProperty(properties, nestedProperty, propertyValue[nestedProperty], propertyPrefix + property + '.');
        }
      } else {
        properties.push(propertyPrefix + property);
      }
    }
    return properties;
  }

  _selectColumnElement(columns, type) {
    const newColumns = [];
    for (let i = 0; i < columns.length; ++i) {
      const column = columns[i];
      newColumns.push(React.cloneElement(
        column,
        {
          cell: type ? column.props[type] : column.props[CELL]
        }
      ));
    }
    return newColumns;
  }

  _showRowSelection({ rowIndex, data, showRowSelection }) {
    if (typeof showRowSelection === 'function') {
      return showRowSelection({
        rowIndex,
        data
      });
    }
    return showRowSelection;
  }

  selectRow(rowIndex, selected) {
    let newSelectedRows;
    if (rowIndex !== undefined && rowIndex !== null && rowIndex > -1) {
      const recordId = this.getIdentifier(rowIndex);
      newSelectedRows = (selected ? this.state.selectedRows.add(recordId) : this.state.selectedRows.remove(recordId));
    } else { // de/select all
      newSelectedRows = this.state.selectedRows;
      const { data } = this.props;
      //
      for (let i = 0; i < data.length; i++) {
        if (this._showRowSelection({ ...this.props, rowIndex: i })) {
          if (selected) {
            newSelectedRows = newSelectedRows.add(this.getIdentifier(i));
          } else {
            newSelectedRows = newSelectedRows.remove(this.getIdentifier(i));
          }
        }
      }
    }
    this.setState({
      selectedRows: newSelectedRows
    }, this._onRowSelect(rowIndex, selected, newSelectedRows.toArray()));
  }

  /**
   * Clears row selection
   */
  clearSelectedRows() {
    this.setState({
      selectedRows: this.state.selectedRows.clear()
    });
  }

  _onRowSelect(rowIndex, selected, selection) {
    const { onRowSelect } = this.props;
    if (!onRowSelect) {
      return;
    }
    onRowSelect(rowIndex, selected, selection);
  }

  getSelectedRows() {
    return this.state.selectedRows.toArray();
  }

  _isAllRowsSelected() {
    const { data } = this.props;
    const { selectedRows } = this.state;
    for (let i = 0; i < data.length; i++) {
      if (!selectedRows.has(this.getIdentifier(i))) {
        return false;
      }
    }
    return true;
  }

  getIdentifierProperty() {
    // TODO: support for custom property?
    return 'id';
  }

  getIdentifier(rowIndex) {
    const { data } = this.props;
    return data[rowIndex][this.getIdentifierProperty()];
  }

  renderHeader(columns) {
    const { showLoading, showRowSelection } = this.props;
    const headerColumns = this._selectColumnElement(columns, HEADER);
    return (
      <thead>
        <Row
          columns={headerColumns}
          rowIndex={-1}
          showLoading={showLoading}
          showRowSelection={showRowSelection}
          onRowSelect={showRowSelection ? this.selectRow.bind(this) : null}
          selected={this._isAllRowsSelected()}/>
      </thead>
    );
  }

  renderBody(columns) {
    const { data } = this.props;
    if (!data || data.length === 0) {
      return null;
    }
    const rows = [];
    for (let i = 0; i < data.length; i++) {
      rows.push(this.renderRow(columns, i));
    }
    return (
      <tbody>
        { rows }
      </tbody>
    );
  }

  renderRow(columns, rowIndex) {
    const { onRowClick, onRowDoubleClick, showRowSelection, rowClass } = this.props;
    const key = 'row_' + rowIndex;
    return (
       <Row
         key={key}
         data={this.props.data}
         columns={columns}
         rowIndex={rowIndex}
         showRowSelection={showRowSelection}
         onRowSelect={showRowSelection ? this.selectRow.bind(this) : null}
         selected={this.state.selectedRows.has(this.getIdentifier(rowIndex))}
         onClick={onRowClick}
         onDoubleClick={onRowDoubleClick}
         rowClass={rowClass}/>
    );
  }

  renderFooter() {
    return null;
  }

  render() {
    const { data, noData, rendered, showLoading, hover, className, classNameBasicTable } = this.props;
    if (!rendered) {
      return null;
    }

    if (!data || data.length === 0) {
      // TODO: colspan with table header will be better - column definition are needed in this variant
      if (this.props.showLoading) {
        return (
          <div className="basic-table">
            <Loading showLoading className="static"/>
          </div>
        );
      }
      return (
        <div className="basic-table">
          <Alert text={noData}/>
        </div>
      );
    }

    const columns = this._resolveColumns();
    const header = this.renderHeader(columns);
    const body = this.renderBody(columns);
    const footer = this.renderFooter();
    const classNamesTable = classNames(
      className,
      { 'table': true },
      { 'table-hover': hover}
    );
    const classNamesBasicTable = classNames(
      classNameBasicTable,
      'basic-table'
    );

    return (
      <div className={classNamesBasicTable}>
        <Loading showLoading={showLoading}>
          <table className={classNamesTable}>
            { header }
            { body }
            { footer }
          </table>
        </Loading>
      </div>
    );
  }
}

Table.propTypes = {
  ...AbstractComponent.propTypes,
  /**
   * input data as array of json objects
   */
  data: PropTypes.array,
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
   * selected row indexes as immutable set
   */
  selectedRows: PropTypes.arrayOf(PropTypes.oneOfType([PropTypes.string, PropTypes.number])),
  /**
   * Enable row selection - checkbox in first cell
   */
  showRowSelection: PropTypes.oneOfType([PropTypes.bool, PropTypes.func]),
  /**
   * css added to row
   */
  rowClass: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.func
  ]),
  /**
   * If table data is empty, then this text will be shown
   *
   * @type {string}
   */
  noData: PropTypes.oneOfType([PropTypes.string, PropTypes.element])
};
Table.defaultProps = {
  ...AbstractComponent.defaultProps,
  data: [],
  selectedRows: [],
  showRowSelection: false,
  noData: 'No record found',
  hover: true
};

export default Table;
