#! /bin/bash
set -x

if [ -z "$1" ]; then
    echo "Supply a previous version argument"
    exit 1
fi

if [ -z "$2" ]; then
    echo "Supply a new version argument"
    exit 1
fi

# checkout develop
git checkout develop

# exit if not clean
if ! git diff-index --quiet HEAD --; then
  echo 'You have uncommitted changes - exit'
  exit 1
fi

#update versions
cd fitpay
sed -i'.original' -e "s/version = '$1'/version = '$2'/g" build.gradle
rm *.original
cd ..

#update docs - enable once Dokka is set up
#./gradlew javadoc

# commit and push develop
git add -A
git commit -m "v$2"
git push

# switch to master and merge develop
git checkout master
git pull
git merge develop -m "v$2 merge develop"

# check for conflicts
CONFLICTS=$(git ls-files -u | wc -l)
if [ "$CONFLICTS" -gt 0 ] ; then
    echo "There is a merge conflict. Aborting"
    git merge --abort
    exit 1
fi

# push
git push

# create tag
git tag -a "v$2" -m "v$2"
git push origin "v$2"

# deploy
./gradlew bintrayUpload
