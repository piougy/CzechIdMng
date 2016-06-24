import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import { AbstractContent, Panel, PanelHeader, PanelBody, Icon } from '../../../../components/basic';


export default class Error404 extends AbstractContent {

  constructor(props, context) {
     super(props, context);
     this.state = {
       id: null // 404 with record id
     }
  }

  getContentKey() {
    return 'content.error.404';
  }

  componentDidMount(){
    this.selectNavigationItem('home');
    let { query } = this.props.location;
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
              this.i18n('record', { id: id, escape: false })
            }
          </div>
        </div>
      </div>
    );
  }
}
