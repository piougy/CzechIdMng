# AdvancedTable Component

Encapsulates all features from BasicTable component.

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| manager | object.isRequired | EntityManager subclass, which provides data fetching | |
| uiKey | string  | optional table identifier - it's used as key in store  | if isn't filled, then manager.getEntityType() is used |
| pagination | bool | If pagination is shown | true |
| forceSearchParameters | object | "Hard filter" - sometimes is useful show just some data (e.q. data filtered by logged user) |   |
| onRowClick  | func   | Callback that is called when a row is clicked |  |
| onRowDoubleClick  | func   | Callback that is called when a row is double clicked. | |
| forceSearchParameters | object | "Hard filter" - its useful for data filtering by internal parameter , see usage ||
| defaultSearchParameters | object | "Default filter" - its useful for default sorting etc. ||
| rowClass | oneOfType([string,func]) | ccs class added for row ||
| filter | element | Filter definition ||
| filterOpened | bool | If filter is opened by default | false |
| filterCollapsible | bool | If filter can be collapsed |  |
| actions | arrayOf(object) | Bulk actions e.g. { value: 'activate', niceLabel: this.i18n('content.users.action.activate.action'), action: this.onActivate.bind(this) } |  |
| buttons | arrayOf(element) | Buttons are shown on the right of toogle filter button | ||


# AdvancedColumn Component

Header text is automatically resolved by entity and column property. Advanced column supports different data types defined by face property.

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| property | string.isRequired | Json property name. Nested properties can be used e.g. `identityManager.name` | |
| sort | bool | Column supports sorting | false |
| width | string | Pixel or percent width of table. | |
| face | oneOf(['text','date', 'datetime', 'bool']) | Data type | 'text' |

# AdvancedColumnLink Component

All parameters from AdvancedColumn are supported. Added parameters:

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| to | string.isRequired  | React router links "to". Parameters can be used and theirs value is propagated from data[rowIndex].property | | |


## Usage
```javascript


import React from 'react';
import Helmet from 'react-helmet';
//
import * as Basic from '../../../components/basic';
import * as Advanced from '../../../components/advanced';
import { IdentityManager } from '../../../redux/data';

const identityManager = new IdentityManager();

class Team extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  componentDidMount() {
    this.selectNavigationItem('team');
  }

  onRowClick(event, rowIndex, data) {
    console.log('onClick', rowIndex, data, event);
  }

  onRowDoubleClick(event, rowIndex, data) {
    console.log('onRowDoubleClick', rowIndex, data, event);
    // redirect to profile
    const username = data[rowIndex]['name'];
    this.context.router.push('/user/' + username + '/profile');
  }

  useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().useFilter(this.refs.filterForm);
  }

  cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().cancelFilter(this.refs.filterForm);
  }

  render() {
    const { identities, total, showLoading, searchParameters} = this.props;
    const forceFilters = {
      filters: [
        {
          field: 'identityType',
          value: 'USER'
        }
      ]
    };
    return (
      <div>
        <Helmet title={this.i18n('navigation.menu.subordinates.label')} />
        <Basic.Panel>
          <Basic.PanelHeader text={this.i18n('navigation.menu.subordinates.label')} help="#kotva"/>
          <Advanced.Table
            ref="table"
            uiKey="identity_table"
            manager={identityManager}
            forceSearchParameters={{filter: forceFilters}}
            onRowClick={this.onRowClick.bind(this)}
            onRowDoubleClick={this.onRowDoubleClick.bind(this)}
            rowClass={({rowIndex, data}) => { return data[rowIndex]['disabled'] ? 'disabled' : ''}}
            filter={
              <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
                <Basic.AbstractForm ref="filterForm">
                  <Basic.Row>
                    <div className="col-lg-4">
                      <Advanced.Filter.TextField
                        ref="filterCreatedAtFrom"
                        field="createdAt"
                        relation="GE"
                        placeholder={this.i18n('filter.createdAtFrom.placeholder')}
                        label={this.i18n('filter.createdAtFrom.label')}/>
                    </div>
                    <div className="col-lg-4">
                      <Advanced.Filter.TextField
                        ref="filterCreatedAtTill"
                        field="createdAt"
                        relation="LE"
                        placeholder={this.i18n('filter.createdAtTill.placeholder')}
                        label={this.i18n('filter.createdAtTill.label')}/>
                    </div>
                    <div className="col-lg-4 text-right">
                      <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
                    </div>
                  </Basic.Row>
                </Basic.AbstractForm>
              </Advanced.Filter>
            }
            filterOpened={true}
            filterCollapsible={true}
            showRowSelection={true}
            actions={
              [
                { value: 'remove', niceLabel: this.i18n('content.users.action.remove.action'), action: () => alert('not implemented'), disabled: true },
                { value: 'activate', niceLabel: this.i18n('content.users.action.activate.action'), action: () => alert('not implemented') }
              ]
            }
            buttons={
              [
                <Basic.Button type="submit" className="btn-xs" onClick={() => alert('not implemented')} rendered={true}>
                  <Basic.Icon type="fa" icon="user-plus"/>
                  {this.i18n('content.user.create.button.add')}
                </Basic.Button>
              ]
            }>
            <Advanced.ColumnLink to="user/:username/profile" property="name" width="20%" sort={true} face="text"/>
            <Advanced.Column property="lastName" width="15%" sort={true} face="text" />
            <Advanced.Column property="firstName" width="15%" face="text" />
            <Advanced.Column property="email" width="15%" face="text" />
            <Basic.Column
              header={this.i18n('entity.Identity.description')}
              cell={<Basic.TextCell property="description" />}/>
            <Advanced.Column property="createdAt" width="10%" face="date" />
          </Advanced.Table>
        </Basic.Panel>
      </div>
    );
  }
}

Team.propTypes = {
}
Team.defaultProps = {
}

export default Team;
```
