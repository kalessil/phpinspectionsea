# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure("2") do |config|
  config.vm.box = "ubuntu/trusty64"
  config.vm.provider :virtualbox do |vb|
    vb.customize ["modifyvm", :id,
        "--memory",          1024,
        "--cpus",            2,
        "--cpuexecutioncap", 100,
        "--usb",             "off",
        "--usbehci",         "off",
        "--audio",           "none"
    ]
  end

  config.vm.synced_folder ".", "/vagrant_share"

  # to get vagrant working behind proxy check this: https://stackoverflow.com/a/26800847/1879087

  config.vm.provision "shell", inline: <<-SHELL
    # prepare packages setup environment (fix swap issue causing composer failures)
    sudo /bin/dd if=/dev/zero of=/var/swap.1 bs=1M count=1024 && sudo /sbin/mkswap /var/swap.1 && sudo /sbin/swapon /var/swap.1

    # add ubuntu and composer packages
    sudo apt-get install -y php5-cli git-core htop
    wget --quiet --no-verbose https://getcomposer.org/composer.phar && sudo mv composer.phar /usr/local/bin/composer && sudo chmod +x /usr/local/bin/composer
    composer require --optimize-autoloader shopsys/phpstorm-inspect
    sudo chown -R vagrant /home/vagrant/vendor

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
    sudo chown -R vagrant /home/vagrant/.PhpStorm2016.2

    # sample project
    # git clone https://github.com/FriendsOfPHP/PHP-CS-Fixer.git
    # export project=~/PHP-CS-Fixer
    # export inspection_profile=$project/.idea/inspectionProfiles/Project_Default.xml
    # export default_inspections='<component name="InspectionProjectProfileManager"><profile version="1.0"><option name="myName" value="Project Default"/></profile></component>'
    # [ ! -e $inspection_profile ] && mkdir -p $project/.idea/inspectionProfiles && touch $inspection_profile && echo $default_inspections >$inspection_profile
    # export project_misc=$project/.idea/misc.xml
    # export default_misc='<?xml version="1.0" encoding="UTF-8"?><project version="4"><component name="ProjectRootManager" version="2"/></project>'
    # [ ! -e $project_misc ] && touch $project_misc && echo $default_misc >$project_misc
    # ~/vendor/bin/phpstorm-inspect ~/PhpStorm-*/bin/inspect.sh ~/.PhpStorm2016.2/system $project $inspection_profile $project/src

    # TODO: ensure 64 bit instance loaded (check IDE log files)
  SHELL
end
