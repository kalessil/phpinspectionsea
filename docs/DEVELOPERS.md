### Pre-requisites

- Java 8 **SDK** SE
- Intellij Idea Ultimate (for running Unit Tests)
- PhpStorm 2016.2.1 (for PHP-related SDK)

> Note: alternatively you can add PHP plugin to IDEA and use it instead. Some test might fail in this case.

### Checking out project

- Fork https://github.com/kalessil/phpinspectionsea on Github
- In IDE: VCS -> Checkout from Version Control -> Git
  - Provide url, e.g. https://github.com/<your_github_account>/phpinspectionsea.git
  - IDE will suggest to open a folder after checking source code out

### Known issues

Classes responsibility are not separated: inspections are also generating replacements and containing inner QF classes.

### Configuring project

- Rename file: `PhpInspectionsEA.iml.dist` to `PhpInspectionsEA.iml`

- In IDE: File -> Project Structure
  - Project Settings -> Project
    ![Project Settings](images/project-settings.png)

  - Project Settings -> libraries (Screenshot; Idea lib, PS jar, PS PHP/CSS plugin folders)
    ![Project libs](images/libraries.png)

    If you use JetBrains toolbox to install IDEs, be sure to use directories where the IDE installation is. Don't use
    the suggested directory containing `app` path since it does not contain all needed dependencies.
  - Project Settings -> Modules

    ![Module Settings](images/module-settings.png)

    ![Module Dependencies](images/module-settings-deps.png)

  - Platform Settings -> SDKs
    ![Platform SDKs](images/sdks.png)

## Configuring and running test

In IDE: Run -> Edit Configurations; add a new configuration for JUnit called "Tests" (as on screenshot):

![Tests run configuration](images/test-run-configuration.png)

You can use the provided `log4j.properties` in project root as default/seed configuration.

## Configuring GitHub Task Integration

You can easily configure IntelliJ to fetch task from GitHub:

1. Settings | Tasks | Servers
2. Add server with relevant data:
    * Username = kalessil
    * API Token = Create one for you using `Create API Token` button.
    * Repository = phpinspectionsea
    * Host = https://github.com

## Configuring spell checking dictionary

As of [this](https://youtrack.jetbrains.com/issue/IDEA-121886) IntelliJ IDEs does not support very well multiple spelling
dictionaries. If you would like to have such feature, please vote on that issue so they can take care.

For the time being use the provided file in `docs/dictionaries/kalessil.xml` to configure your dictionary located in:
`.idea/dictionaries/{username}.xml`, next time you restart the IDE, the words will be available under:
File | Settings | Spelling | Accepted Words.

Regarding IntelliJ suggestion of installing php plugins to use with the `.php` files, IGNORE it. Most of the files used
in fixture are not valid PHP files and though IDE inspections will complain.
