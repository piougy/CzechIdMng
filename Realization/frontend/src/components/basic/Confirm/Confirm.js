'use strict';

import React, { PropTypes } from 'react';
//
import AbstractContextComponent from '../AbstractContextComponent/AbstractContextComponent';
import Modal from '../Modal/Modal';
import Alert from '../Alert/Alert';
import Button from '../Button/Button';

class Confirm extends AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      show: this.props.show
    };
  }

  confirm() {
    let continu = true;
    if (this.state.func){
        continu = this.state.func('confirm',this);
    }
    if (continu){
      this.state.dispatch(true);
      this.closeModal();
    }
  }

  reject() {
    let continu = true;
    if (this.state.func){
        continu = this.state.func('reject',this);
    }
    if (continu){
      this.state.dispatch(false);
      this.closeModal();
    }
  }

  closeModal() {
    this.setState({
      show: false
    });
  }

  show(message, title, func) {
   var promise = new Promise(function(resolve, reject) {
      this.setState({
        dispatch: function (result) {
          if (result){
            resolve('confirmed');
          } else {
            reject('rejected');
          }
        }
      });
    }.bind(this));
    this.setState({
      show: true,
      message: message,
      title: title,
      func: func
    });
    return promise;
  }

  render() {
    const { rendered, showLoading, level } = this.props;
    const { title, message, show } = this.state;
    if (!rendered) {
      return null;
    }

    return (
      <div>
        <Modal show={show} showLoading={showLoading} onHide={this.closeModal.bind(this)}>
          <Modal.Header text={title} rendered={title !== undefined && title !== null} />
          <Modal.Body>
            <span dangerouslySetInnerHTML={{ __html: message }}/>
            {this.props.children}
          </Modal.Body>
          <Modal.Footer>
            <Button level="link" onClick={this.reject.bind(this)}>{this.i18n('button.no')}</Button>
            <Button level={level} onClick={this.confirm.bind(this)}>{this.i18n('button.yes')}</Button>
          </Modal.Footer>
        </Modal>
      </div>
    );
  }
}

Confirm.propTypes = {
  ...AbstractContextComponent.propTypes,
  /**
   * if cvonfirm dialog is shown
   */
  show: PropTypes.bool,
  level: Button.propTypes.level
}

Confirm.defaultProps = {
  ...AbstractContextComponent.defaultProps,
  show: false,
  level: 'success'
}

export default Confirm;
