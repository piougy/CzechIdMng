# Advanced Tree component

Component for load and render tree of entities. Using EntityManager, with support for loading tree structures.

Manager has to support methods:
* manager has to extend standard ``EntityManager`` - methods for work entities are used (e.g. find entities, render nice label).
* ``getRootSearchParameters()`` - returns search parameters to find roots.
* ``getTreeSearchParameters()`` - returns search parameters to find children with 'parent' paremeter filter.


Entity has to contain properties:
* ``id`` - node id
* ``parent`` - parent node id
* ``childrenCount`` - sub nodes count (required for loading children)

Entity property names and manager's methods are not configurable - enrich entity or manager with required properties and methods.

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| uiKey | string.isRequired | Key prefix in redux (loading / store data). | |
| manager | EntityManager.isRequired | EntityManager for fetching entities in tree | |
| roots | arrayOf(oneOfType([string, object])) | "Hard roots" - roots can be loaded from outside and given as parameter, then root will not be loaded by method getRootSearchParameters(). Roots can be given as array of ids only - entities has to be loaded in redux store! Search is disabled, if roots are given. | |
| multiSelect | bool   | If is true then component is in multi select mode| false |
| forceSearchParameters | Domain.SearchParameters | "Hard filters" | |
| onChange | func | onChange callback. Selected node (or array of nodes, if multiSelect is true) is given as parameter. | |
| onDoubleClick | func | On double click node callback. Selected node is given as parameter. | |
| onDetail | func | Show detail function. Detail icon is rendered in tree header. | |
| traverse | bool | raverse to selected folder | false |
| header | oneOfType([string, element]) | Tree header. If ``null`` is given, then header is not rendered. | this.i18n('component.advanced.Tree.header')|
| noData | oneOfType([string, element]) | If tree roots are empty, then this text will be shown | this.i18n('component.advanced.Tree.noData') |
| className | string | Tree css |  |
| style | object | Tree styles |  |
| bodyClassName | string | Tree body css |  |
| bodyStyle | object | Tree body styles |  | |
| clearable | bool   | Selected options can be cleared | true |
| nodeIcon | oneOfType([string, func]) | Node icon - single icon for all nodes (string) or callback - named parameters "node" and "opened" will be given. { null } can be given - disable default icons. | default icons for folder 'fa:folder', 'fa:folder-open' and file 'fa:file-o' |
| nodeIconClassName | oneOfType([string, func]) | Node icon class name - string or callback - named parameters "node" and "opened" will be given. { null } can be given - disable default icon class names. | default 'folder' and 'file' |
| nodeNiceLabel | func | Node label. Manager's nice label is used by default. | manager.getNiceLabel(node) |
| showRefreshButton | bool | Shows refresh button. | true |

## Usage

```javascript
import { RoleCatalogueManager } from '../../redux';
//
const manager = new RoleCatalogueManager();
...

<Advanced.Tree
  ref="roleCatalogueTree"
  uiKey="role-catalogue-tree"
  manager={ manager }
  onChange={ (nodeId) => alert('Selected: ' + nodeId) }/>

```
