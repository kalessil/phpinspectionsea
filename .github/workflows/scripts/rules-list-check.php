<?php

    $basePath = __DIR__ . '/..';

    $manifest = simplexml_load_string(file_get_contents($basePath . '/src/main/resources/META-INF/plugin.xml'));
    $rules    = file($basePath . '/RULES.md');
    if (false === $manifest || false === $rules) {
        throw new \RuntimeException('Failed to load resources');
    }

    /* collect documented rules */
    $documentedRules = array();
    foreach ($rules as $lineNumber => $lineContent) {
        $lineContent = trim($lineContent);
        if (false !== stripos($lineContent, 'inspect') && false !== strpos($lineContent, '|')) {
            $fragments = explode('|', $lineContent);
            if (count($fragments) >= 8) {
                $groupName        = trim($fragments[1]);
                $shortName        = trim($fragments[2]);
                $displayName      = trim($fragments[3]);
                $hasTests         = 'yes' === trim($fragments[5]);
                $hasDocumentation = 'yes' === trim($fragments[7]);

                $documentedRules[$shortName]   = $valueObject = new \stdClass();
                $valueObject->groupName        = $groupName;
                $valueObject->shortName        = $shortName;
                $valueObject->displayName      = $displayName;
                $valueObject->hasTests         = $hasTests;
                $valueObject->hasDocumentation = $hasDocumentation;
            }
        }
    }

    /* collect manifest rules */
    $definedRules     = array();
    $descriptionFiles = array();
    foreach ($manifest->xpath('//localInspection') as $inspectionDefinition) {
        $attributes  = $inspectionDefinition->attributes();

        $groupName   = trim($attributes->groupName);
        $shortName   = trim($attributes->shortName);
        $displayName = trim($attributes->displayName);

        $definedRules[$shortName] = $valueObject = new \stdClass();
        $valueObject->groupName   = $groupName;
        $valueObject->shortName   = $shortName;
        $valueObject->displayName = $displayName;

        $implementationClass   = substr($attributes->implementationClass, strrpos($attributes->implementationClass, '.') + 1);
        $targetTestFile        = sprintf('%sTest.java', $implementationClass);
        $valueObject->hasTests = false;
        foreach (new \RecursiveIteratorIterator(new \RecursiveDirectoryIterator($basePath . '/src/test/java/')) as $file) {
            if ($targetTestFile === $file->getFilename()) {
                $valueObject->hasTests = true;
                break;
            }
        }

        $descriptionFile = sprintf('%s/src/main/resources/inspectionDescriptions/%s.html', $basePath, $shortName);
        if (false === $description = file_get_contents($descriptionFile)) {
            throw new \RuntimeException('Could not read description file: ' . $descriptionFile);
        }
        $descriptionFiles []= $descriptionFile;
        $valueObject->hasDocumentation = false !== strpos($description, '/kalessil/phpinspectionsea/blob/master/docs/');
    }

    /* step 1: report un-documented inspections */
    $undocumented = array_diff(array_keys($definedRules), array_keys($documentedRules));
    if ($undocumented !== []) {
        echo 'Following inspections are not documented: ' . PHP_EOL . implode(',' . PHP_EOL, $undocumented) . PHP_EOL;
        exit(1);
    }
    /* step 2: report non-existing inspections */
    $unknown = array_diff(array_keys($documentedRules), array_keys($definedRules));
    if ($unknown !== []) {
        echo 'Following inspections are not registered: ' . PHP_EOL . implode(',' . PHP_EOL, $unknown) . PHP_EOL;
        exit(1);
    }
    /* step 3: report outdated documentation */
    $outdated = array();
    foreach ($definedRules as $shortName => $valueObject) {
        /** @noinspection TypeUnsafeComparisonInspection ; hacky object content comparison */
        if ((array)$valueObject != (array)$documentedRules[$shortName]) {
            $outdated []= $shortName;
        }
    }
    if ($outdated !== []) {
        echo 'Following inspections info is outdated: ' . PHP_EOL . implode(',' . PHP_EOL, $outdated) . PHP_EOL;
        exit(1);
    }

    /* step 4: report un-tested inspections */
    $untested = array();
    foreach ($definedRules as $shortName => $valueObject) {
        if (false === $valueObject->hasTests) {
            $untested []= $shortName;
        }
    }
    if ($untested !== []) {
        echo 'Following inspections are not tested: ' . PHP_EOL . implode(',' . PHP_EOL, $untested) . PHP_EOL;
        exit(1);
    }

    /* step 5: report orphaned description files */
    $orphanedDescriptions = [];
    $descriptionsBase     = sprintf('%s/src/main/resources/inspectionDescriptions', $basePath);
    foreach (glob(sprintf('%s/*.html', $descriptionsBase)) as $file) {
        if (!in_array($file, $descriptionFiles, true)) {
            $orphanedDescriptions []= basename($file);
        }
    }
    if ($orphanedDescriptions !== []) {
        echo 'Following descriptions are orphaned: ' . PHP_EOL . implode(',' . PHP_EOL, $orphanedDescriptions) . PHP_EOL;
        exit(1);
    }

    echo 'Rules list: [OK]', PHP_EOL;