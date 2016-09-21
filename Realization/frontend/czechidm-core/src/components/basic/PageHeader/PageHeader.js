import React from 'react';
import classnames from 'classnames';
//
import AbstractComponent from '../AbstractComponent/AbstractComponent';
import Icon from '../Icon/Icon';

/**
 * Page header
 */
class PageHeader extends AbstractComponent {

  constructor(props) {
    super(props);
  }

  render() {
    const { rendered, showLoading, children, className, text, ...others } = this.props;
    if (!rendered) {
      return null;
    }

    const classNames = classnames(
      'page-header',
      className
    );
    return (
      <div className={classNames} {...others}>
        <h1>
          {
            showLoading
            ?
            <Icon type="fa" icon="refresh" showLoading className="icon-loading"/>
            :
            <span>
              {text}
              {children}
            </span>
          }
        </h1>
      </div>
    );
  }
}

PageHeader.propTypes = {
  ...AbstractComponent.propTypes
};

PageHeader.defaultProps = {
  ...AbstractComponent.defaultProps
};

export default PageHeader;
