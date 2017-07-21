import React, { PropTypes } from 'react';
import AbstractContextComponent from '../../basic/AbstractContextComponent/AbstractContextComponent';
import * as Basic from '../../basic/index.js';

/**
 * Button for closable filter mainly for advanced table
 *
 * @author Radek Tomiška
 */
export default class ToogleFilterButton extends AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      filterOpened: this.props.filterOpened
    };
  }

  _filterOpen(opened) {
    const { filterOpen } = this.props;
    this.setState({
      filterOpened: opened
    }, () => {
      if (filterOpen) {
        filterOpen(opened);
      }
    });
  }

  render() {
    const { rendered, showLoading, searchParameters, ...others } = this.props;
    const { filterOpened } = this.state;
    if (!rendered) {
      return null;
    }
    let level = 'default';
    let tooltip = this.i18n('component.advanced.Table.filter.empty');
    if (searchParameters.filters.size > 0) {
      level = 'info';
      tooltip = this.i18n('component.advanced.Table.filter.notEmpty');
    }
    //
    return (
      <Basic.Tooltip value={tooltip}>
        <span>
          <Basic.Button
            className="btn-xs"
            level={level}
            onClick={this._filterOpen.bind(this, !filterOpened)}
            showLoading={showLoading}
            icon="filter"
            {...others}>
            { this.i18n('button.filter.toogle') }
            {' '}
            <Basic.Icon icon={!filterOpened ? 'triangle-bottom' : 'triangle-top'} style={{ fontSize: '0.85em'}}/>
          </Basic.Button>
        </span>
      </Basic.Tooltip>
    );
  }
}

ToogleFilterButton.propTypes = {
  ...Basic.AbstractContextComponent.propTypes,
  /**
   * Callback, when filter is opened
   * @type {function} function(bool)
   */
  filterOpen: PropTypes.func,
  /**
   * Filter is opened
   * @type {bool}
   */
  filterOpened: PropTypes.bool,
  /**
   * Used search parameters in redux
   *
   * @type {SearchParameters}
   */
  searchParameters: PropTypes.object
};
ToogleFilterButton.defaultProps = {
  ...Basic.AbstractContextComponent.defaultProps
};
