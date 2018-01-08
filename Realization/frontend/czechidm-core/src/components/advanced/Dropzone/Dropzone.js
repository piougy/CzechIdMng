import React from 'react';
//
import * as Basic from '../../basic';

/**
* Advanced Dropzone component
* - adds support for prepare droped files before submitting - use 'getFiles' method.
*
* TODO: use form component super class?
* TODO: clear button
* TODO: prop for replacing / appending dropped files - replace is implemented now
*
* @author Radek Tomiška
* @since 7.7.0
*/
class Dropzone extends Basic.AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      files: []
    };
  }

  /**
   * Dropzone component function called after select file
   * @param  {array} files Array of selected files
   */
  _onDrop(files) {
    this.setState({
      files
    }, () => {
      // external onDrop
      const { onDrop } = this.props;
      if (onDrop) {
        onDrop(files);
      }
    });
  }

  /**
   * Returns selected files
   *
   * @return {arrayOf(file)} selected files
   */
  getFiles() {
    return this.state.files;
  }

  /**
   * Returns one of the selected files (use, when multiple files cannot be selected).
   *
   * @return {file} selected file
   */
  getFile() {
    const files = this.state.files;
    if (!files || files.length === 0) {
      return null;
    }
    return files[0];
  }

  render() {
    const { rendered, onDrop, children, ...others } = this.props;
    const { files } = this.state;
    //
    if (!rendered) {
      return null;
    }
    //
    return (
      <Basic.Dropzone
        onDrop={ this._onDrop.bind(this) }
        {...others}>
        {
          !files || files.length === 0
          ?
          children
          :
          files.map(file => {
            return file.name;
          })
          .join(', ')
        }
      </Basic.Dropzone>
    );
  }
}

Dropzone.propTypes = {
  ...Basic.Dropzone.propTypes
};

Dropzone.defaultProps = {
  ...Basic.Dropzone.defaultProps
};

export default Dropzone;
