#!/bin/bash

# bash strict mode is on
set -euo pipefail
IFS=$'\n\t'

ideaVersion="2017.1"
if [ "$IDE_ID" == "2016.1.2" ]; then
    ideaVersion="2016.1.4"
elif [ "$IDE_ID" == "2016.2.1" ]; then
    ideaVersion="2016.2.5"
elif [ "$IDE_ID" == "2016.3" ]; then
    ideaVersion="2016.3.5"
elif [ "$IDE_ID" == "2016.3.1" ]; then
    ideaVersion="2016.3.5"
elif [ "$IDE_ID" == "2016.3.2" ]; then
    ideaVersion="2016.3.5"
elif [ "$IDE_ID" == "2017.1" ]; then
    ideaVersion="2017.1"
elif [ "$IDE_ID" == "eap" ]; then
    ideaVersion="163.5644.15"
fi

travisCache=".cache"

if [ ! -d ${travisCache} ]; then
    echo "Create cache" ${travisCache}
    mkdir ${travisCache}
fi

function download {

  url=$1
  basename=${url##*[/|\\]}
  cachefile=${travisCache}/${basename}

  if [ ! -f ${cachefile} ]; then
      wget $url -P ${travisCache};
    else
      echo "Cached file `ls -sh $cachefile` - `date -r $cachefile +'%Y-%m-%d %H:%M:%S'`"
  fi

  if [ ! -f ${cachefile} ]; then
    echo "Failed to download: $url"
    exit 1
  fi
}

# Unzip IDEA

if [ -d ./idea  ]; then
  rm -rf idea
fi

mkdir idea

# Download main idea folder
download "http://download.jetbrains.com/idea/ideaIU-${ideaVersion}.tar.gz"
tar zxf ${travisCache}/ideaIU-${ideaVersion}.tar.gz -C .

# Move the versioned IDEA folder to a known location
ideaPath=$(find . -name 'idea-IU*' | head -n 1)
mv ${ideaPath}/* ./idea

if [ -d ./plugins ]; then
  rm -rf plugins
fi

mkdir plugins

if [ "$IDE_ID" == "2017.1" ]; then
    download "https://plugins.jetbrains.com/files/6610/33685/php-171.3780.104.zip"
    unzip -qo $travisCache/php-171.3780.104.zip -d ./plugins
elif [ "$IDE_ID" == "eap" ]; then
    download "https://plugins.jetbrains.com/files/6610/28124/php-163.3512.10.zip"
    unzip -qo $travisCache/php-163.3512.10.zip -d ./plugins
else
    echo "Unknown IDE_ID value: $IDE_ID"
    exit 1
fi

# run tests
echo "Running from: " `pwd`
ant -f ./.travis/ant-build.xml -DIDEA_HOME=./idea || { echo "Build has failed, please check logs below"; exit 1; }