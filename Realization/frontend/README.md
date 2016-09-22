# CzechIdM frontend


## Usage

In frontent project folder:

### Install Node.js

**Node.js version 4.x or higher is required (npm version 3.6 or higher is required).** Download and install Node.js by your OS.

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

**IMPORTANT!** Module-link script does not work on Windows. If are you Windows user, then you have to create symlink on node_modules (in parent directory) manually (use command 'mklink /D').

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

We can install other application modules. We will install optional example module **czechidm-example**.

All application modules are in **czechidm-modules** directory (in czechidm-client). Go to him and create symlink on example module.

**IMPORTANT!** If are you Windows user, then you have to create symlink with command 'mklink /D'.

`cd czechidm-modules`

`ln -s ../../czechidm-example`

Go to the example module. You can use symlink in czechidm-modules.

`cd czechidm-example`

Install dependencies for production scope.

`npm install --production`

Go to client module.

`cd ../../`

## Make all modules together
After when we have installed all required modules, we have to copy them together. Its means create symlinks from czechidm-modules to client node_modules.

`gulp makeModules`

## Test

`gulp test`

For watch use test-watch (will work after compiling application ... it means after run "gulp" or "gulp build" or "gulp test")

`npm run test-watch`

__Test via gulp (for profile "default" and stage "test". Profile and stage arguments are supported. Profiles could be defined in [configuration](./czechidm-client/config)):__

`gulp test -p default -s test`

## Development mode with livereload

`gulp`

__For run with specific profile and stage (default value for profile is `default`  and for stage argument is `development`):__

`gulp -p default -s test`

## Build

When you are done, a production ready version of the JS bundle can be created:

`gulp build -p default -s test`

Builded application will be located in `dist` folder. Application could be deployed to any http server (e.g. Apache).

## Unmount submodule
When we want unmount some optional module, we have to delete it (or his symlink) from czechidm-modules. Then clear all modules from client node_modules and make new compilation of modules.

`rm -r czechidm-modules/czechidm-example`

`npm prune`

`gulp makeModules`
