# Advanced Tree component

Component for view tree of entities. Using EntityManager.

| Parameter | Type | Description | Default  |
| - | :- | :- | :- |
| manager | object.isRequired | EntityManager for fetching entities in tree | |
| rootNode | object.isRequired | Key for save data to redux store | |
| uiKey | string.isRequired | Define attribute in entity which will be used for show name of node | |
| propertyName | string.isRequired | Define attribute in entity which will be used as node id |   |
| propertyId | string.isRequired | Define attribute in entity which will be used as node id |   |
| propertyParent | string.isRequired | Define attribute in entity which will be used for search children nodes |   |
| style  | object   | Define styles in object | default style (/styles.js) |
| loadingDecorator  | func   |  Can be use for override loading decorator |default decorator |
| toggleDecorator  | func   |  Can be use for override toggle decorator | default decorator|
| headerDecorator  | func   |  Can be use for override header decorator | default decorator| |


## Usage
### Simplest example:
```javascript

 <Advanced.Tree
  rootNode={{name: 'top', shortName: 'Organizace', toggled: true}}
  propertyId="name"
  propertyParent="parentId"
  propertyName="shortName"
  uiKey="orgTree"
  manager={this.getManager()}
  />
```
### Example with custom Header decorator:
```javascript

<Advanced.Tree
  rootNode={{id: 'top', name: 'top', toggled: false, shortName: 'Organizační struktura', children: []}}
  propertyId="name"
  propertyParent="parentId"
  propertyName="shortName"
  headerDecorator={this._orgTreeHeaderDecorator.bind(this)}
  uiKey="user-table-org-tree"
  manager={this.organizationManager}
  />

  _orgTreeHeaderDecorator(props){
    const style = props.style;
    const iconType = props.node.isLeaf ? 'group' : 'building';
    const iconClass = `fa fa-${iconType}`;
    const iconStyle = { marginRight: '5px' };
    return (
      <div style={style.base}>
        <div style={style.title}>
          <i className={iconClass} style={iconStyle}/>
          <Basic.Button level="link" style={{padding: '0px 0px 0px 0px'}} onClick={this._homeOrganizationFilter.bind(this, props.node)}>
            {props.node['shortName']}
          </Basic.Button>

        </div>
      </div>
    );
  }
```
