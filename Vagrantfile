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

    # get IDE and plugins
    # extract IDE, drop it's plugin folder (except php,css) add PhpInspectionsEA.jar there
    # try running ./vendor/bin/phpstorm-inspect on a sample project interactively (requires inspection profile in VCS)
    # idea.sh inspect (we need to ensure 64 bit instance loaded - from IDE log files)
  SHELL
end
