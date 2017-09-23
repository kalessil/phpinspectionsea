# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure("2") do |config|
  config.vm.box = "ubuntu/trusty64"

  config.vm.synced_folder ".", "/vagrant_share"

  # to get vagrant working behind proxy check this: https://stackoverflow.com/a/26800847/1879087

  config.vm.provision "shell", inline: <<-SHELL
    # prepare packages setup environment (also fix swap issue causing composer failures)
    sudo /bin/dd if=/dev/zero of=/var/swap.1 bs=1M count=1024 && sudo /sbin/mkswap /var/swap.1 && sudo /sbin/swapon /var/swap.1

    # add ubuntu and composer packages
    sudo apt-get install -y php5-cli git-core
    wget --quiet --no-verbose https://getcomposer.org/composer.phar && sudo mv composer.phar /usr/local/bin/composer && sudo chmod +x /usr/local/bin/composer
    composer require --optimize-autoloader shopsys/phpstorm-inspect

    # IDE installation and plugins management
    sudo rm -rf PhpStorm-* && wget --quiet --no-verbose https://download.jetbrains.com/webide/PhpStorm-2016.2.2.tar.gz && tar -xf PhpStorm-*.tar.gz
    cd PhpStorm-*/plugins
    # drop un-needed plugins for performance sake
    ls | grep -Evi "^(css|php)?$" | xargs rm -rf
    # get new plugins installed
    wget --quiet --no-verbose https://download.plugins.jetbrains.com/7622/38887/PhpInspectionsEA.jar
    # feed the license to PhpStorm
    [ ! -e ~/.PhpStorm2016.2/system ] && mkdir -p ~/.PhpStorm2016.2/system
    [ ! -e ~/.PhpStorm2016.2/config ] && mkdir -p ~/.PhpStorm2016.2/config && cp /vagrant_share/phpstorm.key ~/.PhpStorm2016.2/config

    # sample project
    # git clone https://github.com/FriendsOfPHP/PHP-CS-Fixer.git
    # export project=~/PHP-CS-Fixer
    # export inspection_profile=$project/.idea/inspectionProfiles/Project_Default.xml
    # export default_inspections='<component name="InspectionProjectProfileManager"><profile version="1.0"><option name="myName" value="Project Default"/></profile></component>'
    # [ ! -e $inspection_profile ] && mkdir -p $project/.idea/inspectionProfiles && touch $inspection_profile && echo $default_inspections >$inspection_profile
    # ~/vendor/bin/phpstorm-inspect ~/PhpStorm-*/bin/inspect.sh ~/.PhpStorm2016.2/system $project $inspection_profile $project/src

    # TODO: ensure 64 bit instance loaded (check IDE log files)
  SHELL
end
