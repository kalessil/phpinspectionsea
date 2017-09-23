# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure("2") do |config|
  config.vm.box = "ubuntu/trusty64"

  config.vm.synced_folder ".", "/vagrant_share"

  # to get vagrant working behind proxy check this: https://stackoverflow.com/a/26800847/1879087

  config.vm.provision "shell", inline: <<-SHELL
    # repositories configuration
    sudo add-apt-repository ppa:webupd8team/java -y
    sudo apt-get update

    # prepare packages setup environment
    sudo apt-get install -y python-software-properties debconf-utils
    echo "oracle-java8-installer shared/accepted-oracle-license-v1-1 select true" | sudo debconf-set-selections

    # add ubuntu and composer packages
    sudo apt-get install -y php5-cli oracle-java8-installer git-core
    wget https://getcomposer.org/composer.phar && sudo mv composer.phar /usr/local/bin/compose && sudo chmod +x /usr/local/bin/composer
    composer require --optimize-autoloader shopsys/phpstorm-inspect

    # sample project
    git clone https://github.com/FriendsOfPHP/PHP-CS-Fixer.git

    # IDE installation and plugins management
    sudo rm -rf PhpStorm-* && wget https://download.jetbrains.com/webide/PhpStorm-2016.2.2.tar.gz && tar -xf PhpStorm-*.tar.gz
    cd PhpStorm-*/plugins
    # drop un-needed plugins for performance sake
    ls | grep -Evi "^(css|php)?$" | xargs rm -rf
    # get new plugins installed
    wget https://download.plugins.jetbrains.com/7622/38887/PhpInspectionsEA.jar

    # TODO: transfer ~/.PhpStorm*/config/phpstorm.key onto the VM in order to activate the IDE
    # TODO: ensure 64 bit instance loaded (check IDE log files)

    export project=~/PHP-CS-Fixer
    export inspection_profile=$project/.idea/inspectionProfiles/Project_Default.xml
    [ ! -e ~/.PhpStorm2016.2/system ] && mkdir -p ~/.PhpStorm2016.2/system
    export default_inspections='<component name="InspectionProjectProfileManager"><profile version="1.0"><option name="myName" value="Project Default"/></profile></component>'
    [ ! -e $inspection_profile ] && mkdir -p $project/.idea/inspectionProfiles && touch $inspection_profile && echo $default_inspections >$inspection_profile
    ~/vendor/bin/phpstorm-inspect ~/PhpStorm-*/bin/inspect.sh ~/.PhpStorm2016.2/system $project $inspection_profile $project/src
  SHELL
end
