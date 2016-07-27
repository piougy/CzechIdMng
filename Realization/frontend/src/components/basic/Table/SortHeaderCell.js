import React, { PropTypes } from 'react';
import classnames from 'classnames';
//
import AbstractComponent from '../AbstractComponent/AbstractComponent';
import Icon from '../Icon/Icon';

const ASC = 'ASC';
const DESC = 'DESC';

/**
 * Header with sort action
 */
class SortHeaderCell extends AbstractComponent {

  constructor(props) {
    super(props);
  }

  _activeSort() {
    const { searchParameters, property } = this.props;
    //
    if (!searchParameters) {
      return null;
    }
    const sort = searchParameters.getSort(property);
    if (sort === null) {
      return null;
    }
    return sort === true ? ASC : DESC;
  }

  _handleSort(order, event) {
    if (event) {
      event.preventDefault();
    }
    const { property, sortHandler } = this.props;
    if (!sortHandler) {
      // if handleSort is not set, then its nothing to do
      return null;
    }
    sortHandler(property, order);
  }

  render() {
    const { header, property, sortHandler, showLoading, className } = this.props;
    const active = this._activeSort();
    const title = header || property;
    const classNames = classnames(
      'sort-header-cell',
      className
    );
    return (
      <div className={classNames}>
        {
          sortHandler
          ?
          <a href="#" onClick={this._handleSort.bind(this, active === 'ASC' ? 'DESC' : 'ASC')}>
            {title}
            <SortIcon active={active} showLoading={showLoading}/>
          </a>
          :
          <span>{title}</span>
        }
      </div>
    );
  }
}

SortHeaderCell.propTypes = {
  /**
   * Property for sorting
   */
  property: PropTypes.string.isRequired,
  /**
   * Column header text - if isn't set, then property is shown
   */
  header: PropTypes.string,
  /**
   * Current searchparameters - sort
   */
  searchParameters: React.PropTypes.object,
  /**
   * Callback action for data sorting

   * @param string property
   * @param string order [ASC, DESC]
   */
  sortHandler: React.PropTypes.func,
  /**
   * loadinig indicator
   */
  showLoading: React.PropTypes.bool
};
SortHeaderCell.defaultProps = {
  showLoading: false
};

/**
 * Sort icon
 */
class SortIcon extends AbstractComponent {

  render() {
    const { active, showLoading } = this.props;
    const ascClassName = classnames(
      'sort-icon sort-asc',
      { 'active': active === ASC}
    );
    const descClassName = classnames(
      'sort-icon sort-desc',
      { 'active': active === DESC }
    );

    return (
      <span className="sort-icons">
        {(showLoading && active)
          ?
          <Icon type="fa" icon="refresh" showLoading className="sort-icon active"/>
          :
          <span>
            <Icon icon="triangle-top" className={ascClassName}/>
            <Icon icon="triangle-bottom" className={descClassName}/>
          </span>
        }
      </span>
    );
  }
}

SortIcon.propTypes = {
  /**
   * current order for current property
   */
  active: PropTypes.oneOf([ASC, DESC]),
  /**
   * loadinig indicator
   */
  showLoading: React.PropTypes.bool
};
SortIcon.defaultProps = {
  active: null,
  showLoading: false
};

const SortHeader = ({...props}) => (
  <SortHeaderCell {...props}/>
);

export default SortHeader;
