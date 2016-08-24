import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import * as Basic from '../../../../components/basic';
import { WorkflowProcessDefinitionService } from '../../services';

/**
* Workflow definition detail
*/

class Definition extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {};
    this.workflowDefinitionService = new WorkflowProcessDefinitionService();
  }

  getContentKey() {
    return 'content.workflow.definition';
  }

  componentDidMount() {
    const { definitionId } = this.props.params;
    this.setState({
      showLoading: true
    });
    const promise = this.workflowDefinitionService.getById(definitionId);
    promise.then((json) => {
      this.setState({
        showLoading: false,
        definition: json
      });
      this.refs.form.setData(json);
    }).catch(ex => {
      this.setState({
        showLoading: false
      });
      this.addError(ex);
    });
    this.selectNavigationItem('workflow-definitions');
    this.workflowDefinitionService.downloadDiagram(definitionId, this.reciveDiagram.bind(this));
  }

  reciveDiagram(blob) {
    const objectURL = URL.createObjectURL(blob);
    this.setState({diagramUrl: objectURL});
  }

  _showFullDiagram() {
    this.setState({showModalDiagram: true});
  }

  _closeModalDiagram() {
    this.setState({showModalDiagram: false});
  }

  render() {
    const {showLoading, showModalDiagram, diagramUrl} = this.state;
    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <Basic.PageHeader>
          {this.i18n('header')}
        </Basic.PageHeader>

        <Basic.Panel showLoading={showLoading}>
          <Basic.AbstractForm ref="form" readOnly className="form-horizontal">
            <Basic.TextField ref="key" label={this.i18n('key')}/>
            <Basic.TextField ref="name" label={this.i18n('name')}/>
            <Basic.TextField ref="resourceName" label={this.i18n('resourceName')}/>
            <Basic.TextField ref="category" label={this.i18n('category')}/>
            <Basic.TextField ref="diagramResourceName" label={this.i18n('diagramResourceName')}/>
            <Basic.TextField ref="version" label={this.i18n('version')}/>
            <Basic.TextField ref="id" label={this.i18n('id')}/>
            <Basic.TextField ref="deploymentId" label={this.i18n('deploymentId')}/>
            <Basic.TextArea ref="description" label={this.i18n('description')}/>
          </Basic.AbstractForm>
          <Basic.PanelFooter>
            <Basic.Button type="button" level="link" onClick={this.context.router.goBack}>
              {this.i18n('button.back')}
            </Basic.Button>
          </Basic.PanelFooter>
        </Basic.Panel>
        <Basic.Panel showLoading={!diagramUrl}>
          <Basic.PanelHeader>
            {this.i18n('diagram')}
            <div className="pull-right">
              <Basic.Button type="button" className="btn-sm" level="success" onClick={this._showFullDiagram.bind(this)}>
                <Basic.Icon icon="fullscreen"/>
              </Basic.Button>
            </div>
          </Basic.PanelHeader>
          <div style={{textAlign: 'center', marginBottom: '40px'}}>
            <img style={{maxWidth: '70%'}} src={diagramUrl}/>
          </div>
        </Basic.Panel>
        <Basic.Modal show={showModalDiagram} dialogClassName="modal-large" onHide={this._closeModalDiagram.bind(this)} style={{width: '90%'}} keyboard={!diagramUrl}>
          <Basic.Modal.Header text={this.i18n('fullscreenDiagram')}/>
          <Basic.Modal.Body style={{overflow: 'scroll'}}>
            <img src={diagramUrl}/>
          </Basic.Modal.Body>
          <Basic.Modal.Footer>
            <Basic.Button level="link" disabled={showLoading} onClick={this._closeModalDiagram.bind(this)}>{this.i18n('button.close')}</Basic.Button>
          </Basic.Modal.Footer>
        </Basic.Modal>
      </div>
    );
  }
}

Definition.propTypes = {
};
Definition.defaultProps = {
};

function select() {
  return {};
}

export default connect(select, null, null, { withRef: true})(Definition);
