# Table Component

Table of records, features
* sorting
* pagination
* ...

| Parameter | Type | Description | Default  |
| - | :- | :- | :- |
| data | array[json]    | Array of json objects (e.q. response from rest service)   |  |
| showLoading  | bool   | Shows loading overlay | false |
| onRowClick  | func   | Callback that is called when a row is clicked |  |
| onRowDoubleClick  | func   | Callback that is called when a row is double clicked. | |
| rowClass | oneOfType([string,func]) | ccs class added for row || |

# Column Component

| Parameter | Type | Description | Default  |
| - | :- | :- | :- |
| property | string | Json property name. Nested properties can be used e.g. `identityManager.name` | |
| header | string, node or function | Header cell value | by property |
| cell | string, node or function | Body cell value |   |
| width | string | Column width in pixel or percent |   | |

# Pagination Component

Adds pagination support to table data

| Parameter | Type | Description | Default  |
| - | :- | :- | :- |
| total | string | Total records count | |
| from | string | First record index |  |
| size | string | Page size | |
| paginationHandler | function | Callback action for data pagination -  paginationHandler(from, size) |  |
| sizeOptions | array[number] | Available Page sizes | [1, 10, 25, 50, 100] |



## Simplest Usage
```html
<Table data={arrayOfJsonObjects}/>
```
Table renders array of json object. Columns are resolved from json properties without sorting or pagination.

## Usage with column definitions
```html
<Table data={arrayOfJsonObjects}>
  <Column property="lastName"/>
  <Column property="firstName" header={this.i18n('entity.Identity.firstName')}/>
  <Column
    header={<Cell>{this.i18n('entity.Identity.email')}</Cell>}
    cell={<TextCell data={identities} property="email" />}/>
</Table>
```

Column can be defined as function:
```javascript
/**
 * Renders cell with text content
 */
const TextCell = ({rowIndex, data, property, ...props}) => (
  <DefaultCell {...props}>
    {data[rowIndex][property]}
  </DefaultCell>
);
```

Or static:
```javascript
...
<Column
  header={this.i18n('entity.Identity.lastName')}
  cell={<Cell>Static content</Cell>}/>
...
```

### Prepared cell types for column renrering:

| Cell |  Description  |
| - | :- | :- |
| TextCell | Renders cell with text content. |
| LinkCell | Renders cell with link and text content. |
| DateCell | Renders cell with date content with given format |
| EnumCell | Renders cell with enum localization by enum value |
| BooleanCell | Renders cell with disabled checkbox, which is checked by boolean value |

For whole description, read docs in component file.

## Advanced usage with pagination and sorting (and redux actions)

```javascript
import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import { Link }  from 'react-router';
import { AbstractContent, Table, Column, Pagination, Cell, SortHeaderCell, TextCell, DateCell } from '../components/basic';
import { fetchIdentities, handlePagination, handleSort } from '../redux/data/dataActions';

class Team extends AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  componentDidMount() {
    this.selectNavigationItem('team');
    this._fetchIdentities(this.props.searchParameters);
  }

  _fetchIdentities(searchParameters) {
    this.context.store.dispatch(fetchIdentities(searchParameters));
  }

  _handlePagination(from, size) {
    this.context.store.dispatch(handlePagination(from, size));
  }

  _handleSort(property, order) {
    this.context.store.dispatch(handleSort(property, order));
  }

  _onRowClick(event, rowIndex, data) {
    console.log('onClick', rowIndex, data, event);
  }

  _onRowDoubleClick(event, rowIndex, data) {
    console.log('onRowDoubleClick', rowIndex, data, event);
    // redirect to profile
    const username = data[rowIndex]['name'];
    this.context.router.push('/user/' + username + '/profile');
  }

  render() {
    const { identities, total, showLoading, searchParameters} = this.props;
    return (
      <div>
        <Helmet title={this.i18n('navigation.menu.subordinates.label')} />
        <Panel>
          <PanelHeader text={this.i18n('navigation.menu.subordinates.label')} help="#kotva"/>
            <Table
              data={identities}
              showLoading={showLoading}
              onRowClick={this._onRowClick.bind(this)}
              onRowDoubleClick={this._onRowDoubleClick.bind(this)}
              rowClass={({rowIndex, data}) => { return data[rowIndex]['disabled'] ? 'disabled' : ''}}>
              <Column
                property="name"
                header={<SortHeaderCell header={this.i18n('entity.Identity.name')} sortHandler={this._handleSort.bind(this)} searchParameters={searchParameters}/>}
                cell={<TextCell data={identities} property="name"/>}/>
               <Column
                 header={<SortHeaderCell property="lastName" header={this.i18n('entity.Identity.lastName')} sortHandler={this._handleSort.bind(this)} searchParameters={searchParameters}/>}
                 cell={<TextCell data={identities} property="lastName" />}/>
               <Column property="firstName" header={this.i18n('entity.Identity.firstName')}/>
               <Column
                 header={<Cell>{this.i18n('entity.Identity.email')}</Cell>}
                 cell={<TextCell data={identities} property="email" />}/>
               <Column
                 header={<Cell>{this.i18n('entity.Identity.description')}</Cell>}
                 cell={<TextCell data={identities} property="description" />}/>
               <Column
                 header={<Cell>{this.i18n('entity.Identity.createdAt')}</Cell>}
                 cell={<DateCell format={this.i18n('format.date')} property="createdAt" />}/>
            </Table>
          <Pagination
            paginationHandler={this._handlePagination.bind(this)}
            total={total} page={searchParameters.getPage()} size={searchParameters.getSize()} />
        </Panel>
      </div>
    );
  }
}

Team.propTypes = {
  showLoading: React.bool, // loadinig indicator
  identities: React.arrayOf(React.object),
  total: React.number,
  searchParameters: React.object
}
Team.defaultProps = {
  showLoading: true,
  identities: [],
  total: null,
  searchParameters: null
}

function select(state) {
  return {
    showLoading: state.data.showLoading,
    identities: state.data.items,
    total: state.data.total,
    searchParameters: state.data.searchParameters,
  }
}

export default connect(select)(Team)
```
