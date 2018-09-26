import React from 'react';
import { connect } from 'react-redux';
import * as Basic from '../../components/basic';
import { GeneratedValueManager } from '../../redux';
import GeneratedValueTable from './GeneratedValueTable';

const TABLE_UIKEY = 'generated-values-table-ui-key';

/**
* Content with all generated values
*/
class GeneratedValues extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.manager = new GeneratedValueManager();
  }

  getManager() {
    return this.manager;
  }

  getContentKey() {
    return 'content.generatedValues';
  }

  getNavigationKey() {
    return 'generated-values';
  }

  render() {
    return (
      <div>
        {this.renderPageHeader()}

        <Basic.Panel>
          <GeneratedValueTable manager={this.manager} uiKey={TABLE_UIKEY} params={ this.props.params } filterOpened/>
        </Basic.Panel>
      </div>
    );
  }
}

GeneratedValues.propTypes = {
};
GeneratedValues.defaultProps = {
};

export default connect()(GeneratedValues);
