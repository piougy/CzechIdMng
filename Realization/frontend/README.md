# CzechIdM frontend


## Usage

In project folder:

### Install Node.js

`sudo dnf install nodejs`

__Node.js version 4.x or higher is required.__

Check nodejs version:

`nodejs -v`

For update nodejs from 0.x versions:
* https://nodejs.org/en/download/package-manager/#enterprise-linux-and-fedora
* http://tecadmin.net/upgrade-nodejs-via-npm/#

### Install gulp

Globally as root:

`sudo npm install -g gulp`

or locally:

`npm install gulp`

## Install the dependencies

`npm install`

## Test

`npm run test`
or better
`npm run test-watch`

__ Test via gulp:__

`gulp test -p koop -s test` (profile and stage arguments are supported)

or for livereload (check src and test dir)

`gulp test -w`  (profile and stage arguments are not supported)

### Development mode with livereload

`gulp`

__For run with specific profile and stage:__

`gulp -p koop -s test` (default values of profile and stage arguments are default/development)

### Build

When you are done, create a production ready version of the JS bundle:

`gulp build -p koop -s test` (default values of profile and stage arguments are default/development)


### npm link for components development (draft)
* create module: https://docs.npmjs.com/getting-started/creating-node-modules
* publish module https://docs.npmjs.com/getting-started/scoped-packages
* Nexus: https://books.sonatype.com/nexus-book/reference/npm-deploying-packages.html
* npm link:
  * https://docs.npmjs.com/cli/link
  * http://justjs.com/posts/npm-link-developing-your-own-npm-modules-without-tears
