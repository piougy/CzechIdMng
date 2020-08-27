import React from 'react';
//
import { Basic } from 'czechidm-core';
import { UniformPasswordManager } from '../../redux';
import UniformPasswordTable from './UniformPasswordTable';

/**
 * List of uniform password definition
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 */
export default class UniformPasswords extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.manager = new UniformPasswordManager();
  }

  getContentKey() {
    return 'acc:content.uniformPassword';
  }

  getNavigationKey() {
    return 'uniform-password';
  }

  render() {
    return (
      <div>
        {this.renderPageHeader()}

        <Basic.Panel>
          <UniformPasswordTable uiKey="uniform-password-table" manager={this.manager}/>
        </Basic.Panel>

      </div>
    );
  }
}

UniformPasswords.propTypes = {
};
UniformPasswords.defaultProps = {
};
