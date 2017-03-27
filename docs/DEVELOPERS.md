### Pre-requisites

- Java 8 **SDK** SE
- Intllij Idea Untimate (for running Unit Tests)
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

- In IDE: File -> Project Structure
  - Project Settings -> Project
    ![Project Settings](images/project-settings.png)
  - Project Settings -> Modules
    ![Module Settings](images/module-settings.png)
    ![Module Dependencies](images/module-settings-deps.png)
  - Project Settings -> libraries (Screenshot; Idea lib, PS jar, PS PHP/CSS plugin folders)
    ![Project libs](images/libraries.png)
  - Platform Settings -> SDKs
    ![Platform SDKs](images/sdks.png)

## Configuring and running test

In IDE: Run -> Edit Configurations; add a new configuration for JUnit called "Tests" (as on screenshot):

![Tests run configuration](images/test-run-configuration.png)