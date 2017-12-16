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
    ### Set environment up (fix swap issue causing composer failures) ###
    export home_directory=/home/vagrant
    sudo /bin/dd if=/dev/zero of=/var/swap.1 bs=1M count=1024 && sudo /sbin/mkswap /var/swap.1 && sudo /sbin/swapon /var/swap.1
    sudo apt-get install -y php5-cli php5-curl git-core htop
    wget --quiet --no-verbose https://getcomposer.org/composer.phar && sudo mv composer.phar /usr/local/bin/composer && sudo chmod +x /usr/local/bin/composer

    ### IDE installation and plugins management ###
    export phpstorm_preferences=$home_directory/.PhpStorm2016.2
    sudo rm -rf PhpStorm-* && wget --quiet --no-verbose https://download.jetbrains.com/webide/PhpStorm-2016.2.2.tar.gz && tar -xf PhpStorm-*.tar.gz
    sudo chown -R vagrant PhpStorm-*
    cd PhpStorm-*/plugins && ls | grep -Evi "^(css|php)?$" | xargs rm -rf
    wget --quiet --no-verbose https://download.plugins.jetbrains.com/7622/41075/PhpInspectionsEA.jar

    ### Feed license to PhpStorm ###
    # TODO: place phpstorm.key from your ~/.PhpStorm2016.2/config near this vagrant file (that's your license)
    [ ! -e $phpstorm_preferences/system ] && mkdir -p $phpstorm_preferences/system
    [ ! -e $phpstorm_preferences/config ] && mkdir -p $phpstorm_preferences/config && cp /vagrant_share/phpstorm.key $phpstorm_preferences/config
    sudo chown -R vagrant $phpstorm_preferences && sudo ln -s $phpstorm_preferences /root/.PhpStorm2016.2

    ### Install a wrapper for running inspections ###
    cd $home_directory
    composer require --optimize-autoloader kalessil/phpstorm-inspect && sudo chown -R vagrant $home_directory/vendor

    ### Sample project inspection; the part for moving into e.g. Jenkins ###
    export project=$home_directory/PHP-CS-Fixer
    git clone https://github.com/FriendsOfPHP/PHP-CS-Fixer.git && sudo chown -R vagrant $project
    # you can install project deps as well, this is recommended (additional packages might be needed, see line 25)
    # [ -e $project/composer.json ] && cd $project && composer install --no-dev
    $home_directory/vendor/bin/phpstorm-inspect \
        $home_directory/PhpStorm-*/bin/inspect.sh  $phpstorm_preferences/system \
        $project  $project/.idea/inspectionProfiles/Project_Default.xml  $project/src  checkstyle
    # `... checkstyle > <file-name>.xml` will store results in a file of your choice
  SHELL
end
