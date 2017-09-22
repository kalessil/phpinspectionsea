# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure("2") do |config|
  config.vm.box = "ubuntu/trusty64"

  # config.vm.synced_folder "../data", "/vagrant_data"

  config.vm.provision "shell", inline: <<-SHELL
    # repositories configuration
    sudo add-apt-repository ppa:webupd8team/java -y
    sudo apt-get update

    # prepare packages setup environment
    sudo apt-get install -y python-software-properties debconf-utils
    echo "oracle-java8-installer shared/accepted-oracle-license-v1-1 select true" | sudo debconf-set-selections

    # add packages
    sudo apt-get install -y php5-cli oracle-java8-installer

    # get IDE and plugins
    # extract IDE, manage active plugins
    # get phar, require globally kalessil/phpstorm-inspect
  SHELL
end
