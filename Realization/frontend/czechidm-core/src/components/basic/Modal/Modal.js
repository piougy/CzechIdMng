import React from 'react';
import ReactDOM from 'react-dom';
import PropTypes from 'prop-types';
import { Modal } from 'react-bootstrap';
import _ from 'lodash';
import ReactResizeDetector from 'react-resize-detector';
//
import AbstractComponent from '../AbstractComponent/AbstractComponent';
import Loading from '../Loading/Loading';
import HelpIcon from '../HelpIcon/HelpIcon';
import Icon from '../Icon/Icon';
import Div from '../Div/Div';

const SUPPORTED_SIZES = ['lg', 'large', 'sm', 'small'];

/**
 * Wrapped bootstrap modal
 * - adds backdrop fix
 *
 * https://github.com/react-bootstrap/react-bootstrap/blob/v0.28.3/src/Modal.js
 * https://react-bootstrap.github.io/components.html#modals
 *
 * @author Radek Tomiška
 */
export default class BasicModal extends AbstractComponent {

  constructor(props, context) {
    super(props, context);
    //
    this.modalRef = React.createRef();
    //
    this.state = {
      bodyStyle: {}
    };
  }

  /**
   * Add event listener
   */
  componentDidMount() {
    const { affixFooter } = this.props;
    if (!affixFooter) {
      // affix footer is disabled
      return;
    }
    window.addEventListener('resize', this._setFooterStyle.bind(this, null, null));
  }

  /**
   * Remove event listener
   */
  componentWillUnmount() {
    const { affixFooter } = this.props;
    if (!affixFooter) {
      // affix footer is disabled
      return;
    }
    window.removeEventListener('resize', this._setFooterStyle.bind(this, null, null));
  }

  /**
   * Fix modal backdrop size
   */
  _onEnter(onEnter) {
    // find modal-backdrop
    if (typeof $ !== 'undefined') {
      $('.modal-backdrop').css({
        bottom: 0 - $(window).scrollTop()
      });
      this._setFooterStyle(onEnter);
    } else if (onEnter) {
      // by props
      onEnter();
    }
  }

  _onExit(onExit) {
    this.setState({
      bodyStyle: {}
    }, () => {
      // original
      if (onExit) {
        onExit();
      }
    });
  }

  _setFooterStyle(cb, event) {
    const { affixFooter } = this.props;
    if (!affixFooter) {
      // affix footer is disabled
      return;
    }
    //
    // TODO: Using of findDOMNode is not recommended. Find a another solution.
    /* eslint-disable react/no-find-dom-node */
    const modal = $(ReactDOM.findDOMNode(this.modalRef.current));
    const modalDialog = modal.find('.modal-dialog');
    const modalFooter = modal.find('.modal-footer');
    //
    // single footer can be affixed only
    if (modalFooter.length !== 1) {
      return;
    }
    //
    // inner scroll event (e.g. on txt area)
    if (event && !$(event.target).hasClass('modal')) {
      return;
    }
    //
    const heightDifference = modalDialog.height() - $(window).height();
    const footerBottom = heightDifference - (event ? event.target.scrollTop : 0);
    const footerHeight = modalFooter.outerHeight();
    const modalMargin = (parseInt(modalDialog.css('margin-bottom'), 10) - 1) || 29; // FIXME: -1 => border bottom
    const _fixedBottom = footerBottom +
      (modalMargin - (footerBottom < -modalMargin ? (footerBottom + modalMargin) : 0)); // on end -> between dialog margin
    const { fixedBottom } = this.state;
    //
    if (fixedBottom === _fixedBottom) {
      // => no change
      return;
    }
    //
    // console.log('_fixedBottom', _fixedBottom);
    this.setState({
      fixedBottom: _fixedBottom,
      bodyStyle: {
        paddingBottom: footerHeight
      }
    }, () => {
      modalFooter.css({
        position: 'absolute',
        bottom: _fixedBottom,
        backgroundColor: 'white',
        width: '100%',
        borderRadius: '0px 0px 6px 6px', // FIXME: by modal radius
        zIndex: 3 // confidential, validations etc. uses 2
      });
      if (cb) {
        cb();
      }
    });
  }

  /**
   * Resize modal body ~ content
   */
  onResize() {
    this._setFooterStyle(null, null);
  }

  render() {
    const {
      rendered,
      bsSize,
      showLoading,
      onEnter,
      onExit,
      enforceFocus,
      container,
      affixFooter,
      ...others
    } = this.props;
    const { bodyStyle } = this.state;
    //
    if (!rendered) {
      return null;
    }
    if (bsSize && SUPPORTED_SIZES.indexOf(bsSize) > -1) { // workaround for ugly Modal warning, when bsSize lack default
      others.bsSize = bsSize;
    }
    // disabled enforceFocus - input in popover cannot be selected otherwise
    return (
      <Modal
        ref={ this.modalRef }
        onScroll={ affixFooter ? this._setFooterStyle.bind(this, null) : null }
        onEnter={ this._onEnter.bind(this, onEnter) }
        onExit={ this._onExit.bind(this, onExit) }
        enforceFocus={ enforceFocus }
        container={ container }
        { ...others }>
        {
          showLoading
          ?
          <Modal.Body>
            <Loading isStatic showLoading/>
          </Modal.Body>
          :
          null
        }
        {/* prevent exception, when parent component is touching to childerns ref etc.*/}
        <Div
          style={ bodyStyle }
          className={ showLoading ? 'hidden' : '' }>
          {
            !affixFooter
            ||
            <ReactResizeDetector handleHeight onResize={ this.onResize.bind(this) } />
          }
          { this.props.children }
        </Div>
      </Modal>
    );
  }
}

BasicModal.propTypes = {
  ...AbstractComponent.propTypes,
  /**
   * Callback fired before the Modal transitions in
   */
  onEnter: PropTypes.func,
  /**
   * Callback fired after the Modal transitions out
   */
  onExit: PropTypes.func,
  /**
   * Component size variations.
   */
  bsSize: PropTypes.oneOf(_.concat(SUPPORTED_SIZES, 'default')),
  /**
   * Footer (~with buttons) will be affixed on the bottom.
   */
  affixFooter: PropTypes.bool
  /**
   * ... and other react bootstap modal props
   */
};

BasicModal.defaultProps = {
  ...AbstractComponent.defaultProps,
  bsSize: 'default',
  enforceFocus: false,
  affixFooter: true
};

/**
 * Modal header
 *
 * @author Radek Tomiška
 */
class BasicModalHeader extends AbstractComponent {
  render() {
    const { rendered, text, children, help, showLoading, icon, buttons, ...others } = this.props;
    if (!rendered) {
      return null;
    }
    return (
      <Modal.Header { ...others }>
        <Div style={{ display: 'flex', alignItems: 'center' }}>
          <Icon type="fa" icon="refresh" showLoading rendered={ showLoading } />
          <Div style={{ flex: 1 }}>
            {
              showLoading || text
              ?
              <h2>
                <Icon value={ icon } showLoading={ showLoading } style={{ marginRight: 5 }}/>
                {
                  React.isValidElement(text)
                  ?
                  text
                  :
                  <span dangerouslySetInnerHTML={{ __html: text }}/>
                }
              </h2>
              :
              null
            }
            { children }
          </Div>
          { buttons }
          <HelpIcon content={ help }/>
        </Div>

      </Modal.Header>
    );
  }
}

BasicModalHeader.propTypes = {
  ...AbstractComponent.propTypes,
  /**
   * Header text.
   */
  text: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.node
  ])
};

BasicModalHeader.defaultProps = {
  ...AbstractComponent.defaultProps
};

BasicModal.Header = BasicModalHeader;
BasicModal.Body = Modal.Body;
BasicModal.Footer = Modal.Footer;
