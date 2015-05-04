#!/bin/bash

# Publish javadoc on gh-pages
# Adopted from http://benlimmer.com/2013/12/26/automatically-publish-javadoc-to-gh-pages-with-travis-ci/

if [ "$TRAVIS_REPO_SLUG" == "asoem/greyfish" ] &&
    [ "$TRAVIS_JDK_VERSION" == "oraclejdk8" ] &&
    [ "$TRAVIS_PULL_REQUEST" == "false" ] &&
    [ "$TRAVIS_BRANCH" == "master" ]; then

  echo -e "Publishing javadoc on gh-pages...\n"

  cd "$HOME"
  git config --global user.email "travis@travis-ci.org"
  git config --global user.name "travis-ci"
  git clone --branch=gh-pages https://"$GH_TOKEN"@github.com/asoem/greyfish gh-pages

  submodules=( "greyfish-utils" "greyfish-core" "greyfish-cli-app" )
  for submodule in "${submodules[@]}"
  do
     javadoc_src_dir="$TRAVIS_BUILD_DIR/$submodule/build/docs/javadoc/"
     javadoc_dest_dir="$HOME/gh-pages/javadoc/latest/$submodule/"

     mkdir -p "$javadoc_dest_dir"
     rsync -a --delete "$javadoc_src_dir" "$javadoc_dest_dir"
  done

  cd "$HOME/gh-pages"
  git add -A
  git commit -a -m "Updated Javadoc from travis-ci build"
  git push origin gh-pages

fi