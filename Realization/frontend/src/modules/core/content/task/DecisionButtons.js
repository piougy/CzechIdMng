'use strict';

import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import * as Basic from '../../../../components/basic';
import { SecurityManager, IdentityManager, WorkflowTaskInstanceManager } from '../../../../modules/core/redux';
import * as Advanced from '../../../../components/advanced';
import ComponentService from '../../../../services/ComponentService';

const workflowTaskInstanceManager = new WorkflowTaskInstanceManager();
const componentService = new ComponentService();
let detailComponent;

class DecisionButtons extends Basic.AbstractContent {

  constructor(props) {
    super(props);
    this.state = {showLoading: props.showLoading};
  }

  componentDidMount() {
  }

  getContentKey() {
    return 'content.task.DecisionButton';
  }

  _onClick(decision){
    this.props.onClick(decision);
  }

  _getDecisionsButton(decisions, showLoading){
    let buttons = [];
    for (let decision of decisions) {
      buttons.push(
                    <Basic.Button
                      type="button"
                      level={decision.level}
                      onClick={this._onClick.bind(this,decision)}
                      showLoading={showLoading}
                      tooltip={decision.tooltip}>
                        {this.i18n(decision.label)}
                    </Basic.Button>
                  );
      buttons.push(' ');
    }
    return buttons;
  }

  render() {
    const { readOnly, task, showLoading} = this.props;
    let decisions;
    if (task){
      decisions = task.decisions;
    }
    return (
        <div>
          {decisions ?
            <div>
              <Basic.Button type="button" level="link" onClick={this.context.router.goBack}
                showLoading={showLoading}>{this.i18n('button.back')}</Basic.Button>
              {this._getDecisionsButton(decisions, showLoading)}
            </div>
          :
          <Basic.Well showLoading={true}/>
          }
        </div>
    )
  }
}

DecisionButtons.propTypes = {
  task: PropTypes.object,
  readOnly: PropTypes.bool
}
DecisionButtons.defaultProps = {
  task: null,
  readOnly: false
}

function select(state, component) {
  return {}
}

export default connect(select)(DecisionButtons);
