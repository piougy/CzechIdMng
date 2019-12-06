import React from 'react';
import PropTypes from 'prop-types';
import classnames from 'classnames';
//
import AbstractComponent from '../AbstractComponent/AbstractComponent';
import Icon from '../Icon/Icon';
import HelpIcon from '../HelpIcon/HelpIcon';
import Loading from '../Loading/Loading';

/**
 * Basic panel decorator
 *
 * @author Radek Tomiška
 */
export class Panel extends AbstractComponent {

  render() {
    const { className, rendered, showLoading, level, style } = this.props;
    if (rendered === null || rendered === undefined || rendered === '' || rendered === false) {
      return null;
    }
    const classNames = classnames(
      'panel',
      `panel-${ level }`,
      className
    );
    return (
      <div className={classNames} style={style}>
        <Loading showLoading={showLoading}>
          {this.props.children}
        </Loading>
      </div>
    );
  }
}

Panel.propTypes = {
  ...AbstractComponent.propTypes,
  /**
   * Panel level / css / class
   */
  level: PropTypes.oneOf(['default', 'success', 'warning', 'info', 'danger', 'primary'])
};
Panel.defaultProps = {
  ...AbstractComponent.defaultProps,
  level: 'default'
};

export class PanelHeader extends AbstractComponent {

  render() {
    const { className, rendered, showLoading, text, help, children, style, buttons } = this.props;
    if (rendered === null || rendered === undefined || rendered === '' || rendered === false) {
      return null;
    }
    const classNames = classnames(
      'panel-heading',
      className
    );

    return (
      <div className={classNames} style={style}>
        <div className={ help || buttons ? 'pull-left' : null}>
          <Icon type="fa" icon="refresh" showLoading rendered={ showLoading }/>
          {
            showLoading
            ||
            text
            ?
            <h2>{text}</h2>
            :
            null
          }
          {children}
        </div>
        {
          !buttons
          ||
          <div className="pull-right">
            { buttons }
          </div>
        }
        {
          help
          ?
          <div className="pull-right">
            <HelpIcon content={ help }/>
          </div>
          :
          null
        }
        <div className="clearfix" />
      </div>
    );
  }
}

PanelHeader.propTypes = {
  ...AbstractComponent.propTypes,
  /**
   * Header text
   */
  text: PropTypes.any,
  /**
   * link to help
   */
  help: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.object
  ])
};
PanelHeader.defaultProps = {
  ...AbstractComponent.defaultProps
};

export class PanelBody extends AbstractComponent {

  render() {
    const { className, rendered, showLoading, style } = this.props;
    if (rendered === null || rendered === undefined || rendered === '' || rendered === false) {
      return null;
    }
    const classNames = classnames(
      'panel-body',
      className
    );

    return (
      <div className={classNames} style={style}>
        <Loading showLoading={showLoading}>
          {this.props.children}
        </Loading>
      </div>
    );
  }
}

PanelBody.propTypes = {
  ...AbstractComponent.propTypes
};
PanelBody.defaultProps = {
  ...AbstractComponent.defaultProps
};


export class PanelFooter extends AbstractComponent {

  render() {
    const { rendered, className, showLoading, style } = this.props;
    if (rendered === null || rendered === undefined || rendered === '' || rendered === false) {
      return null;
    }
    const classNames = classnames(
      'panel-footer',
      className
    );

    return (
      <div className={classNames} style={style}>
        <Loading className="simple" showLoading={showLoading} showAnimation={false}>
          {this.props.children}
        </Loading>
      </div>
    );
  }
}

PanelFooter.propTypes = {
  ...AbstractComponent.propTypes
};
PanelFooter.defaultProps = {
  ...AbstractComponent.defaultProps
};
