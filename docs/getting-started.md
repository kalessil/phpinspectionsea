# Getting started with Php Inspections (EA Extended)

## Prerequisites

To get maximum benefit from using the plugin, we should agree on following:
* organizing code in classes/functions;
* using curly brackets for control statements (if/else, loops and etc.);
* using Composer would be great, as security checks can be applied to third party components;

## Installation

Regular installation process includes 2 steps:
- Navigate to *File -> Settings -> Plugins* and click *Browse Repositories*. New window will popup listing available plugins. 
- Type *Php Inspections (EA Extended)* into the top search field and install the plugin. Click *OK* buttons on both open windows.

After installation IDE will suggest restarting, do so to get plugin loaded.

## Initial configuration

If you just started using the analyzer, it is worth following those steps:
* Right click on root folder, click "Inspect code" or "Analyze"->"Inspect code";
* Exclude all third-party components (e.g. vendor folder);
* Click "Ok" and wait for analysis completion;
* Review reported issues and decide if you want to deactivate some of them;
* It is also worth reviewing entries reported in the "Probable Bugs" group at first place;

You can activate disabled inspections later in IDE settings. Also some inspections have own settings,
and perhaps you would want to review them before deactivating anything.

## Automatic fixes for found issues

Most of the inspections has so called Quick-Fixes fixing the reported issues. They are applicable in 2 ways:
* from inspection results (button with action title)
* from a bulb appearing when you placing the cursor on a reported code fragment (code is grayed out, colored and etc.);
