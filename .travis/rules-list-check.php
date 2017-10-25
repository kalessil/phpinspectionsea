<?php

    $basePath = __DIR__ . '/../';
    $manifest = simplexml_load_string(file_get_contents($basePath . 'src/main/resources/META-INF/plugin.xml'));
    $rules    = file($basePath . 'RULES.md');
    if (false === $manifest || false === $rules) {
        throw new \RuntimeException('Failed to load resources');
    }

    /* collect documented rules */
    $documentedRules = array();
    foreach ($rules as $lineNumber => $lineContent) {
        $lineContent = trim($lineContent);
        if (false !== stripos($lineContent, 'inspect') && false !== strpos($lineContent, '|')) {
            $fragments = explode('|', $lineContent);
            if (count($fragments) >= 4) {
                $groupName   = trim($fragments[1]);
                $shortName   = trim($fragments[2]);
                $displayName = trim($fragments[3]);

                $valueObject              = new \stdClass();
                $valueObject->groupName   = $groupName;
                $valueObject->shortName   = $shortName;
                $valueObject->displayName = $displayName;

                $documentedRules[$shortName] = $valueObject;
            }
        }
    }

    /* collect manifest rules */
    $definedRules = array();
    foreach ($manifest->xpath('//localInspection') as $inspectionDefinition) {
        $attributes  = $inspectionDefinition->attributes();

        $groupName   = trim($attributes->groupName);
        $shortName   = trim($attributes->shortName);
        $displayName = trim($attributes->displayName);

        $valueObject              = new \stdClass();
        $valueObject->groupName   = $groupName;
        $valueObject->shortName   = $shortName;
        $valueObject->displayName = $displayName;

        $definedRules[$shortName] = $valueObject;
    }

    /* step 1: report un-documented inspections */
    $undocumented = array_diff(array_keys($definedRules), array_keys($documentedRules));
    if (count($undocumented) > 0) {
        echo 'Following inspections are not documented: ' . PHP_EOL . implode(',' . PHP_EOL, $undocumented) . PHP_EOL;
        exit(-1);
    }
    /* step 2: report non-existing inspections */
    $unknown = array_diff(array_keys($documentedRules), array_keys($definedRules));
    if (count($unknown) > 0) {
        echo 'Following inspections are not registered: ' . PHP_EOL . implode(',' . PHP_EOL, $unknown) . PHP_EOL;
        exit(-1);
    }
    /* step 3: report outdated documentation */
    $outdated = array();
    foreach ($definedRules as $shortName => $valueObject) {
        /** @noinspection TypeUnsafeComparisonInspection ; hacky object content comparison */
        if ((array)$valueObject != (array)$documentedRules[$shortName]) {
            $outdated []= $shortName;
        }
    }
    if (count($outdated) > 0) {
        echo 'Following inspections info is outdated: ' . PHP_EOL . implode(',' . PHP_EOL, $outdated) . PHP_EOL;
        exit(-1);
    }
