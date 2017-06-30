[![Version](http://phpstorm.espend.de/badge/7622/version)](https://plugins.jetbrains.com/plugin/7622)
[![Build Status](https://img.shields.io/travis/kalessil/phpinspectionsea.svg?branch=master)](https://travis-ci.org/kalessil/phpinspectionsea)
[![Downloads](http://phpstorm.espend.de/badge/7622/downloads)](https://plugins.jetbrains.com/plugin/7622)
[![Downloads last month](http://phpstorm.espend.de/badge/7622/last-month)](https://plugins.jetbrains.com/plugin/7622)
[![Donate to this project using Paypal](https://img.shields.io/badge/paypal-donate-yellow.svg)](https://www.paypal.me/VReznichenko)
[![Donate to this project using Patreon](https://img.shields.io/badge/patreon-donate-yellow.svg)](https://www.patreon.com/kalessil)

Php Inspections (EA Extended)
---
<img src="https://user-images.githubusercontent.com/47294/26991958-2a7ee9f4-4d65-11e7-8d60-f57ca8cbb46a.png" alt="Php Inspections (EA Extended)" height="100" />

This project is a Static Code Analysis tool for PhpStorm and Idea Ultimate.

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
- PhpUnit API usage
- security issues

Some of inspections are expecting conditional statements (e.g. "if") to use group statement for wrapping body 
expressions. If this requirement is met then additional inspections are applied to the source code.

On some projects CPU and therefore battery usage could be intensive, so it should be taken into account when traveling


Installation and getting started
---
Please follow this [documentation link](docs/getting-started.md).

Acknowledgments
---

<a href="https://shopware.com/"><img src="https://de.shopware.com/media/image/shopware_logo_blue.png" alt="Shopware" height="20"></a> Shopware is the biggest supporter of our 2017 <a href="https://www.indiegogo.com/projects/php-inspections-ea-extended-a-code-analyzer-security#/">crowdfunding campaign</a>.

<a href="https://www.cellsynt.com"><img src="https://d22z914jmqt4fj.cloudfront.net/images/logo.gif" alt="Cellsynt" height="20"></a> Cellsynt is a supporter of our 2017 <a href="https://www.indiegogo.com/projects/php-inspections-ea-extended-a-code-analyzer-security#/">crowdfunding campaign</a>.

<a href="https://roave.com"><img src="https://roave.com/themes/ruby-on-roave/images/roave-logo-tiny.svg" alt="Roave" height="20"></a> Roave LLC is a supporter of our 2017 <a href="https://www.indiegogo.com/projects/php-inspections-ea-extended-a-code-analyzer-security#/">crowdfunding campaign</a>.

<a href="http://www.syrcon.com"><img src="http://www.syrcon.com/wp-content/uploads/2016/10/syrcon_Logo_web-Sr_dark.png" alt="Syrcon GmbH" height="20"></a> Syrcon GmbH is a supporter of our 2017 <a href="https://www.indiegogo.com/projects/php-inspections-ea-extended-a-code-analyzer-security#/">crowdfunding campaign</a>.

<a href="https://www.yourkit.com"><img src="https://www.yourkit.com/images/yklogo.png" alt="YourKit" height="20"></a> YourKit supports us with their full-featured [Java Profiler](https://www.yourkit.com/java/profiler/).

<a href="https://jetbrains.com"><img src="https://resources.jetbrains.com/assets/media/open-graph/jetbrains_250x250.png" alt="JetBrains" height="20"></a>JetBrains supports us with their awesome IDEs.

Project activity and various stats: https://www.openhub.net/p/phpinspectionsea
