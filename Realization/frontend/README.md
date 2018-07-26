# CzechIdM frontend

## Usage

In frontent project folder:

### Install Node.js

**Node.js version 4.x.x is required (npm version 3.6 or higher is required).** Download and install Node.js by your OS.

For linux (fedora):

`sudo dnf install nodejs`

Check nodejs version:

`node -v`

Check npm version:

`npm -v`

For update nodejs from 0.x versions:
* https://nodejs.org/en/download/package-manager/#enterprise-linux-and-fedora
* http://tecadmin.net/upgrade-nodejs-via-npm/#

### Install gulp as global

**Gulp version 3.9.0 is required.**

`npm install gulp@3.9.0 -g`

Check gulp version:

`gulp -v`


## Install the dependencies for application module

First go to directory **czechidm-app**. It is basic application module keep dependencies on other sub-module.
This module will start whole application.

`cd czechidm-app`

Install basic dependencies for application module (will be common for all submodules ).

`npm install`

This creates directory 'node_modules' in the 'czechidm-app' directory.

## Install product modules

For install product modules, you need to execute task 'install' in the gulp.

`gulp install`

This installs all product modules that are in the parent folder and begin with 'czechidm-' (core, acc, vs, ...).
All this modules will have symlinks on main 'node_modules' directory in the 'czechidm-app'. For all thes modules will be automaticaly call command 'npm install'.
On all thes modules will be created symlinks from the 'czechidm-app/czechidm-modules' too.

If you hasn't installed GIT on your system, you will need install [this service](https://git-scm.com/downloads). Gulp doesn't works well without GIT.

## (Optional) Install the dependencies for **external** module
If you are developing a custom module (for example named as "czechidm-ext") that is not part of our product, you need to do the following:

* We have the product installed in the **projects/CzechIdM/** folder.
* The **czechidm-app** module is in the **projects/CzechIdM/Realization/frontend/czechidm-app/**.
* For example, the externally developed module (frontend part) **czechidm-ext** is in the **projects/ExternalModule/Realization/frontend/czechidm-ext/** folder.

First, we create the symlink to the "czechidm-ext" module:

`cd projects/CzechIdM/Realization/frontend/czechidm-app/czechidm-modules`

`ln -s ../../../../../ExternalModule/Realization/frontend/czechidm-ext`

Then we create a symlink from the external module to the product "node_modules". This prevents the problem with multiple copies of React (https://facebook.github.io/react/warnings/refs-must-have-owner.html).
The goal is to have only one node_modules directory (for all modules) with React.

`cd ../../../../../ExternalModule/Realization/frontend/czechidm-ext`

`ln -s ../../../../CzechIdM/Realization/frontend/czechidm-app/node_modules`

## Make all modules together
After when we have installed all required modules, we have to copy them together. Its means create symlinks from czechidm-modules to app node_modules.

`gulp makeModules`

## Test

`gulp test`

For watch use test-watch (will work after compiling application ... it means after run "gulp" or "gulp build" or "gulp test")

`npm run test-watch`

__Test via gulp (for profile "default" and stage "test". Profile and stage arguments are supported. Profiles could be defined in [configuration](./czechidm-app/config)):__

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
When we want unmount some optional module, we have to delete it (or his symlink) from czechidm-modules. Then clear all modules from app node_modules and make new compilation of modules.

`rm -r czechidm-modules/czechidm-example`

`npm prune`

`gulp makeModules`

## Check syntax by Eslint
**IMPORTANT!** Syntax verify is executed during each startup and build application via gulp.

If you want to verify that code syntax is written correctly, you can use commands below.  
Go to app module and then run:

`npm run lint`   Check syntax in app module and in all czechidm linked modules (in directory czechidm-modules).

`gulp lint`  Do same check as previous command, but run as gulp task.

## Update dependencies

For update NPM dependencies, you can use task 'install' in the gulp again.

`gulp install`

If you want to use command `npm install` instead `gulp install`. **You have to delete all symlinks on the CzechIdM modules** (from 'node_modules' folder) first. You can use command `npm prune` for that. This command is execute during 'gulp install' too. 
