import React, { PropTypes } from 'react';
import classnames from 'classnames';
//
import AbstractComponent from '../AbstractComponent/AbstractComponent';
import Icon from '../Icon/Icon';
import HelpIcon from '../HelpIcon/HelpIcon';

/**
 * Content header
 */
class ContentHeader extends AbstractComponent {

  constructor(props) {
    super(props);
  }

  render() {
    const { rendered, showLoading, children, className, help, text, ...others } = this.props;
    if (!rendered) {
      return null;
    }

    const classNames = classnames(
      'content-header',
      className
    );
    return (
      <div className={classNames} {...others}>
        <div className="pull-left">
          <h2>
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
          </h2>
        </div>
        {
          help
          ?
          <div className="pull-right">
            <HelpIcon content={help}/>
          </div>
          :
          null
        }
        <div className="clearfix"></div>
      </div>
    );
  }
}

ContentHeader.propTypes = {
  ...AbstractComponent.propTypes,
  /**
   * help content
   * @type {[type]}
   */
  help: PropTypes.string
};

ContentHeader.defaultProps = {
  ...AbstractComponent.defaultProps
};

export default ContentHeader;
