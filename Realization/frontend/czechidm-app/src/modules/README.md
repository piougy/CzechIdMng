# Component descriptor

  Component descriptor has same purpose as XML Bean definition in Spring.
  It is place for definition relation between component key and real component's location in project (require).
  Independent loading component by key, without need to define require on component is main purpose.

  This is especially useful for modularity. Component can be defined in other module than core, but we need use components with same type in one location in core module (for example Dashboard).


| Parameter | Description                                                                                 |
|-----------|---------------------------------------------------------------------------------------------|
| id        | Component identifier (key). With this key will be component loaded. It must be unique.      |
| component | Define require on real location component                                                   |
| type      | Type of component. Using for get all components with same type (for example all dashboards) |
| span      | Span layout. Used in dashboard                                                              |
| order     | Define order of component between other components                                          |


## Usage

#### Definition of component descriptor for one module:
```javascript
{
  'id': 'core',
  'name': 'Core',
  'description': 'Components for Core module',
  'components': [
    {
      'id': 'dynamicRoleTaskDetail',
      'component': require('./content/task/identityRole/DynamicTaskRoleDetail')
    },
    {
      'id': 'assignedTaskDashboard',
      'type': 'dashboard',
      'span': '6',
      'order': '2',
      'component': require('./content/dashboards/AssignedTaskDashboard')
    },
    {
      'id': 'profileDashboard',
      'type': 'dashboard',
      'span': '5',
      'order': '3',
      'component': require('./content/dashboards/ProfileDashboard')
    }
  ]
};
```

#### Get component by key (id)

```javascript
import ComponentService from 'core/services/ComponentService';
DetailComponent = componentService.getComponent('someComponentKey');
```

#### Get components by specific type

```javascript
import ComponentService from 'core/services/ComponentService';
let dashboards = [];
dashboards = this.componentService.getComponentDefinitions('dashboard');
```

# Module descriptor

Each module has his own descriptor with information about himself:
* `id` - unique module identifier
* `name` - user readable name of module
* `description` - textual module overview, purpose ...
* `navigation` - contains navigation items. Navigation is merged from all modules and then is rendered in application.

## Navigation item

Single navigation item parameters:

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| id | string | Identifier | |
| type | oneOf(['DYNAMIC', 'TAB']) | Rendering option. 'DYNAMIC' is rendered automatically in merged navigation. 'TAB' has to be rendered manually on some content. | 'DYNAMIC' |
| section | oneOf(['main', 'system']) | Navigation is splitted to more sections (two is supported now).  | 'main' |
| label | string | Item label | |
| labelKey | string | Item label key to localization. If key is not found in localization, then label is shown. | |
| title | string | Item title (tooltip) | |
| titleKey | string | Item title (tooltip) key to localization. If key is not found in localization, then title is shown. | |
| icon | string | Item icon | 'fa:circle-o' |
| iconColor | string | Item icon color  | #333 |
| order | number | Item order in merged navigation | 0 |
| priority | number | Is used for overiding item in some module. Item with the same id and highest priority will be shown. | 0 |
| path | string | Path for react router is used, when navigation item is active (onClick). | |
| access | arrayOf(object) | See next section. | [{ type: 'IS_AUTHENTICATED' }] |
| items | string | sub navigation items. Only two levels are supported now.  | ||

### Usage

```javascript
...
'navigation': {
  'items': [
    {
      'id': 'identity-profile',
      'type': 'DYNAMIC',
      'section': 'main',
      'label': 'Profil',
      'labelKey': 'navigation.menu.profile.label',
      'title': 'Můj profil',
      'titleKey': 'navigation.menu.profile.title',
      'icon': 'user',
      'iconColor': '#428BCA',
      'order': 10,
      'priority': 0,
      'path': '/identity/:loggedUsername/profile',
      'items': [
        {
          'id': 'profile-personal',
          'type': 'TAB',
          'label': 'Osobní údaje',
          'labelKey': 'content.identity.sidebar.profile',
          'order': 10,
          'priority': 0,
          'path': '/identity/:userID/profile',
          'icon': 'user'
        }
      ]
    },
    {
      'id': 'identities',
      'labelKey': 'navigation.menu.identities.label',
      'titleKey': 'navigation.menu.identities.title',
      'icon': 'user',
      'order': 40,
      'path': '/identities',
      'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['APP_ADMIN'] } ]
    },
    {
      'id': 'system',
      'labelKey': 'navigation.menu.system',
      'icon': 'cog',
      'order': 1000,
      'path': '/configurations',
      'iconColor': '#c12e2a',
      'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['CONFIGURATION_WRITE', 'CONFIGURATIONSECURED_READ'] } ],
      'items': [
        {
          'id': 'system-configuration',
          'labelKey': 'navigation.menu.configuration',
          'icon': 'cog',
          'order': 20,
          'path': '/configurations',
          'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['CONFIGURATION_WRITE', 'CONFIGURATIONSECURED_READ'] } ]
        },
        {
          'id': 'system-modules',
          'labelKey': 'content.system.app-modules.title',
          'order': 30,
          'path': '/app-modules',
          'access': [ { 'type': 'HAS_ANY_AUTHORITY', 'authorities': [ 'APP_ADMIN' ] } ]
        }
      ]
    },
    {
      'id': 'password-change',
      'section': 'main',
      'labelKey': 'content.password.change.title',
      'order': 10,
      'path': '/password/change',
      'icon': false,
      'access': [ { 'type': 'NOT_AUTHENTICATED' } ]
    }
  ]
}
```

## Access property

Access property is used in `SecurityManager.hasAccess` for evaluating, what can logged identity see or not. Access property contains array of access items.

Access item properties:

| Type | Description | Example  |
| --- | :--- | :--- |
| DENY_ALL | Never visible | { 'type': 'DENY_ALL' } |
| PERMIT_ALL | Visible always | { 'type': 'PERMIT_ALL' } |
| NOT_AUTHENTICATED | Visible, when identity is not logged in | { 'type': 'NOT_AUTHENTICATED' } |
| IS_AUTHENTICATED | Visible, when identity is not logged in | { 'type': 'IS_AUTHENTICATED' } |
| HAS_ANY_AUTHORITY | Visible, when logged identity has at least on of given authorities |  { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['CONFIGURATION_WRITE', 'CONFIGURATIONSECURED_READ'] } |
