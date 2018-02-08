import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Services from '../../services';
import * as Managers from '../../redux/data';


/**
* Workflow definition list
*/
class Definitions extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {};
    this.workflowDefinitionService = new Services.WorkflowProcessDefinitionService();
    this.workflowProcessDefinitionManager = new Managers.WorkflowProcessDefinitionManager();
  }

  getContentKey() {
    return 'content.workflow.definitions';
  }

  componentDidMount() {
    this.selectNavigationItem('workflow-definitions');
  }

  /**
   * Validate extension type and upload definition
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
          message: this.i18n('fileUploaded', {name: file.name})
        });
        this.refs.table.getWrappedInstance().reload();
      });
    })
    .catch(error => {
      this.setState({
        showLoading: false
      });
      this.addError(error);
    });
  }

  getManager() {
    return this.workflowProcessDefinitionManager;
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
    const { showLoading } = this.state;
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
          <Advanced.Table
            ref="table"
            uiKey="workflow-definitions-agenda"
            manager={this.getManager()}
            showLoading={showLoading}
            rowClass={({rowIndex, data}) => { return data[rowIndex].disabled ? 'disabled' : ''; }}
            noData={this.i18n('component.basic.Table.noData')}>

            <Advanced.Column property="key" header={this.i18n('key')} width="25%"
              cell={<Basic.LinkCell property="key" to="workflow/definitions/:key"/>} sort />
            <Advanced.Column property="name" header={this.i18n('name')} width="20%" sort />
            <Advanced.Column property="resourceName" header={this.i18n('resourceName')} width="15%" />
            <Advanced.Column property="description" header={this.i18n('description')} width="25%" />
            <Advanced.Column property="version" header={this.i18n('version')} width="5%" />
          </Advanced.Table>
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
