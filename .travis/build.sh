#!/bin/bash

# bash strict mode is on
set -euo pipefail
IFS=$'\n\t'

# trap errors, so failed lines are getting reported
function handle_error {
    local retval=$?
    local line=$1
    echo "Failed at $line: $BASH_COMMAND"
    exit $retval
}
trap 'handle_error $LINENO' ERR

ideUrl="IU-2017.3"
if [ "$IDE_ID" == "IU-2018.1" ]; then
    ideUrl="http://download.jetbrains.com/idea/idea$IDE_ID.4.tar.gz"
elif [ "$IDE_ID" == "IU-2017.3" ]; then
    ideUrl="http://download.jetbrains.com/idea/idea$IDE_ID.3.tar.gz"
elif [ "$IDE_ID" == "IU-2017.2" ]; then
    ideUrl="http://download.jetbrains.com/idea/idea$IDE_ID.6.tar.gz"
elif [ "$IDE_ID" == "IU-2017.1" ]; then
    ideUrl="http://download.jetbrains.com/idea/idea$IDE_ID.5.tar.gz"
elif [ "$IDE_ID" == "IU-2016.3" ]; then
    ideUrl="http://download.jetbrains.com/idea/idea$IDE_ID.6.tar.gz"
elif [ "$IDE_ID" == "IU-2016.2" ]; then
    ideUrl="http://download.jetbrains.com/idea/idea$IDE_ID.5.tar.gz"
fi

travisCache=".cache"

if [ ! -d ${travisCache} ]; then
    echo "Create cache" ${travisCache}
    mkdir ${travisCache}
fi

function download {

  ideUrl=$1
  failover=$2
  cachefile=${travisCache}/${ideUrl##*/}

  if [ ! -f ${cachefile} ]; then
      ([[ ! -z "${failover}" ]] && cp ./.travis/failover/${failover} ${cachefile}) || wget --quiet --no-verbose $ideUrl -P ${travisCache};
    else
      echo "Cached file `ls -sh $cachefile` - `date -r $cachefile +'%Y-%m-%d %H:%M:%S'`"
  fi

  if [ ! -f ${cachefile} ]; then
    echo "Failed to download: $ideUrl"
    exit 1
  fi
}

# Unzip IDEA

if [ -d ./idea  ]; then
  rm -rf idea
fi
mkdir idea

# Download main idea folder
download "$ideUrl" ""
tar zxf ${travisCache}/${ideUrl##*/} -C .
echo "Successfully downloaded $ideUrl -> ${travisCache}/${ideUrl##*/}"

# Move the versioned IDEA folder to a known location
ideaPath=$(find . -name 'idea-*' | head -n 1)
mv ${ideaPath}/* ./idea

if [ -d ./plugins ]; then
  rm -rf plugins
fi
mkdir plugins

if [ "$IDE_ID" == "IU-2018.1" ]; then
    download "http://plugins.jetbrains.com/files/6610/44552/php-181.4203.565.zip" "php-181.4203.565-2018.1.zip"
    unzip -qo $travisCache/php-181.4203.565.zip -d ./plugins
elif [ "$IDE_ID" == "IU-2017.3" ]; then
    download "http://plugins.jetbrains.com/files/6610/42364/php-173.4301.34.zip" "php-173.4301.34-2017.3.zip"
    unzip -qo $travisCache/php-173.4301.34.zip -d ./plugins
elif [ "$IDE_ID" == "IU-2017.2" ]; then
    download "http://plugins.jetbrains.com/files/6610/38775/php-172.4155.41.zip" "php-172.4155.41-2017.2.zip"
    unzip -qo $travisCache/php-172.4155.41.zip -d ./plugins
elif [ "$IDE_ID" == "IU-2017.1" ]; then
    download "http://plugins.jetbrains.com/files/6610/33685/php-171.3780.104.zip" "php-171.3780.104-2017.1.zip"
    unzip -qo $travisCache/php-171.3780.104.zip -d ./plugins
elif [ "$IDE_ID" == "IU-2016.3" ]; then
    download "http://plugins.jetbrains.com/files/6610/31161/php-163.10504.2.zip" "php-163.10504.2-2016.3.zip"
    unzip -qo $travisCache/php-163.10504.2.zip -d ./plugins
elif [ "$IDE_ID" == "IU-2016.2" ]; then
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
