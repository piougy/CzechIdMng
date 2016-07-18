import React, { PropTypes } from 'react';
import marked from 'marked';
//
import AbstractContextComponent from '../AbstractContextComponent/AbstractContextComponent';
import Icon from '../Icon/Icon';
import Button from '../Button/Button';
import Modal from '../Modal/Modal';
import Tooltip from '../Tooltip/Tooltip';

marked.setOptions({
  renderer: new marked.Renderer(),
  gfm: true,
  tables: true,
  breaks: false,
  pedantic: false,
  sanitize: false,
  smartLists: true,
  smartypants: false
});

export default class HelpIcon extends AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      showModal: false
    };
  }

  close() {
    this.setState({ showModal: false });
  }

  open(event) {
    if (event) {
      event.preventDefault();
    }
    this.setState({ showModal: true });
  }

  getHeaderText(content) {
    let lines = content.split('\n');
    if (lines[0].lastIndexOf('# ', 0) === 0) { // startsWith - this work even in ie
      return lines[0].substr('# '.length);
    }
    return this.i18n('component.basic.HelpIcon.title');
  }

  getContentText(content) {
    let lines = content.split('\n');
    if (lines[0].lastIndexOf('# ', 0) === 0) { // startsWith - this work even in ie
      lines.shift();
      return lines.join('\n');
    }
    return content;
  }

  render() {
    const { content, rendered, showLoading, titlePlacement, ...others } = this.props;
    if (!rendered || !content) {
      return null;
    }
    let headerText = this.getHeaderText(content);
    let markedContent = marked(this.getContentText(content));

    return (
      <span className="help-icon-container" {...others}>
        <Tooltip placement={titlePlacement} id={`button-tooltip`} value={this.i18n('component.basic.HelpIcon.title')}>
          <a href="#" onClick={this.open.bind(this)} className="help-icon">
            <Icon icon="question-sign"/>
          </a>
        </Tooltip>
        <Modal show={this.state.showModal} onHide={this.close.bind(this)} bsSize="large" className="help-icon-modal">
          <Modal.Header closeButton>
            <h1><span dangerouslySetInnerHTML={{ __html: headerText }}/></h1>
          </Modal.Header>
          <Modal.Body className="markdown-body">
            <span dangerouslySetInnerHTML={{ __html: markedContent }}/>
          </Modal.Body>
          <Modal.Footer>
            <Button level="link" onClick={this.close.bind(this)}>Zavřít</Button>
          </Modal.Footer>
        </Modal>
      </span>
    );
  }
}

HelpIcon.propTypes = {
  ...AbstractContextComponent.propTypes,
  content: PropTypes.string,
  /**
   * Help icon title position
   */
  titlePlacement: PropTypes.oneOf(['top', 'bottom', 'right', 'left'])
};

HelpIcon.defaultProps = {
  ...AbstractContextComponent.defaultProps,
  titlePlacement: 'bottom'
};
