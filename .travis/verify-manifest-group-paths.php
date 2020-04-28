<?php

    $basePath = __DIR__ . '/..';
    $manifest = simplexml_load_string(file_get_contents($basePath . '/src/main/resources/META-INF/plugin.xml'));
    if (false === $manifest) {
        throw new \RuntimeException('Failed to load manifest');
    }

    $violations = [];
    foreach ($manifest->xpath('//localInspection') as $inspectionDefinition) {
        $attributes = $inspectionDefinition->attributes();
        $groupPath = trim($attributes->groupPath);
        if ($groupPath !== 'PHP,Php Inspections (EA Ultimate)') {
            $violations []= trim($attributes->shortName);
        }
    }

    if ($violations !== []) {
        echo 'Following inspections have outdated groupPath definition: ' . PHP_EOL . implode(',' . PHP_EOL, $violations) . PHP_EOL;
        exit(1);
    }
