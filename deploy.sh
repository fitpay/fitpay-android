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

git checkout develop

# exit if not clean
if ! git diff-index --quiet HEAD --; then
  echo 'You have uncommitted changes - exit'
  exit 1
fi

#update docs

#update versions
cd fitpay
sed -i'.original' -e "s/$1/$2/g" build.gradle
rm *.original
cd ..

git process
git add -A
git commit -m "v$2"
git push

git checkout master
git pull
git merge develop -m "v$2 merge develop"
git push

#deploy
#./gradlew bintrayUpload
