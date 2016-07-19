

import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import * as Basic from '../components/basic';
import { i18nInit } from '../redux/Layout/layoutActions';

export class Root extends Basic.AbstractContent {

  componentDidMount() {
    this.context.store.dispatch(i18nInit());
  }

  componentDidUpdate() {
  }

  componentWillUnmount() {
  }

  getChildContext() {
    return {
    };
  }

  render() {
    const { i18nReady } = this.props;
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
    )
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
    i18nReady: state.layout.get('i18nReady')
  }
}

export default connect(select)(Root);
