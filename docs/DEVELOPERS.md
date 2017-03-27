=== Pre-requisits ===

- Java 8 **SDK** SE
- Intllij Idea Untimate (for running Unit Tests)
- PhpStorm 2016.2.1 (for PHP-related SDK)

=== Checking out project ===

- Fork https://github.com/kalessil/phpinspectionsea on Github
- In IDE: VCS -> Checkout from Version Control -> Git
--> Provide url, e.g. https://github.com/<your_github_account>/phpinspectionsea.git
--> IDE will suggest to open a folder after checking source code out

=== Known issues ===

- Responsibilities are not separated: inspections are also generating replacements and containing inner QF classes.

=== Configuring project ===

- In IDE: File -> Project Structure
-- > Project Settings -> Project (Project SDK: PS + Java 6 -> Screenshot)
![Project SDKs](https://-/images/-.png)
-- > Project Settings -> Modules (Module SDK: Idea -> Screenshot)
![Module SDKs](https://-/images/-.png)
-- > Project Settings -> libraries (Screenshot; Idea lib, PS jar, PS PHP/CSS plugin folders)
![Project libs](https://-/images/-.png)
-- > Platform Settings -> SDKs
![Platform SDKs](https://-/images/-.png)

=== Configuring and running test ===

- In IDE: Run -> Edit Configurations
--> add a new configuration for JUnit called "Tests" (as on screenshot)
![Tests run configuration](https://-/images/-.png)