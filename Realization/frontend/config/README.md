# Configuration
* Configuration folder is separated by profile (client) and stage (development/test/production)

__For build (select configuration json) you can use command:__

`gulp -p default -s test` (default values of profile and stage arguments are default/development)

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
"crtEncrytpEnable": false,
"crtSigningEnable": true,
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
