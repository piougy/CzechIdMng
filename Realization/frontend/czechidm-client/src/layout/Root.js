

import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import modules from '../../dist/modules/moduleAssembler';
import { componentDescriptors } from '../../dist/modules/componentAssembler';
//
import {Basic, LayoutActions} from 'czechidm-core';
import ComponentLoader from 'czechidm-core/src/utils/ComponentLoader';
import ConfigLoader from 'czechidm-core/src/utils/ConfigLoader';
import config from '../../dist/config.json';

export class Root extends Basic.AbstractContent {

  constructor() {
    super();
  }

  componentDidMount() {
    this.context.store.dispatch(LayoutActions.modulesInit(modules.moduleDescriptors));
    ConfigLoader.initConfig(config);
    this.context.store.dispatch(LayoutActions.i18nInit());
    ComponentLoader.initComponents(componentDescriptors);
  }


  componentWillUpdate() {
    const {modulesReady} = this.props;
    if (modulesReady) {
      this.context.store.dispatch(LayoutActions.navigationInit());
    }
  }

  componentWillUnmount() {
  }

  getChildContext() {
    return {
    };
  }

  render() {
    const { i18nReady} = this.props;
    const titleTemplate = '%s | ' + this.i18n('app.name');
    return (
      <div>
        <Basic.Loading className="global" showLoading={!i18nReady}/>
        {
          !i18nReady
          ||
          <div>
            <Helmet title={this.i18n('navigation.menu.home')} titleTemplate={titleTemplate}/>
            { this.props.app }
          </div>
        }
      </div>
    );
  }
}

Root.propTypes = {
  i18nReady: PropTypes.bool
};

Root.defaultProps = {
  i18nReady: false
};

Root.childContextTypes = {
};


function select(state) {
  return {
    i18nReady: state.layout.get('i18nReady'),
    modulesReady: state.layout.get('modulesReady')
  };
}

export default connect(select)(Root);
