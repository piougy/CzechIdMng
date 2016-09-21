# CzechIdM frontend


## Usage

In frontent project folder:

### Install Node.js

**Node.js version 4.x or higher is required (npm version 3.x or higher is required).** Download and install Node.js by your OS.

For linux (fedora):

`sudo dnf install nodejs`

Check nodejs version:

`node -v`

or

`nodejs -v`

Check npm version:

`npm -v`

For update nodejs from 0.x versions:
* https://nodejs.org/en/download/package-manager/#enterprise-linux-and-fedora
* http://tecadmin.net/upgrade-nodejs-via-npm/#

### Install gulp

Globally as root:

`sudo npm install -g gulp`

or locally:

`npm install gulp`

## Install the dependencies for client module

First go to directory **czechidm-client**. It is basic application module keep dependencies on other sub-module.
This module will start whole application.

`cd czechidm-client`

Run script **modules-link** defined in package.json. This script will create directory **node_modules** in parent directory and create symlink on him in **czechidm-client**. This prevents the problem with multiple copies of React (https://facebook.github.io/react/warnings/refs-must-have-owner.html). The goal is to have only one node_modules directory (for all ours modules) with React.

`npm run modules-link`

Install basic dependencies for client module (will be common for all submodules ).

`npm install`

## Install the dependencies for core module

Now we need to install mandatory core module. Go to core directory. You can use symlink in czechidm-modules.

`cd czechidm-modules/czechidm-core`

Install dependencies for production scope. It is important for prevent problem with multiple copies of React. In production dependency scope is not React present.

`npm install --production`

Go to client module.

`cd ../../`

## (Optional) Install the dependencies for other sub modules

We can install other application modules. For example we will install optional account management module **czechidm-acc**.

All application modules are in **czechidm-modules** directory (in czechidm-client). Go to him and create symlink on acc module.

`cd czechidm-modules`
`ln -s ../../czechidm-acc`

Go to the acc module. You can use symlink in czechidm-modules.

`cd czechidm-acc`

Install dependencies for production scope.

`npm install --production`

Go to client module.

`cd ../../`

## Make all modules together
After when we have installed all required modules, we have to copy them together. Its means create symlinks from czechidm-modules to client node_modules.

`gulp makeModules`

## Test

`npm run test`
or better
`npm run test-watch`

__Test via gulp (for profile "default" and stage "test". Profile and stage arguments are supported. Profiles could be defined in [configuration](./config)):__

`gulp test -p default -s test`

or for livereload (check src and test dir)

`gulp test -w`  (profile and stage arguments are not supported)

## Development mode with livereload

`gulp`

__For run with specific profile and stage (default value for profile is `default`  and for stage argument is `development`):__

`gulp -p default -s test`

## Build

When you are done, a production ready version of the JS bundle can be created:

`gulp build -p default -s test`

Builded application will be located in `dist` folder. Application could be deployed to any http server (e.g. Apache).

## Unmount submodules
When we want unmount some optional module, we have to delete it (or his symlink) from czechidm-modules. Then clear all modules from client node_modules and make new compilation of modules.

`rm -r czechidm-modules/czechidm-acc`
`npm prune`
`gulp makeModules`

### [Docs](./docs/README.md)


### npm link for components development (draft)
* create module: https://docs.npmjs.com/getting-started/creating-node-modules
* publish module https://docs.npmjs.com/getting-started/scoped-packages
* Nexus: https://books.sonatype.com/nexus-book/reference/npm-deploying-packages.html
* npm link:
  * https://docs.npmjs.com/cli/link
  * http://justjs.com/posts/npm-link-developing-your-own-npm-modules-without-tears
