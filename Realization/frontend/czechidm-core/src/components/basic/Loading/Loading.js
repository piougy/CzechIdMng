import React from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import $ from 'jquery';
import ReactResizeDetector from 'react-resize-detector';
//
import AbstractComponent from '../AbstractComponent/AbstractComponent';

/**
 * Loading indicator.
 *
 * ! Be careful: prevent to use Basic.Div inside => cicrular reference.
 *
 * @author Radek Tomiška
 */
class Loading extends AbstractComponent {

  constructor(props, context) {
    super(props, context);
    //
    this.containerRef = React.createRef();
  }

  componentDidMount() {
    this._resize();
  }

  componentDidUpdate() {
    this._resize();
  }

  _showLoading() {
    const { showLoading, show } = this.props;
    //
    return showLoading || show;
  }

  _resize() {
    const showLoading = this._showLoading();
    if (!showLoading) {
      return;
    }
    const panel = $(this.containerRef.current);
    const loading = panel.find('.loading');
    if (loading.hasClass('global') || loading.hasClass('static')) {
      // we don't want resize loading container
      return;
    }
    // TODO: offset, scroll
    loading.css({
      top: panel.position().top,
      left: panel.position().left,
      width: panel.width(),
      height: panel.height()
    });
  }

  render() {
    const {
      rendered,
      className,
      containerClassName,
      showAnimation,
      isStatic,
      loadingTitle,
      style,
      containerTitle,
      onClick,
      ...others
    } = this.props;
    //
    if (!rendered) {
      return null;
    }
    const showLoading = this._showLoading();
    //
    // Loading is used as standard div => wee need to render css even if loading is not active
    const _containerClassNames = classNames(
      'loader-container',
      containerClassName
    );
    const loaderClassNames = classNames(
      className,
      'loading',
      { hidden: !showLoading },
      { static: isStatic }
    );
    // onClick required props
    others.onClick = onClick;
    others.tabIndex = others.tabIndex || (onClick ? 0 : null);
    others.role = others.role || (onClick ? 'button' : null);
    //
    return (
      <div
        ref={ this.containerRef }
        className={ _containerClassNames }
        style={ style }
        title={ containerTitle }
        { ...others }>
        {
          showLoading
          ?
          <div className={ loaderClassNames }>
            <div className="loading-wave-top" />
            {
              showAnimation
              ?
              <div className="loading-wave-container" title={ loadingTitle }>
                <div className="loading-wave hidden">
                  <div/><div/><div/><div/><div/>
                </div>
                <div className="loading-logo">
                  <div/><div/><div/><div/><div/><div/><div/><div/><div/>
                </div>
              </div>
              :
              null
            }
            <div className="title hidden">{ loadingTitle }</div>
          </div>
          :
          null
        }
        { this.props.children }
      </div>
    );
  }
}

class ResizeLoading extends AbstractComponent {
  render() {
    const { rendered, ...others } = this.props;
    if (!rendered) {
      return null;
    }
    //
    return (
      <ReactResizeDetector
        handleHeight
        render={ ({ height }) => (
          <Loading height={ height } { ...others } />
        )}/>
    );
  }
}

Loading.propTypes = {
  ...AbstractComponent.propTypes,
  /**
   * Shows loading overlay (showLoadin alias)
   */
  show: PropTypes.bool,
  /**
   * when loading is visible, then show animation too
   */
  showAnimation: PropTypes.bool,
  /**
   * static loading without overlay
   */
  isStatic: PropTypes.bool,
  /**
   * Loading title
   */
  loadingTitle: PropTypes.string,
  /**
   * Title - static container (div wrapper).
   */
  containerTitle: PropTypes.string,
  /**
   * Css - static container (div wrapper).
   */
  containerClassName: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.func
  ])
};
Loading.defaultProps = {
  ...AbstractComponent.defaultProps,
  show: false,
  showAnimation: true,
  isStatic: false,
  loadingTitle: 'Zpracovávám ...' // TODO: localization or undefined ?
};

export default ResizeLoading;
