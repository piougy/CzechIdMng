

import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import * as Basic from '../../../components/basic';
import * as Advanced from '../../../components/advanced';
import { SettingManager, AttachmentManager } from '../../../redux';
import { SecurityManager } from '../../../modules/core/redux';

const settingManager = new SettingManager();
const attachmentManager = new AttachmentManager();

class Setting extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      showLoading: false
    }
  }

  componentDidMount() {
    this.selectNavigationItem('system-setting');
  }

  upload(event) {
    if (event) {
      event.preventDefault();
    }
    this.setState({
      showLoading: true
    });
    let formData = new FormData();
    formData.append( 'file', this.refs.file.getDOMNode().files[0] );
    formData.append( 'description', this.refs.description.value);
    attachmentManager.getService().upload(formData)
    .then(response => {
      this.setState({
        showLoading: false
      });
      return response.json();
    }).then(json => {
      if (!json.error) {
        this.addMessage({
          message: 'Příloha uložena'
        });
        this.refs.table.getWrappedInstance().reload();
      } else {
        this.addError(json.error);
      }
    })
    .catch(error => {
      this.addError(error);
    });
  }

  downloadCell({rowIndex, data, property, ...props}) {
    return <a href={attachmentManager.getService().getDownloadUrl(data[rowIndex]['id'])} title="Download">{data[rowIndex][property]}</a>;
  }

  render() {
    const { userContext } = this.props;
    const forceFilters = [{
      field: 'nextVersion',
      value: '',
    }];

    return (
      <Basic.Row>
        <div className="col-lg-offset-1 col-lg-10">
          <Helmet title={this.i18n('navigation.menu.setting')} />

          <div className="row hidden">
              <div className="col-lg-3 col-md-6">
                  <div className="panel panel-primary">
                      <div className="panel-heading">
                          <div className="row">
                              <div className="col-xs-3">
                                  <Basic.Icon icon="cloud-download" className="fa-5x"/>
                              </div>
                              <div className="col-xs-9 text-right">
                                  <div className="huge">26</div>
                                  <div>New Comments!</div>
                              </div>
                          </div>
                      </div>
                      <a href="#">
                          <div className="panel-footer">
                              <span className="pull-left">View Details</span>
                              <span className="pull-right"><i className="fa fa-arrow-circle-right"></i></span>
                              <div className="clearfix"></div>
                          </div>
                      </a>
                  </div>
              </div>
              <div className="col-lg-3 col-md-6">
                  <div className="panel panel-success">
                      <div className="panel-heading">
                          <div className="row">
                              <div className="col-xs-3">
                                  <Basic.Icon icon="tasks" className="fa-5x"/>
                              </div>
                              <div className="col-xs-9 text-right">
                                  <div className="huge">12</div>
                                  <div>New Tasks!</div>
                              </div>
                          </div>
                      </div>
                      <a href="#">
                          <div className="panel-footer">
                              <span className="pull-left">View Details</span>
                              <span className="pull-right"><i className="fa fa-arrow-circle-right"></i></span>
                              <div className="clearfix"></div>
                          </div>
                      </a>
                  </div>
              </div>
              <div className="col-lg-3 col-md-6">
                  <div className="panel panel-warning">
                      <div className="panel-heading">
                          <div className="row">
                              <div className="col-xs-3">
                                  <Basic.Icon icon="shopping-cart" className="fa-5x"/>
                              </div>
                              <div className="col-xs-9 text-right">
                                  <div className="huge">124</div>
                                  <div>New Orders!</div>
                              </div>
                          </div>
                      </div>
                      <a href="#">
                          <div className="panel-footer">
                              <span className="pull-left">View Details</span>
                              <span className="pull-right"><i className="fa fa-arrow-circle-right"></i></span>
                              <div className="clearfix"></div>
                          </div>
                      </a>
                  </div>
              </div>
              <div className="col-lg-3 col-md-6">
                  <div className="panel panel-danger">
                      <div className="panel-heading">
                          <div className="row">
                              <div className="col-xs-3">
                                  <Basic.Icon icon="phone-alt" className="fa-5x"/>
                              </div>
                              <div className="col-xs-9 text-right">
                                  <div className="huge">13</div>
                                  <div>Support Tickets!</div>
                              </div>
                          </div>
                      </div>
                      <a href="#">
                          <div className="panel-footer">
                              <span className="pull-left">View Details</span>
                              <span className="pull-right"><i className="fa fa-arrow-circle-right"></i></span>
                              <div className="clearfix"></div>
                          </div>
                      </a>
                  </div>
              </div>
          </div>

          <Basic.Panel rendered={false}>
            <Basic.PanelHeader text={this.i18n('content.setting.header') + ' uživatele'}/>
            <Basic.PanelBody>
              Počet záznamů na stránce, výchozí lokalizace ...
            </Basic.PanelBody>
          </Basic.Panel>

          <Basic.Panel style={{wordWrap: 'break-word'}} rendered={SecurityManager.isAdmin(userContext)}>
            <Basic.PanelHeader text="Nastavení aplikace"/>
            <Advanced.Table manager={settingManager} pagination={false}>
              <Advanced.Column property="key" width="50%" face="text" />
              <Advanced.Column property="value" width="50%" face="text"/>
            </Advanced.Table>
          </Basic.Panel>

          <Basic.Panel rendered={SecurityManager.isAdmin(userContext) && 1 !== 1}>
            <Basic.PanelHeader text="Agenda příloh"/>
            <Advanced.Table
                ref="table"
                manager={attachmentManager}
                pagination={true}
                forceSearchParameters={{ filter: {filters: forceFilters}}}>
              <Advanced.Column property="name" width="20%" cell={this.downloadCell.bind(this)} sort={true}/>
              <Advanced.Column property="description" face="text" sort={true}/>
              <Advanced.Column property="mimetype" width="10%" face="text" sort={true}/>
              <Advanced.Column property="encoding" width="10%" face="text"/>
              <Advanced.Column property="filesize" width="10%" face="text"/>
              <Advanced.Column property="versionLabel" width="10%" face="text"/>
            </Advanced.Table>
            <form ref="uploadForm" encType="multipart/form-data" className="uploader form-inline" onSubmit={this.upload.bind(this)}>
              <Basic.PanelHeader text="Nová příloha"/>
              <Basic.PanelBody showLoading={this.state.showLoading}>

                <div className="form-group">
                  <input ref="file" type="file" name="file" className="upload-file" style={{display: 'inline-block'}}/>
                </div>
                <div className="form-group" style={{width: '50%', marginLeft: '10px'}}>
                  <input ref="description" type="text" name="description" className="form-control" placeholder="Popis" style={{width: '100%'}}/>
                </div>
              </Basic.PanelBody>
              <Basic.PanelFooter>
                <Basic.Button type="submit">Uložit</Basic.Button>
              </Basic.PanelFooter>
            </form>
          </Basic.Panel>
        </div>
      </Basic.Row>
    );
  }
}

Setting.propTypes = {
  userContext: PropTypes.object
};

Setting.defaultProps = {
  userContext: null
};

function select(state) {
  return {
    userContext: state.security.userContext
  };
}

export default connect(select)(Setting);
