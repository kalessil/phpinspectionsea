
[![Version](https://img.shields.io/jetbrains/plugin/v/7622-php-inspections-ea-extended-.svg)](https://plugins.jetbrains.com/plugin/7622-php-inspections-ea-extended-)
[![Build Status](https://travis-ci.org/kalessil/phpinspectionsea.svg?branch=master)](https://travis-ci.org/kalessil/phpinspectionsea)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/7622-php-inspections-ea-extended-.svg)](https://plugins.jetbrains.com/plugin/7622-php-inspections-ea-extended-)
[![Downloads last month](http://phpstorm.espend.de/badge/7622/last-month)](https://plugins.jetbrains.com/plugin/7622)
[![Donate to this project using Paypal](https://img.shields.io/badge/paypal-donate-yellow.svg)](https://www.paypal.me/VReznichenko)
[![Donate to this project using Patreon](https://img.shields.io/badge/patreon-donate-yellow.svg)](https://www.patreon.com/kalessil)

Php Inspections (EA Extended)
---
<img src="docs/images/ea-logo.png" alt="Php Inspections (EA Extended)" height="100" />

This project is an OSS Static Code Analysis [tool](https://plugins.jetbrains.com/plugin/7622-php-inspections-ea-extended-) for PhpStorm (2016.2+) and Idea Ultimate.

It covers:
- architecture related issues
- weak types control and possible code construct simplifications
- performance issues
- non-optimal, duplicate and suspicious "if" conditions
- validation of magic methods usage
- regular expressions
- validation of exception handling workflow
- compatibility issues
- variety of time-consuming bugs
- PHPUnit API usage
- security issues

Some of inspections are expecting conditional statements (e.g. "if") to use group statement for wrapping body 
expressions. If this requirement is met then additional inspections are applied to the source code.

On some projects CPU and therefore battery usage could be intensive, so it should be taken into account when traveling

Installation and getting started
---
Please follow this [documentation link](docs/getting-started.md).

Enhancing the experience
---
Once you have accommodated with the analyzer and want move to further, check [Php Inspections (EA Ultimate)](http://plugins.jetbrains.com/plugin/10215-php-inspections-ea-ultimate-) out.

Crowdfunding
---

It's also possible to support the project on [Patreon](https://www.patreon.com/kalessil) or by buying a 
[Php Inspections (EA Ultimate)](http://plugins.jetbrains.com/plugin/10215-php-inspections-ea-ultimate-) license.

This funding is used for maintaining the project and adding new features into Code Style, PHPUnit and similar inspections 
groups of Php Inspections (EA Extended).

Acknowledgments
---

<a href="https://shopware.com/"><img src="https://de.shopware.com/media/unknown/e1/b0/93/shopware_logo_blue.svg" alt="Shopware" height="20"></a> Shopware is the biggest supporter of our 2017 <a href="https://www.indiegogo.com/projects/php-inspections-ea-extended-a-code-analyzer-security#/">crowdfunding campaign</a>.

<a href="https://pixelandtonic.com/"><img src="https://pixelandtonic.com/assets/images/pixelandtonic.svg" alt="Pixel & Tonic" height="20"></a> Pixel & Tonic are supporting us since 2017.

<a href="https://www.cellsynt.com"><img src="https://d22z914jmqt4fj.cloudfront.net/images/logo.gif" alt="Cellsynt" height="20"></a> Cellsynt is a supporter of our 2017 <a href="https://www.indiegogo.com/projects/php-inspections-ea-extended-a-code-analyzer-security#/">crowdfunding campaign</a>.

<a href="https://roave.com"><img src="https://roave.com/themes/ruby-on-roave/images/roave-logo-tiny.svg" alt="Roave" height="20"></a> Roave LLC is a supporter of our 2017 <a href="https://www.indiegogo.com/projects/php-inspections-ea-extended-a-code-analyzer-security#/">crowdfunding campaign</a>.

<a href="http://www.syrcon.com"><img src="http://www.syrcon.com/wp-content/uploads/2016/10/syrcon_Logo_web-Sr_dark.png" alt="Syrcon GmbH" height="20"></a> Syrcon GmbH is a supporter of our 2017 <a href="https://www.indiegogo.com/projects/php-inspections-ea-extended-a-code-analyzer-security#/">crowdfunding campaign</a>.

<a href="https://www.yourkit.com"><img src="https://www.yourkit.com/images/yklogo.png" alt="YourKit" height="20"></a> YourKit supports us with their full-featured [Java Profiler](https://www.yourkit.com/java/profiler/).

<a href="https://jetbrains.com"><img src="/docs/images/jetbrains-variant-4.png" alt="JetBrains" height="20" /></a>JetBrains supports us with their awesome IDEs.

Project activity and various stats: https://www.openhub.net/p/phpinspectionsea
