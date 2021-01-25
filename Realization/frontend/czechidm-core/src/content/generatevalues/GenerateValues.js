import React from 'react';
import { connect } from 'react-redux';
import * as Basic from '../../components/basic';
import { GenerateValueManager } from '../../redux';
import GenerateValueTable from './GenerateValueTable';

const TABLE_UIKEY = 'generate-values-table-ui-key';

/**
* Content with all generate values.
*
* @author Ondřej Kopr
*/
class GenerateValues extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.manager = new GenerateValueManager();
  }

  getManager() {
    return this.manager;
  }

  getContentKey() {
    return 'content.generateValues';
  }

  getNavigationKey() {
    return 'generate-values';
  }

  render() {
    return (
      <Basic.Div>
        { this.renderPageHeader() }

        <Basic.Panel>
          <GenerateValueTable manager={ this.manager } uiKey={ TABLE_UIKEY } match={ this.props.match } filterOpened/>
        </Basic.Panel>
      </Basic.Div>
    );
  }
}

GenerateValues.propTypes = {
};
GenerateValues.defaultProps = {
};

export default connect()(GenerateValues);
