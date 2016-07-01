'use strict';

import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import uuid from 'uuid';
//
import * as Basic from '../../../../components/basic';
import * as Advanced from '../../../../components/advanced';
import { WorkflowDefinitionService } from '../../services';
import _ from 'lodash';

/**
* Workflow definition detail
*/

class Definition extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {};
    this.workflowDefinitionService = new WorkflowDefinitionService();
  }

  getContentKey() {
    return 'content.workflow.definition';
  }

  componentDidMount() {
    console.log('here');
    const { definitionId } = this.props.params;
    this.setState({
      showLoading: true
    });
    let promise = this.workflowDefinitionService.getById(definitionId);
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
  }

  render() {
    const {showLoading, definition} = this.state;
    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <Basic.PageHeader>
          {this.i18n('header')}
        </Basic.PageHeader>

        <Basic.Panel showLoading={showLoading}>
          <Basic.AbstractForm ref="form" readOnly>
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
      </div>
    );
  }
}

Definition.propTypes = {
  definitionId: PropTypes.string.isRequired
}
Definition.defaultProps = {

}

function select(state, component) {
  return {};
}

export default connect(select, null, null, { withRef: true})(Definition);
