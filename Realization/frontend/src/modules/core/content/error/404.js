import React from 'react';
import Helmet from 'react-helmet';
import { AbstractContent, Icon } from '../../../../components/basic';


export default class Error404 extends AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      id: null // 404 with record id
    };
  }

  getContentKey() {
    return 'content.error.404';
  }

  componentWillMount() {
    this.selectNavigationItem('home');
    const { query } = this.props.location;
     // 404 with record id
    if (query) {
      this.setState({
        id: query.id
      });
    }
  }

  render() {
    const { id } = this.state;
    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <div className="alert alert-warning">
          <div className="alert-icon"><Icon icon="exclamation-sign"/></div>
          <div className="alert-desc">
            {
              !id
              ?
              this.i18n('description')
              :
              this.i18n('record', { id, escape: false })
            }
          </div>
        </div>
      </div>
    );
  }
}
