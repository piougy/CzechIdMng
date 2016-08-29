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

  | Parameter | Type | Description | Default  |
  | --- | :--- | :--- | :--- |
  | rendered  | bool |  | true |
  | showLoading  | bool | Shows loading icon  (fa-refresh fa-spin) | false |

  | Parameter | Description                                                                                 |
  |-----------|---------------------------------------------------------------------------------------------|
  | id        | Component identifier (key). With this key will be component loaded. It must be unique.      |

    |Parameter|Description|
    |---|---|
    |id|Component identifier (key). With this key will be component loaded. It must be unique. |



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
import ComponentService from '/services/ComponentService';
DetailComponent = componentService.getComponent('someComponentKey');
```

#### Get components by specific type

```javascript
import ComponentService from '/services/ComponentService';
let dashboards = [];
dashboards = this.componentService.getComponentDefinitions('dashboard');
```

# Module descriptor
//TODO
