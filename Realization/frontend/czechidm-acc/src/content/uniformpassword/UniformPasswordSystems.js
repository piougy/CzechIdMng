import React from 'react';
import Helmet from 'react-helmet';
//
import { Basic, Domain } from 'czechidm-core';
import UniformPasswordSystemTable from './UniformPasswordSystemTable';
import { UniformPasswordSystemManager } from '../../redux';

/**
 * Uniform password - table with connected system
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 */
export default class UniformPasswordSystems extends Basic.AbstractContent {

  constructor(props) {
    super(props);
    this.manager = new UniformPasswordSystemManager();
  }

  getContentKey() {
    return 'acc:content.uniformPasswordSystem';
  }

  getNavigationKey() {
    return this.getRequestNavigationKey('uniform-password-system', this.props.match.params);
  }

  render() {
    const forceSearchParameters = new Domain.SearchParameters().setFilter('uniformPasswordId', this.props.match.params.entityId);
    return (
      <div>
        <Helmet title={this.i18n('detail')} />

        <Basic.ContentHeader text={ this.i18n('detail') } style={{ marginBottom: 0 }}/>
        <UniformPasswordSystemTable
          uiKey="uniform-password-system-table"
          forceSearchParameters={ forceSearchParameters }
          className="no-margin"
          manager={ this.manager }
          match={ this.props.match }/>
      </div>
    );
  }
}
