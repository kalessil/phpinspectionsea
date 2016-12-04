# Getting started with Php Inspections (EA Extended)

## Prerequisites

To get maximum benefit from using the plugin, we should agree on following:
* organizing code in classes/functions;
* using curly brackets for control statements (if/else, loops and etc.);
* using Composer would be great, as security checks can be applied to 3rd parties; 

## Initial configuration

If you just started using the analyzer, it worth following those steps:
* Right click on root folder, click "Inspect code" or "Analyze"->"Inspect code";
* Exclude all third-party components (e.g. vendor folder);
* Click "Ok" and wait for analysis completion;
* Review with your team reported issues and decide if you you want to deactivate some of them;

You can activate disabled inspections later in IDE settings. Also some inspections have own settings 
and perhaps you would want to review them before deactivating anything.

## Fixing issues

Most of inspections has so called Quick-Fixes fixing the reported issues. They are applicable in 2 ways:
* from inspection results (button with action title)
* from a bulb appearing when you placing the cursor on a reported code fragment (code is grayed out, colored and etc.);
