import React from 'react';
import { connect } from 'react-redux';
import * as Basic from '../../components/basic';
import { ConfidentialStorageValueManager } from '../../redux';
import ConfidentialStorageValueTable from './ConfidentialStorageValueTable';

/**
* Content with all confidential storage values
*
* @author Patrik Stloukal
*/
class ConfidentialStorageValues extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.confidentialStorageValueManager = new ConfidentialStorageValueManager();
  }

  getManager() {
    return this.confidentialStorageValueManager;
  }

  getContentKey() {
    return 'content.confidentialStorage';
  }

  getNavigationKey() {
    return 'confidential-storage';
  }

  render() {
    return (
      <div>
        {this.renderPageHeader()}

        <Basic.Panel>
          <ConfidentialStorageValueTable confidentialStorageValueManager={this.confidentialStorageValueManager} uiKey="confidentialStorageValueTable"/>
        </Basic.Panel>
      </div>
    );
  }
}

ConfidentialStorageValues.propTypes = {
};
ConfidentialStorageValues.defaultProps = {
};

export default connect()(ConfidentialStorageValues);
