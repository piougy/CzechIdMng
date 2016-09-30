import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import ConfigLoader from '../../utils/ConfigLoader';
import ComponentService from '../../services/ComponentService';

class FrontendModules extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      moduleDescriptors: [],
      components: []
    };
    this.componentService = new ComponentService();
  }

  getContentKey() {
    return 'content.system.fe-modules';
  }

  componentDidMount() {
    this.selectNavigationItem('fe-modules');
  }


  render() {
    return (
      <div>
        <Helmet title={this.i18n('title')} />

        <Basic.PageHeader text={this.i18n('header')}/>

        {
          ConfigLoader.getModuleDescriptors()
            .sort((one, two) => {
              return one.id > two.id;
            }).map(moduleDescriptor => {
              const componentDescriptor = this.componentService.getComponentDescriptor(moduleDescriptor.id);
              //
              return (
                <Basic.Panel className={ConfigLoader.isEnabledModule(moduleDescriptor.id) ? '' : 'disabled'}>
                  <Basic.PanelHeader>
                    <h2 className="pull-left">
                      <span>{moduleDescriptor.name} <small>{moduleDescriptor.id}</small></span>
                    </h2>
                    <Basic.Label text={this.i18n('label.disabled')} level="default" rendered={!ConfigLoader.isEnabledModule(moduleDescriptor.id)} className="pull-right" style={{ marginTop: 4}}/>
                    <div className="clearfix"></div>
                  </Basic.PanelHeader>

                  <Basic.PanelBody>
                    {moduleDescriptor.description}
                  </Basic.PanelBody>

                  <Basic.PanelHeader>
                    <h2>{this.i18n('components.header')}</h2>
                  </Basic.PanelHeader>

                  <Basic.Table
                    data={componentDescriptor ? componentDescriptor.components : null}
                    rowClass={({rowIndex, data}) => { return data[rowIndex].module !== this.componentService.getComponentDefinition(data[rowIndex].id).module ? 'disabled' : ''; }}>
                    <Basic.Column
                      property="id"
                      header={this.i18n('label.id')}
                      cell={({rowIndex, data}) => {
                        const overridedInModule = this.componentService.getComponentDefinition(data[rowIndex].id).module;
                        const overrided = data[rowIndex].module !== overridedInModule;
                        const textStyle = {};
                        if (overrided) {
                          textStyle.textDecoration = 'line-through';
                        }
                        return (
                          <span
                            title={overrided ? this.i18n('', { moduleId: overridedInModule }) : ''}>
                            <span style={textStyle}>{data[rowIndex].id}</span>
                            {
                              !overrided
                              ||
                              <span> ({overridedInModule})</span>
                            }
                          </span>
                        );
                      }}/>
                    <Basic.Column property="priority" header={this.i18n('components.priority')} width="100px"/>
                    <Basic.Column property="type" header={this.i18n('components.type')} width="100px"/>
                    <Basic.Column property="order" header={this.i18n('components.order')} width="100px"/>
                    <Basic.Column property="span" header={<span>Span <small>col-lg</small></span>} width="100px"/>
                  </Basic.Table>
                </Basic.Panel>
              );
            })
        }

      </div>
    );
  }
}

FrontendModules.propTypes = {
  userContext: PropTypes.object
};
FrontendModules.defaultProps = {
  userContext: null
};

function select(state) {
  return {
    userContext: state.security.userContext
  };
}

export default connect(select)(FrontendModules);
