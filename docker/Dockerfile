FROM php:7.0

ENV PHPSTORMVERSION=2017.3 \
    PHPSTORMDOWNLOAD=2017.3.1

LABEL org.label-schema.name="netformic-phpinspections" \
      org.label-schema.version="0.9.0" \
      org.label-schema.description="headless PHPStorm + PHP Inspections plugin" \
      org.label-schema.vendor="NETFORMIC GmbH" \
      org.label-schema.schema-version="1.0" \
      org.label-schema.vcs-url="" \
      org.label-schema.docker.cmd="docker run --rm -v /my/project:/var/ci/project netformic-phpinspections"      

RUN apt-get update && apt-get install -y \
  wget git unzip libfreetype6

RUN cd root; mkdir -p .PhpStorm${PHPSTORMVERSION}/config .PhpStorm${PHPSTORMVERSION}/system /var/ci/project \
&& wget --quiet --no-verbose https://download.jetbrains.com/webide/PhpStorm-${PHPSTORMDOWNLOAD}.tar.gz \
&& tar -xf PhpStorm-*.tar.gz \
&& cd PhpStorm-*/plugins && ls | grep -Evi "^(css|php)?$" | xargs rm -rf \
&& wget --quiet --no-verbose https://download.plugins.jetbrains.com/7622/38887/PhpInspectionsEA.jar \
&& cd - \
&& wget --quiet --no-verbose https://getcomposer.org/composer.phar && mv composer.phar /usr/local/bin/composer && chmod +x /usr/local/bin/composer \
&& composer require --optimize-autoloader kalessil/phpstorm-inspect

COPY phpstorm.key /root/.PhpStorm${PHPSTORMVERSION}/config/
COPY entrypoint.sh /root/

CMD ["/root/entrypoint.sh"]
