# CzechIdM frontend

## Usage

In frontend project folder:

### Install Node.js

* **Node.js version 12.x.x** (12.16.3 verified, 14.15.5 verified) is required.
* **NPM version 6.x.x**  (6.14.11 verified) is required.

Download and install Node.js by your OS.

For linux (fedora):

`sudo dnf install nodejs`

For linux (ubuntu):

`sudo apt install nodejs`

`sudo apt install npm`

Check nodejs version:

`node -v`

Check npm version:

`npm -v`

For update nodejs from 0.x versions:
* https://nodejs.org/en/download/package-manager/#enterprise-linux-and-fedora
* http://tecadmin.net/upgrade-nodejs-via-npm/#

### Install gulp as global

> Note: If you've previously installed gulp globally, run `npm rm --global gulp` or `npm rm --global gulp-cli` before following these instructions. For more information, read [this](https://gulpjs.com/docs/en/getting-started/quick-start/).

**Gulp version 4.x.x is required.**

`npm install --global gulp-cli`

Check gulp version:

`gulp -v`

with result as:
```
CLI version: 2.3.0
Local version: Unknown
```

For Mac:

If command `gulp -v` returns ``no command ‘gulp’ found`` then try to change npm's default directory to:

`npm config set prefix /usr/local`


## Install the dependencies for application module

First go to directory **czechidm-app**. It is basic application module keep dependencies on other sub-module.
This module will start whole application.

`cd czechidm-app`

Install basic dependencies for application module (will be common for all submodules).

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

`cd projects/CzechIdM/Realization/frontend`

For Linux:

`ln -s ../../../ExternalModule/Realization/frontend/czechidm-ext`

For Windows (PowerShell):

`new-item -itemtype symboliclink -Path . -name czechidm-ext -value ../../../ExternalModule/Realization/frontend/czechidm-ext`

> Note: Prevent to commit created symlinks for optional modules into product repository.


## Make all modules together
After when we have installed all required modules, we have to copy them together. Its means create symlinks to czechidm-modules and to app node_modules.

`cd czechidm-app`

`gulp install`

## Development mode with livereload

`gulp`

__For run with specific profile and stage (default value for profile is `default`  and for stage argument is `development`):__

`gulp -p default -s test`

Tests are executed, when application starts up in development mode.

## Build

When you are done, a production ready version of the JS bundle can be created:

`gulp build -p default -s test`

Builded application will be located in `dist` folder. Application could be deployed to any http server (e.g. Apache).

## (Optional) Unmount **external** module
When we want unmount some optional module, we have to delete it (~ delete created symlinks) and then clear and make new compilation of modules.


For Linux:

`cd projects/CzechIdM/Realization/frontend`

`rm -r czechidm-ext`

`cd czechidm-app/czechidm-modules`

`rm -r czechidm-ext`

`gulp install`

For Windows (cmd):

`cd projects\CzechIdM\Realization\frontend`

`rmdir czechidm-ext`

`cd czechidm-app\czechidm-modules`

`rmdir czechidm-ext`

`gulp install`

## Check syntax by Eslint

If you want to verify that code syntax is written correctly, you can use commands below.  
Go to app module and then run:

`npm run lint`   Check syntax in app module and in all czechidm linked modules (in frontend directory).

## Update dependencies

For update NPM dependencies, you can use task 'install' in the gulp again.

`gulp install`

If you want to use command `npm install` instead `gulp install`. **You have to delete all symlinks on the CzechIdM modules** (from 'node_modules' folder) first. You can use command `npm prune` for that. This command is execute during 'gulp install' too.
