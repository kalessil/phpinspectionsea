# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure("2") do |config|
  config.vm.box = "ubuntu/trusty64"

  # config.vm.synced_folder "../data", "/vagrant_data"

  config.vm.provision "shell", inline: <<-SHELL
    # TODO: proxy configuration instructions
    # repositories configuration
    sudo add-apt-repository ppa:webupd8team/java -y
    sudo apt-get update

    # prepare packages setup environment
    sudo apt-get install -y python-software-properties debconf-utils
    echo "oracle-java8-installer shared/accepted-oracle-license-v1-1 select true" | sudo debconf-set-selections

    # add ubuntu and composer packages
    sudo apt-get install -y php5-cli oracle-java8-installer
    sudo wget https://getcomposer.org/composer.phar
    sudo mv composer.phar /usr/local/bin/compose
    sudo chmod +x /usr/local/bin/composer
    composer require --optimize-autoloader shopsys/phpstorm-inspect

    # IDE installation and plugins management
    sudo rm -f PhpStorm-2016.2.2.tar.gz PhpInspectionsEA.jar
    wget https://download.jetbrains.com/webide/PhpStorm-2016.2.2.tar.gz
    wget https://download.plugins.jetbrains.com/7622/38887/PhpInspectionsEA.jar

    #   transfer ~/.PhpStorm*/config/phpstorm.key onto the VM in order to activate the IDE
    # try running ./vendor/bin/phpstorm-inspect on a sample project interactively (requires inspection profile in VCS)
    # idea.sh inspect (we need to ensure 64 bit instance loaded - from IDE log files)
  SHELL
end
