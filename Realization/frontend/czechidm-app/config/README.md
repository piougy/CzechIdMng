# Configuration
* Configuration folder is separated by profile (client) and stage (development/test/production)

__For developing application with selected configuration you can use command (default value for profile is `default`  and for stage argument is `development`):__

`gulp -p default -s development`

__For building application with selected configuration you can use command (default value for profile is `default`  and for stage argument is `development`):__

`gulp build -p default -s test`

__Structure of configurations file:__

```
/project
└───config
│    │  README.md
│    │
│    └───default
│    │    │  development.json
│    │    │  test.json
│    │    │  production.json
│    │    │
│    └───client1
│    │    │  development.json
│    │    │  production.json
│    │    │  test.json
│    │    │
│    └───client2
│         │  test.json
│         │  development.json
│         │  production.json
│  ...
│  config.json (The resulting configuration file)
```

__Override module descriptor values in configuration file:__

Every module have descriptor file `module-descriptor.js`. This file describing basic configuration of this module (etc. navigations, order in menu, access right ...). For override any value from descriptor (for specific profile and stage), you can use attribute `overrideModuleDescriptor`:

IMPORTANT: You have to use same block **id** in module descriptor and configuration!
```
...
"theme": "default",
"overrideModuleDescriptor": {
  "core":{
    "navigation": {
      "items": [
        {
          "id": "tasks",
          "disabled": false
        },
        {
          "id": "user-profile",
          "order": 1000,
          "iconColor": "#000",
          "items": [
            {
              "id": "profile-personal",
              "order": 1000
            },
            {
              "id": "profile-accounts",
              "disabled": false,
              "access": [{ "type": "PERMIT_ALL"}]
            }
          ]
        }
      ]
    }
  }
},
...
```
__This example override navigation for core module:__
* Permit tab `tasks`.
* Change order for tab `user-profile`.
* Change order for sub tab `profile-personal`.
* Permit and change order and access for sub tab `profile-accounts`.

## Change configuration after build

 When frontend is build by gulp, then selected configuration by profile and stage is moved (and transformed) into application as ``config.js`` file . This file can be found in application on root path (e.g. ``dist/config.js``). Configuration in this file can be changed and will be used in application - application rebuild is not needed (just F5).
