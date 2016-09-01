import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import * as Basic from '../../../../components/basic';
import { WorkflowProcessDefinitionService } from '../../services';

/**
* Workflow definition list
*/
class Definitions extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {};
    this.workflowDefinitionService = new WorkflowProcessDefinitionService();
  }

  getContentKey() {
    return 'content.workflow.definitions';
  }

  componentDidMount() {
    this._loadDefinitions();
    this.selectNavigationItem('workflow-definitions');
  }

  /**
   * Load all active and last version workflow definitions
   */
  _loadDefinitions() {
    const promise = this.workflowDefinitionService.getAllDefinitions();
    this.setState({
      showLoading: true
    });
    promise.then((json) => {
      this.setState({
        showLoading: false,
        definitions: json._embedded.resources
      });
    }).catch(ex => {
      this.setState({
        showLoading: false
      });
      this.addError(ex);
    });
  }

  /**
   * Validate extension type and uplod definition
   * @param  {file} file File to upload
   */
  _upload(file) {
    if (!file.name.endsWith('.bpmn20.xml')) {
      this.addMessage({
        message: this.i18n('fileRejected', {name: file.name}),
        level: 'warning'
      });
      return;
    }
    this.setState({
      showLoading: true
    });

    const formData = new FormData();
    formData.append( 'name', file.name );
    formData.append( 'fileName', file.name);
    formData.append( 'data', file );
    this.workflowDefinitionService.upload(formData)
    .then(() => {
      this.setState({
        showLoading: false
      }, () => {
        this.addMessage({
          message: this.i18n('fileUploded', {name: file.name})
        });
        this._loadDefinitions();
      });
    })
    .catch(error => {
      this.setState({
        showLoading: false
      });
      this.addError(error);
    });
  }

  /**
   * Dropzone component function called after select file
   * @param  {array} files Array of selected files
   */
  _onDrop(files) {
    if (this.refs.dropzone.state.isDragReject) {
      this.addMessage({
        message: this.i18n('filesRejected'),
        level: 'warning'
      });
      return;
    }
    files.forEach((file)=> {
      this._upload(file);
    });
  }

  render() {
    const { definitions, showLoading } = this.state;
    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <Basic.PageHeader>
          {this.i18n('header')}
        </Basic.PageHeader>

        <Basic.Panel>
          <Basic.Dropzone ref="dropzone"
            multiple
            accept="text/xml"
            onDrop={this._onDrop.bind(this)}>
          </Basic.Dropzone>
        </Basic.Panel>
        <Basic.Panel>
          <Basic.Table ref="table" data={definitions} showLoading={showLoading} noData={this.i18n('component.basic.Table.noData')}>
            <Basic.Column property="key" header={this.i18n('key')} width="10%"
              cell={<Basic.LinkCell property="key" to="workflow/definitions/:key"/>}/>
            <Basic.Column property="name" header={this.i18n('name')} width="20%"/>
            <Basic.Column property="resourceName" header={this.i18n('resourceName')} width="20%"/>
            <Basic.Column property="description" header={this.i18n('description')} width="35%"/>
            <Basic.Column property="version" header={this.i18n('version')} width="5%"/>
          </Basic.Table>
        </Basic.Panel>
      </div>
    );
  }
}

Definitions.propTypes = {
};
Definitions.defaultProps = {
};

function select() {
  return {};
}

export default connect(select, null, null, { withRef: true})(Definitions);
