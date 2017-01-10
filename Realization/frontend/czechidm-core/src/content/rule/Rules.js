import React from 'react';
import { connect } from 'react-redux';
import * as Basic from '../../components/basic';
import { RuleManager } from '../../redux';
import RuleTable from './RuleTable';

const RULE_TABLE_UIKEY = 'ruleTableUikey';

/**
* Content with all rules
*/
class Rules extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.ruleManager = new RuleManager();
  }

  getManager() {
    return this.treeTypeManager;
  }

  getContentKey() {
    return 'content.rules';
  }

  getNavigationKey() {
    return 'rules';
  }

  render() {
    return (
      <div>
        {this.renderPageHeader()}

        <Basic.Panel>
          <RuleTable ruleManager={this.ruleManager} uiKey={RULE_TABLE_UIKEY}/>
        </Basic.Panel>
      </div>
    );
  }
}

Rules.propTypes = {
};
Rules.defaultProps = {
};

export default connect()(Rules);
