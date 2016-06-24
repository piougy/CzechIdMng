'use strict';

import React, { PropTypes } from 'react';
import ReactDOM from 'react-dom';
import { connect } from 'react-redux';
import invariant from 'invariant';
import _ from 'lodash';
import Immutable from 'immutable';
import ReactDropzone from 'react-dropzone';
//
import AbstractContextComponent from '../AbstractContextComponent/AbstractContextComponent';
import Well from '../Well/Well';
import defaultStyle from './styles';

/**
* Dropzone component
*/
class Dropzone extends AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
    this.state = {}
  }


  render() {
    const { data } = this.state;
    const {onDrop, multiple, accept, style, styleActive, styleReject, showLoading, rendered } = this.props;
    if (!rendered){
      return null;
    }
    if (showLoading){
      return  <Well showLoading={true}/>;
    }
    return (
      <div>
        <ReactDropzone ref="dropzone"
          style={style ? style : defaultStyle.style}
          activeStyle={styleActive ? styleActive : defaultStyle.styleActive}
          rejectStyle={styleReject ? styleReject : defaultStyle.styleReject}
          multiple={multiple}
          accept={accept}
          disablePreview={true}
          onDrop={onDrop}>
          <div style={{color: '#777'}}>{this.i18n('component.basic.Dropzone.infoText')}</div>
        </ReactDropzone>
      </div>

    );
  }
}

Dropzone.propTypes = {
  /**
  * Rendered component
  */
  rendered: PropTypes.bool,
  /**
  * Show loading in component
  */
  showLoading: PropTypes.bool,
  /**
  * Function call after droped or selected any files
  */
  onDrop: PropTypes.func.isRequired,
  /**
  * Can be select multiple files
  */
  multiple: PropTypes.bool,
  /**
  * Define accepted file extension
  */
  accept: PropTypes.string,
  /**
  * Object with styles for dropzone
  */
  style: PropTypes.object,
  /**
  * Object with styles for active state (when are files accepted)
  */
  styleActive: PropTypes.object,
  /**
  * Object with styles for reject state (when are files rejected)
  */
  styleReject: PropTypes.object,
}

Dropzone.defaultProps = {
  rendered: true,
  showLoading: false,
  multiple: true
}


export default Dropzone;
