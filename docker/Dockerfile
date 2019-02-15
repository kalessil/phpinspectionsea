FROM php:7.3

ENV PHPSTORMVERSION=2018.3 \
    PHPSTORMDOWNLOAD=2018.3.3

LABEL org.label-schema.name="netformic-phpinspections" \
      org.label-schema.version="0.9.0" \
      org.label-schema.description="headless PHPStorm + PHP Inspections plugin" \
      org.label-schema.vendor="NETFORMIC GmbH" \
      org.label-schema.schema-version="1.0" \
      org.label-schema.vcs-url="" \
      org.label-schema.docker.cmd="docker run --rm -v /my/project:/var/ci/project netformic-phpinspections"

RUN apt-get update && apt-get install -y \
  wget git unzip libfreetype6

RUN cd root; mkdir -p .PhpStorm${PHPSTORMVERSION}/config .PhpStorm${PHPSTORMVERSION}/system /var/ci/project
RUN wget --quiet --no-verbose https://download.jetbrains.com/webide/PhpStorm-${PHPSTORMDOWNLOAD}.tar.gz
RUN tar xvf PhpStorm-*.tar.gz
RUN cd PhpStorm-*/plugins && ls | grep -Evi "^(css|php)?$" | xargs rm -rf
RUN wget --quiet --no-verbose https://plugins.jetbrains.com/plugin/download?rel=true&updateId=53701
RUN cd -
RUN wget --quiet --no-verbose https://getcomposer.org/composer.phar && mv composer.phar /usr/local/bin/composer && chmod +x /usr/local/bin/composer
RUN composer require --optimize-autoloader kalessil/phpstorm-inspect

COPY phpstorm.key /root/.PhpStorm${PHPSTORMVERSION}/config
COPY entrypoint.sh /root/

CMD ["/root/entrypoint.sh"]
