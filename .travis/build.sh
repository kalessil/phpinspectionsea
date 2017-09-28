#!/bin/bash

# bash strict mode is on
set -euo pipefail
IFS=$'\n\t'

ideaVersion="2017.2"
if [ "$IDE_ID" == "2017.2" ]; then
    ideaVersion="2017.2.4"
elif [ "$IDE_ID" == "2017.1" ]; then
    ideaVersion="2017.1.5"
elif [ "$IDE_ID" == "2016.3" ]; then
    ideaVersion="2016.3.6"
elif [ "$IDE_ID" == "2016.2" ]; then
    ideaVersion="2016.2.5"
fi

travisCache=".cache"

if [ ! -d ${travisCache} ]; then
    echo "Create cache" ${travisCache}
    mkdir ${travisCache}
fi

function download {

  url=$1
  failover=$2
  basename=${url##*[/|\\]}
  cachefile=${travisCache}/${basename}

  if [ ! -f ${cachefile} ]; then
      ([[ ! -z "${failover}" ]] && cp ./.travis/failover/${failover} ${cachefile}) || wget --quiet --no-verbose $url -P ${travisCache};
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
download "http://download.jetbrains.com/idea/ideaIU-${ideaVersion}.tar.gz" ""
tar zxf ${travisCache}/ideaIU-${ideaVersion}.tar.gz -C .

# Move the versioned IDEA folder to a known location
ideaPath=$(find . -name 'idea-IU*' | head -n 1)
mv ${ideaPath}/* ./idea

if [ -d ./plugins ]; then
  rm -rf plugins
fi

mkdir plugins

if [ "$IDE_ID" == "2017.2" ]; then
    download "http://plugins.jetbrains.com/files/6610/38775/php-172.4155.41.zip" "php-172.4155.41-2017.2.zip"
    unzip -qo $travisCache/php-172.4155.41.zip -d ./plugins
elif [ "$IDE_ID" == "2017.1" ]; then
    download "http://plugins.jetbrains.com/files/6610/33685/php-171.3780.104.zip" "php-171.3780.104-2017.1.zip"
    unzip -qo $travisCache/php-171.3780.104.zip -d ./plugins
elif [ "$IDE_ID" == "2016.3" ]; then
    download "http://plugins.jetbrains.com/files/6610/31161/php-163.10504.2.zip" "php-163.10504.2-2016.3.zip"
    unzip -qo $travisCache/php-163.10504.2.zip -d ./plugins
elif [ "$IDE_ID" == "2016.2" ]; then
    download "http://plugins.jetbrains.com/files/6610/27859/php-162.1628.23.zip" "php-162.1628.23-2016.2.zip"
    unzip -qo $travisCache/php-162.1628.23.zip -d ./plugins
else
    echo "Unknown IDE_ID value: $IDE_ID"
    exit 1
fi

# run tests
echo "Running from: " `pwd`
php -f ./.travis/rules-list-check.php || { echo "RULES.md is out of date, please check logs below"; exit 1; }
ant -f ./.travis/ant-build.xml -DIDEA_HOME=./idea -lib ./lib || { echo "Build has failed, please check logs below"; exit 1; }
