import React, { PropTypes } from 'react';
import ReactDOM from 'react-dom';
import classNames from 'classnames';
//
import AbstractComponent from '../AbstractComponent/AbstractComponent';

class Loading extends AbstractComponent {

  constructor(props, context) {
    super(props, context);
  }

  _resize() {
    const { showLoading } = this.props;
    if (!showLoading) {
      return;
    }
    if (typeof $ !== 'undefined') {
      const panel = $(ReactDOM.findDOMNode(this.refs.container));
      const loading = panel.find('.loading');
      if (loading.hasClass('global') || loading.hasClass('static')) {
        // we don't want resize loading container
        return;
      }
      loading.css({
        top: panel.position().top + 1, // TODO: check, if panel contains top header and calculate with header height (now 50 hardcoded)
        left: panel.position().left + 1,
        width: panel.width() - 1,
        height: panel.height() - 1
      });
    }
  }

  componentDidMount() {
    this._resize();
  }

  componentDidUpdate() {
    this._resize();
  }

  render() {
    const { rendered, className, showLoading, showAnimation, isStatic, loadingTitle, ...others } = this.props;
    if (!rendered) {
      return null;
    }
    const loaderClassNames = classNames(
      className,
      'loading',
      { 'hidden': !showLoading },
      { 'static': isStatic }
    );
    return (
      <div ref="container" className="loader-container" {...others}>
        {
          showLoading
          ?
          <div className={loaderClassNames}>
            <div className="loading-wave-top"></div>
            {showAnimation
              ?
              <div className="loading-wave-container" title={loadingTitle}>
                <div className="loading-wave">
                  <div></div><div></div><div></div><div></div><div></div>
                </div>
              </div>
              :
              null
            }
            <div className="title hidden">{loadingTitle}</div>
          </div>
          :
          null
        }
        {this.props.children}
      </div>
    );
  }
}

Loading.propTypes = {
  ...AbstractComponent.propTypes,
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
  loadingTitle: PropTypes.string
};
Loading.defaultProps = {
  ...AbstractComponent.defaultProps,
  showAnimation: true,
  isStatic: false,
  loadingTitle: 'Zpracovávám ...' // TODO: localization or undefined ?
};

export default Loading;
