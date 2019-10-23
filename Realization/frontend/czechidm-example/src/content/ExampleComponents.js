import React from 'react';
//
import { Basic, Advanced, Managers } from 'czechidm-core';

const treeNodeManager = new Managers.TreeNodeManager();
const roleCatalogueManager = new Managers.RoleCatalogueManager();
const roleManager = new Managers.RoleManager();

/**
 * Example of components usage
 *
 * @author Radek Tomiška
 */
export default class ExampleComponents extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      selectedNodes: [],
      setting: {
        managerType: 'treeNode',
        multiSelect: true,
        traverse: false,
        disableSelect: false,
        fileIcon: 'ok',
        folderIcon: 'fa:folder-o',
        folderOpenIcon: 'fa:folder-open-o'
      }
    };
  }

  /**
   * "Shorcut" for localization
   */
  getContentKey() {
    return 'example:content.components';
  }

  /**
   * Selected navigation item
   */
  getNavigationKey() {
    return 'example-components';
  }

  getManager() {
    const { managerType } = this.state.setting;
    //
    switch (managerType) {
      case 'role': {
        return roleManager;
      }
      case 'roleCatalogue': {
        return roleCatalogueManager;
      }
      default: {
        return treeNodeManager;
      }
    }
  }

  onChange(selectedNodes) {
    const { disableSelect, multiSelect } = this.state.setting;
    //
    if (!disableSelect) {
      this.setState({
        selectedNodes: multiSelect ? selectedNodes : [ selectedNodes ]
      });
    }
  }

  onSettingSubmit(event) {
    if (event) {
      event.preventDefault();
    }
    this.setState({
      setting: this.refs.form.getData()
    }, () => {
      this.refs['example-tree'].reload();
    });
  }

  getNodeIcon({ node, opened }) {
    const { fileIcon, folderIcon, folderOpenIcon } = this.state.setting;
    //
    let icon = fileIcon;
    if (node.childrenCount > 0 || node.childrenCount === undefined) {
      if (opened) {
        icon = folderOpenIcon;
      } else {
        icon = folderIcon;
      }
    }
    return icon;
  }

  render() {
    const { selectedNodes, setting } = this.state;
    //
    return (
      <Basic.Div>
        { this.renderPageHeader({ showTitle: true }) }

        <Basic.Panel>
          <Basic.PanelHeader text={ this.i18n('tree.header') } />
          <Basic.PanelBody style={{ borderBottom: '1px solid #ddd' }}>
            <form onSubmit={ this.onSettingSubmit.bind(this) }>
              <Basic.AbstractForm
                ref="form"
                data={ setting }
                style={{ paddingTop: 0 }}>
                <Basic.Row>
                  <Basic.Col lg={ 6 } >
                    <Basic.TextField ref="fileIcon" label={ this.i18n('tree.fileIcon.label') }/>
                    <Basic.TextField ref="folderIcon" label={ this.i18n('tree.folderIcon.label') }/>
                    <Basic.TextField ref="folderOpenIcon" label={ this.i18n('tree.folderOpenIcon.label') }/>
                  </Basic.Col>
                  <Basic.Col lg={ 6 } >
                    <Basic.EnumSelectBox
                      label={ this.i18n('tree.managerType.label') }
                      helpBlock={ this.i18n('tree.managerType.help') }
                      ref="managerType"
                      clearable={ false }
                      options={[
                        { value: 'treeNode', niceLabel: this.i18n('tree.managerType.option.treeNode') },
                        { value: 'role', niceLabel: this.i18n('tree.managerType.option.role') },
                        { value: 'roleCatalogue', niceLabel: this.i18n('tree.managerType.option.roleCatalogue') },
                      ]}/>
                    <Basic.Checkbox ref="traverse" label={ this.i18n('tree.traverse.label') }/>
                    <Basic.Checkbox ref="multiSelect" label={ this.i18n('tree.multiSelect.label') }/>
                  </Basic.Col>
                </Basic.Row>
              </Basic.AbstractForm>
              <Basic.PanelFooter className="marginable">
                <Basic.Button type="submit" level="success">
                  { this.i18n('button.set') }
                </Basic.Button>
              </Basic.PanelFooter>
            </form>
          </Basic.PanelBody>
          <Basic.Row>
            <Basic.Col lg={ 6 } style={{ borderRight: '1px solid #ddd', paddingRight: 0 }}>
              <Advanced.Tree
                ref="example-tree"
                uiKey="example-tree"
                manager={ this.getManager() }
                onChange={ this.onChange.bind(this) }
                multiSelect={ setting.multiSelect }
                traverse={ setting.traverse }
                nodeIcon={ this.getNodeIcon.bind(this) }
                nodeIconClassName={ null }/>
            </Basic.Col>
            <Basic.Col lg={ 6 } style={{ paddingLeft: 0 }}>
              <Advanced.Tree
                ref="example-selected-tree"
                uiKey="example-selected-tree"
                manager={ this.getManager() }
                header={ this.i18n('label.selected') }
                onChange={ () => false }
                noData={ this.i18n('label.select') }
                roots={ selectedNodes }
                showRefreshButton={ false }/>
            </Basic.Col>
          </Basic.Row>
        </Basic.Panel>

        <Basic.Panel>
          <Basic.PanelHeader text={ this.i18n('icon.header', { escape: false }) } />
          <Basic.PanelBody>
            <Basic.Alert level="info" className="no-margin" title={ this.i18n('icon.usage') }>
              <pre>
                { '<Basic.Icon value="component:role"/>' }
              </pre>
            </Basic.Alert>
            <Advanced.Icons />
          </Basic.PanelBody>
        </Basic.Panel>
      </Basic.Div>
    );
  }
}
