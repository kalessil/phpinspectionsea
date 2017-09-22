# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure("2") do |config|
  config.vm.box = "ubuntu/trusty64"

  # config.vm.synced_folder "../data", "/vagrant_data"

  config.vm.provision "shell", inline: <<-SHELL
    sudo add-apt-repository ppa:webupd8team/java
    sudo apt-get update
    sudo apt-get install -y php5-cli oracle-java8-installer
  SHELL
end
