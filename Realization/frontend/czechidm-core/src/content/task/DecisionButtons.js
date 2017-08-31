

import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';

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

  _onClick(decision) {
    this.props.onClick(decision);
  }

  _getDecisionsButton(decisions, showLoading) {
    const buttons = [];
    for (const decision of decisions) {
      buttons.push(
                    <Basic.Button
                      type="button"
                      level={decision.level}
                      onClick={this._onClick.bind(this, decision)}
                      showLoading={showLoading}
                      tooltip={this.i18n(decision.tooltip ? decision.tooltip : `wf.decision.${decision.id}.tooltip` )}>
                        {this.i18n(decision.label ? decision.label : `wf.decision.${decision.id}.label` )}
                    </Basic.Button>
                  );
      buttons.push(' ');
    }
    return buttons;
  }

  _goBack() {
    const { task } = this.props;
    if (this.context.router.goBack()) {
      // nothig, router just can go back
    } else {
      // transmition to /task, history doesnt exist
      this.context.router.push(`tasks/${task.variables.implementerIdentifier}`);
    }
  }

  render() {
    const {task, showLoading} = this.props;
    let decisions;
    if (task) {
      decisions = task.decisions;
    }

    return (
        <div>
          {decisions ?
            <div>
              <Basic.Button type="button" level="link" onClick={this._goBack.bind(this)}
                showLoading={showLoading}>{this.i18n('button.back')}</Basic.Button>
              {this._getDecisionsButton(decisions, showLoading)}
            </div>
          :
          <Basic.Well showLoading/>
          }
        </div>
    );
  }
}

DecisionButtons.propTypes = {
  task: PropTypes.object,
  readOnly: PropTypes.bool
};

DecisionButtons.defaultProps = {
  task: null,
  readOnly: false
};

function select() {
  return {};
}

export default connect(select)(DecisionButtons);
