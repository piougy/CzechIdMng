import fs from 'fs';
import gulp from 'gulp';
import del from 'del';
import path from 'path';
import browserify from 'browserify';
import watchify from 'watchify';
import source from 'vinyl-source-stream';
import buffer from 'vinyl-buffer';
import shell from 'gulp-shell';
import babelify from 'babelify';
import uglify from 'gulp-uglify';
import notify from 'gulp-notify';
import browserSync, { reload } from 'browser-sync';
import sourcemaps from 'gulp-sourcemaps';
import htmlReplace from 'gulp-html-replace';
import less from 'gulp-less';
import minifyCSS from 'gulp-minify-css';
import autoprefixer from 'gulp-autoprefixer';
import mocha from 'gulp-mocha';
import stringify from 'stringify';
import yargs from 'yargs';
import util from 'gulp-util';
import pathmodify from 'pathmodify';
import flatmap from 'gulp-flatmap';
import replace from 'gulp-replace';
import concat from 'gulp-concat';
import vfs from 'vinyl-fs';
import inject from 'gulp-inject';
import _ from 'lodash';
import version from 'gulp-version-number';
import packageDescriptor from './package.json';

/**
 * App build script.
 *
 * @author Vít Švanda
 * @author Radek Tomiška
 */

const paths = {
  bundle: 'app.js',
  srcJs: ['node_modules/bootstrap-less/js/bootstrap.min.js', 'node_modules/jquery/dist/jquery.min.js'],
  srcFont: ['node_modules/bootstrap-less/fonts/*', 'src/fonts/*'],
  srcWebFont: [ 'node_modules/@fortawesome/fontawesome-free/webfonts/*' ],
  srcJsx: 'src/Index.js',
  srcLess: ['src/css/main.less'],
  srcIncludedLess: [],
  srcCssSeparate: ['src/css/google.fonts.css'], // this css will be add as separate files (not conacat to main.less)
  srcImg: 'images/**', // client images
  srcThemes: ['images/**'], // only images for now
  srcLocale: [], // will filled during compile (with locales from submodules)
  srcModuleDescriptor: 'node_modules/**/module-descriptor.js',
  srcModuleAssembler: 'src/modules/moduleAssembler.js', // default modules assembler
  srcRouteAssembler: 'src/modules/routeAssembler.js', // default routes assembler
  srcComponentAssembler: 'src/modules/componentAssembler.js', // default component assembler
  testSrc: ['./czechidm-modules/**/test/localization/Localization-test.js'], // TODO: localization tests are supported only
  dist: 'dist',
  distJs: 'dist/js',
  distImg: 'dist/images',
  distCss: 'dist/css',
  distFont: 'dist/fonts',
  distWebFont: 'dist/webfonts',
  distLocale: 'dist/locales',
  distThemes: 'dist/themes',
  distModule: 'dist/modules',
  distMainNodeModules: '../node_modules',
  src: 'src/**/*.js'
};

const pathmodifyOptions = {
  mods: [
    pathmodify.mod.dir('app', path.join(__dirname, 'src'))
  ]
};
const compileMark = '// <compile mark>';
let modulesAssemblerContent = '';
let routesAssemblerContent = '';
let componentsAssemblerContent = '';

/**
 * Select environment stage and profile by input arguments.
 */
function selectStageAndProfile(done) {
  const argv = yargs
    .alias('p', 'profile')
    .alias('s', 'stage')
    .usage('Usage: $0 --profile [name of profile] --stage [development/test/production]')
    .choices('stage', ['development', 'test', 'production'])
    .help('help')
    .alias('h', 'help')
    .argv;
  let profile = argv.profile;
  if (!profile) {
    profile = 'default';
    util.log(`No profile argument present. Profile "${ profile }" will be used for build!`);
  } else {
    util.log(`Profile "${ profile }" will be used for build.`);
  }
  let stage = argv.stage;
  if (!stage) {
    stage = 'development';
    util.log(`No stage argument present. Stage "${ stage }" will be used for build!`);
  } else {
    util.log(`Stage "${ stage }" will be used for build.`);
  }
  process.env.NODE_ENV = stage;
  process.env.NODE_PROFILE = profile;
  done();
}

function clean(done) {
  del.sync([ 'dist' ]);
  done();
}

/**
 * Remove czechidm modules symlinks from node_modules
 * (prevent cyclink during "npm prune").
 */
function removeSymlinks() {
  return vfs
    .src([ '../czechidm-*' ])
    .pipe(flatmap((stream, file) => {
      const pathToSymlink = `./node_modules/${ file.basename }`;
      util.log('Symlink to delete:', pathToSymlink);
      del.sync(pathToSymlink);
      return stream;
    }));
}

/**
 * Delete app module from modules (prevent cycle).
 */
function removeAppLink(done) {
  del.sync([ './czechidm-modules/czechidm-app' ]);
  done();
}

/**
 * Clear node modules (remove links to ours modules)
 */
function npmPrune(done) {
  shell.task([ 'npm prune' ], { verbose: true, quiet: false });
  done();
}

/**
 * Npm install
 */
function npmInstall(done) {
  shell.task([ 'npm install' ], { verbose: true, quiet: false });
  done();
}

/**
 * Register node_modules for product modules.
 */
function makeProductModules() {
  return vfs.src([ '../czechidm-*' ]) // Exclusion '!../czechidm-app' not works on linux
    .pipe(flatmap((stream, file) => {
      if (!file.path.endsWith('czechidm-app')) {
        util.log('Product module found:', file.path);
        vfs.src('./node_modules')
          .pipe(vfs.symlink(`${ file.path }/`, { useJunctions: true }))
          .pipe(flatmap((streamLog, fileLog) => {
            util.log('Created symlink on main "node_modules"', util.colors.magenta(fileLog.path));
            return streamLog;
          }))
          .pipe(shell([ 'npm install' ], { verbose: true, quiet: false }));
      }
      return stream;
    }))
    .pipe(vfs.symlink('./czechidm-modules', { useJunctions: true }));
}

/**
 * Register node_modules for modules.
 */
function makeModules() {
  return vfs
    .src('./czechidm-modules/czechidm-*')
    .pipe(vfs.symlink('./node_modules', { useJunctions: true }));
}

/**
 * Load module-descriptors.
 * Move them to dist.
 * Generate content for module assembler (add to global variable).
 */
function loadModules() {
  return gulp
    .src(paths.srcModuleDescriptor)
    .pipe(flatmap((stream, file) => {
      const descriptor = require(file.path);
      if (descriptor.npmName) {
        util.log('Loaded module-descriptor with ID:', descriptor.id);
        const pathRelative = `${descriptor.npmName }/module-descriptor.js`;
        // Add row to module assembler
        modulesAssemblerContent = `${ modulesAssemblerContent } moduleDescriptors = moduleDescriptors.set("${
          descriptor.id }", require("${ pathRelative }"));\n`;
      }
      return stream;
    }));
}

/**
 * Create final module assembler
 * Add paths on module descriptors to modules assembler file.
 * Move modules assembler to dist.
 */
function createModuleAssembler() {
  return gulp
    .src(paths.srcModuleAssembler)
    .pipe(replace(compileMark, modulesAssemblerContent))
    .pipe(gulp.dest(paths.distModule));
}

/**
 * Load main styles form modules and add them to css paths array.
 */
function loadModuleStyles() {
  return gulp
    .src(paths.srcModuleDescriptor)
    .pipe(flatmap((stream, file) => {
      const descriptor = require(file.path);
      util.log('Loading style for module with ID:', descriptor.id);
      if (descriptor.mainStyleFile) {
        const fullStylePath = file.path.substring(0, file.path.lastIndexOf('module-descriptor.js')) + descriptor.mainStyleFile;
        util.log('Main module style file path:', fullStylePath);
        paths.srcIncludedLess.push(fullStylePath);
      }
      return stream;
    }));
}

/**
 * Load modules routes.
 */
function loadModuleRoutes() {
  return gulp
    .src(paths.srcModuleDescriptor)
    .pipe(flatmap((stream, file) => {
      const descriptor = require(file.path);
      if (descriptor.mainRouteFile && descriptor.npmName) {
        util.log('Loading routes for module with ID:', descriptor.id);
        const fullRoutePath = file.path.substring(0, file.path.lastIndexOf('module-descriptor.js')) + descriptor.mainRouteFile;
        util.log('Main module route file path:', fullRoutePath);
        const relativeRoutePath = `${ descriptor.npmName }/${ descriptor.mainRouteFile }`;
        // Add row to route assembler
        routesAssemblerContent = `${ routesAssemblerContent }require("${ relativeRoutePath }"),\n`;
      }
      return stream;
    }));
}

/**
 * Create final routes assembler
 * Add paths on module routes to modules assembler file.
 * Move routes assembler to dist.
 */
function createRouteAssembler() {
  return gulp
    .src(paths.srcRouteAssembler)
    .pipe(replace(compileMark, routesAssemblerContent))
    .pipe(gulp.dest(paths.distModule));
}

/**
 * Load modules components.
 */
function loadModuleComponents() {
  return gulp
    .src(paths.srcModuleDescriptor)
    .pipe(flatmap((stream, file) => {
      const descriptor = require(file.path);
      if (descriptor.mainComponentDescriptorFile && descriptor.npmName) {
        util.log('Loading components for module with ID:', descriptor.id);
        const fullComponentPath = file.path.substring(0, file.path.lastIndexOf('module-descriptor.js')) + descriptor.mainComponentDescriptorFile;
        util.log('Main module route file path:', fullComponentPath);
        const relativeComponentPath = `${ descriptor.npmName }/${ descriptor.mainComponentDescriptorFile }`;
        // Add row to component assembler
        componentsAssemblerContent = `${componentsAssemblerContent } componentDescriptors = componentDescriptors.set("${
          descriptor.id }", require("${ relativeComponentPath }"));\n`;
      }
      return stream;
    }));
}

/**
 * Create final component assembler
 * Add requires on components descriptors (for each fined module) to components assembler file.
 * Move components assembler to dist.
 */
function createComponentAssembler() {
  return gulp
    .src(paths.srcComponentAssembler)
    .pipe(replace(compileMark, componentsAssemblerContent))
    .pipe(gulp.dest(paths.distModule));
}

/**
 * Returns configuration for requestet environment
 * @param  {string} env environment
 * @return {object}     config json
 */
function getConfigByEnvironment(env = 'development', profile = 'default') {
  return require(`./config/${ profile }/${ env }.json`);
}

/**
 * Use configured theme (copy impages, less).
 */
function themes(done) {
  const configuration = getConfigByEnvironment(process.env.NODE_ENV, process.env.NODE_PROFILE);
  if (configuration.theme) {
    const themeFullPath = path.join(__dirname, '/node_modules/', configuration.theme);
    util.log('Theme will load form path:', themeFullPath);
    gulp
      .src(path.join(themeFullPath, '/images/**'))
      .pipe(gulp.dest(paths.distImg)); // Stream can not continue ...it was sometime problem during build (image directory was add as less)
    // Find theme styles and add them to srcLess array
    return gulp
      .src(path.join(themeFullPath, '/css/*.less'))
      .pipe(flatmap((stream, file) => {
        util.log('Add theme style from:', file.path);
        paths.srcIncludedLess.push(file.path);
        return stream;
      }));
  }
  done();
  return null;
}

/**
 * Select config by profile and stage.
 */
function config(done) {
  fs.writeFile(
    path.join(__dirname, paths.dist, '/config.json'),
    JSON.stringify(getConfigByEnvironment(process.env.NODE_ENV, process.env.NODE_PROFILE)),
    done
  );
}

/**
 * Externalize config - copy to config.js - can be changed after build
 */
function copyConfig(done) {
  const configuration = getConfigByEnvironment(process.env.NODE_ENV, process.env.NODE_PROFILE);
  //
  return fs.writeFile(
    path.join(__dirname, paths.dist, '/config.js'),
    `config = ${ JSON.stringify(configuration) };\n`,
    done
  );
}

/**
 * Compile less styles.
 */
function styles() {
  util.log('Main application less:', paths.srcLess);
  util.log('Less for include to main:', paths.srcIncludedLess);
  const configuration = getConfigByEnvironment(process.env.NODE_ENV, process.env.NODE_PROFILE);
  //
  return gulp
    .src(paths.srcLess)
    .pipe(sourcemaps.init())
    /**
     * Dynamically injects @import statements into the main app.less file, allowing
     * .less files to be placed around the app structure with the component
     * or page they apply to.
     */
    .pipe(inject(gulp.src(paths.srcIncludedLess, { read: false }), {
      starttag: '/* inject:imports */',
      endtag: '/* endinject */',
      transform: function transform(filepath) {
        return `@import "${ __dirname }${ filepath }";`;
      }
    }))
    .pipe(less({
      compress: true,
      globalVars: {
        ENV: configuration.env,
        version: 10,
        theme: `"${ configuration.theme }"` // wrap to quotes - less engine needs it to skip formating slash characters
      }
    }).on('error', util.log))
    .pipe(autoprefixer('last 10 versions', 'ie 9'))
    .pipe(minifyCSS({keepBreaks: false}))
    .pipe(concat('main.css'))
    .pipe(sourcemaps.write('.'))
    .pipe(gulp.dest(paths.distCss))
    .pipe(reload({stream: true}))
    .pipe(gulp.src(paths.srcCssSeparate))
    .pipe(gulp.dest(paths.distCss));
}

/**
 * Replace links to resources used in index.html.
 */
function useHtmlReplace() {
  return gulp
    .src(['index.html'])
    .pipe(htmlReplace(
      {
        icon: {
          src: `images/favicon.ico`,
          tpl: '<link rel="shortcut icon" href="%s" type="image/x-icon" />'
        },
        favicon: {
          src: `images/favicon.gif`,
          tpl: '<link rel="icon" href="%s" type="image/gif" />'
        },
        css: ['css/main.css', 'css/google.fonts.css'],
        js: ['js/jquery.min.js', 'js/bootstrap.min.js', 'config.js', 'js/app.js']
      }
    ))
    .pipe(version({
      value: _.kebabCase(packageDescriptor.version),
      append: {
        key: 'v',
        to: ['css', 'js']
      }
    }))
    .pipe(gulp.dest(paths.dist));
}

/**
 * Include images (no minify is aplied now).
 */
function images() {
  return gulp
    .src(paths.srcImg)
    .pipe(gulp.dest(paths.distImg));
}

/**
 * Include css.
 */
function js() {
  return gulp
    .src(paths.srcJs)
    .pipe(gulp.dest(paths.distJs));
}

/**
 * Include fonts.
 */
function fonts() {
  return gulp
    .src(paths.srcFont)
    .pipe(gulp.dest(paths.distFont));
}

/**
 * Include web fonts.
 */
function webfonts() {
  return gulp
    .src(paths.srcWebFont)
    .pipe(gulp.dest(paths.distWebFont));
}

/**
 * Load locales form modules and copy them to dist.
 */
function loadModuleLocales() {
  return gulp
    .src(paths.srcModuleDescriptor)
    .pipe(flatmap((stream, file) => {
      const descriptor = require(file.path);
      if (descriptor.mainLocalePath) {
        util.log('Loading locale for module with ID:', descriptor.id);
        const fullLocalesPath = path.join(
          file.path.substring(0, file.path.lastIndexOf('module-descriptor.js')),
          descriptor.mainLocalePath,
          '*.json'
        );
        util.log('Main module locale file path:', fullLocalesPath);
        //
        // For watch purpose only- pattern doesn.t work under symlinks
        paths.srcLocale.push(
          path.join(
            file.path.substring(0, file.path.lastIndexOf('module-descriptor.js')),
            descriptor.mainLocalePath,
            'cs.json'
          )
        );
        paths.srcLocale.push(
          path.join(
            file.path.substring(0, file.path.lastIndexOf('module-descriptor.js')),
            descriptor.mainLocalePath,
            'en.json'
          )
        );
        return gulp
          .src(fullLocalesPath)
          .pipe(gulp.dest(path.join(paths.distLocale, '/', descriptor.id, '/')));
      }
      return stream;
    }))
    .pipe(reload({ stream: true }));
}

/**
 * Start browser sync.
 */
function useBrowserSync(done) {
  browserSync.init({
    server: {
      baseDir: './'
    },
    ghostMode: false
  });
  done();
}

/**
 * Execute tests - tests for localization are supported now only.
 * @TODO: redesign react tests
 */
function runTest() {
  // https://www.npmjs.com/package/gulp-mocha#require
  return gulp
    .src(paths.testSrc, { read: false })
    .pipe(mocha({
      reporter: 'nyan',
      recursive: true,
      require: [ '@babel/register', 'esm', './test/setup.js'],
      ignoreLeaks: false
    }));
}

/**
 * Compile static js.
 */
function useBrowserify() {
  return browserify(paths.srcJsx)
    .plugin(pathmodify, pathmodifyOptions)
    .transform(stringify)
    .transform(babelify, {
      presets: [
        [
          '@babel/preset-env',
          {
            useBuiltIns: 'usage',
            corejs: '2.6.5'
          }
        ],
        '@babel/preset-react'
      ]
    })
    .bundle()
    .pipe(source(paths.bundle))
    .pipe(buffer())
    .pipe(
      uglify({
        compress: {
          global_defs: {
            DEBUG: false
          }
        }
      })
    )
    .pipe(gulp.dest(paths.distJs));
}

/**
 * Compile js for development - watch source code changes.
 */
function useWatchify() {
  const bundler = watchify(
    browserify(paths.srcJsx, { ...watchify.args, debug: true })
      .plugin(pathmodify, pathmodifyOptions)
      .transform(stringify)
  );

  function rebundle() {
    return bundler
      .bundle()
      .on('error', notify.onError())
      .pipe(source(paths.bundle))
      .pipe(buffer())
      .pipe(sourcemaps.init({ loadMaps: true }))
      .pipe(sourcemaps.write('.'))
      .pipe(gulp.dest(paths.distJs))
      .pipe(reload({ stream: true }));
  }
  //
  bundler
    .transform(babelify, {
      presets: [
        [
          '@babel/preset-env',
          {
            useBuiltIns: 'usage',
            corejs: '2.6.5'
          }
        ],
        '@babel/preset-react'
      ]
    })
    .on('update', rebundle);
  //
  return rebundle();
}

/**
 * Watch other files than rjs/eact files - e.g. css, locales.
 */
function useWatch() {
  gulp.watch(paths.srcLess, styles);
  gulp.watch(paths.srcIncludedLess, styles);
  gulp.watch(paths.srcLocale, loadModuleLocales);
}

/**
 * Install node libraries (after first npm install).
 */
exports.install = gulp.series(
  clean,
  removeSymlinks,
  npmPrune,
  npmInstall,
  makeProductModules,
  removeAppLink
);

/**
 * Build atrefact for distribution.
 */
exports.build = gulp.series(
  selectStageAndProfile,
  clean,
  removeAppLink,
  makeModules,
  loadModules,
  createModuleAssembler,
  loadModuleStyles,
  loadModuleRoutes,
  createRouteAssembler,
  loadModuleComponents,
  createComponentAssembler,
  themes,
  config,
  copyConfig,
  styles,
  useHtmlReplace,
  images,
  js,
  fonts,
  webfonts,
  loadModuleLocales,
  useBrowserify
);

/**
 * Watch ~ for development by default.
 */
exports.default = gulp.series(
  selectStageAndProfile,
  clean,
  removeAppLink,
  makeModules,
  loadModules,
  createModuleAssembler,
  loadModuleStyles,
  loadModuleRoutes,
  createRouteAssembler,
  loadModuleComponents,
  createComponentAssembler,
  themes,
  config,
  copyConfig,
  styles,
  images,
  js,
  fonts,
  webfonts,
  loadModuleLocales,
  runTest,
  useBrowserSync,
  useWatchify,
  useWatch
);
