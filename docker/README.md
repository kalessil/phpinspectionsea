Docker container for Php Inspections (EA Extended)
---

This is a docker container setup for executing Php Inspections via a headless PhpStorm instance.

Usage guide:
- build once with your own PhpStorm license key
- run for each project / project directory you want to inspect

Build:
- copy your ~/.PhpStorm-*/config/phpstorm.key into this directory
- execute "docker build -t phpinspections ."

Note: when getting "No valid license found" error message, re-apply base64-encoded license format and try again.

Run:
- execute

      docker run --rm -v /path/to/your/projectdir:/var/ci/project phpinspections

- The result ist stored in /var/ci/phpinspectionresult.xml, so you should e.g. configure gitlab to store this artifact
- if however, you want the image to publish the result to your project dir, you could execute

      docker run --rm -v /path/to/your/projectdir:/var/ci/project -e INSPECTIONRESULTFILE=/var/ci/project/result.xml phpinspections

ENV Variables:
- INSPECTIONRESULTFILE - path to the result XML that will be generated (checkstyle XML format)

  default: /var/ci/phpinspections_cs.xml

- INSPECTIONCONFIGFILE - path to the PHPStorm inspection config XML that should be used

  default: /var/ci/project/.idea/inspectionProfiles/Project_Default.xml 

- INSPECTIONCONFIG - optionally pass an inspection config XML as a string

Project Settings:

The following files can pushed into your repository in order to align the headless behaviour with regular one.

- `.idea/inspectionProfiles/Project_Default.xml`: inspections setting customization (disables, settings and etc.)
- `.idea/php.xml`: PHP language level (IDE takes 5.6 when the file is not specified)