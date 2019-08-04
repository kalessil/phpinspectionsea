<?php

    $basePath = __DIR__ . '/..';

    $manifest = simplexml_load_string(file_get_contents($basePath . '/src/main/resources/META-INF/plugin.xml'));
    $rules    = file($basePath . '/RULES.md');
    if (false === $manifest || false === $rules) {
        throw new \RuntimeException('Failed to load resources');
    }

    $outdatedImplementations = [];

    /* collect manifest rules */
    $definedRules     = array();
    $descriptionFiles = array();
    foreach ($manifest->xpath('//localInspection') as $inspectionDefinition) {
        $attributes                       = $inspectionDefinition->attributes();
        $valueObject                      = new \stdClass();
        $valueObject->shortName           = trim($attributes->shortName);
        $valueObject->displayName         = trim($attributes->displayName);
        $valueObject->implementationClass = trim($attributes->implementationClass);

        $implementation = $basePath . '/src/main/java/' . str_replace('.', '/', $valueObject->implementationClass) . '.java';
        $content        = file_get_contents($implementation);
        if (false === $content) {
            throw new \RuntimeException('Failed to load file: ' . $implementation);
        }

        $updatedContent = preg_replace(
            "/public String getShortName\s*\([^}]+}/im",
            sprintf("public String getShortName() {\n        return \"%s\";\n    }", $valueObject->shortName),
            $content
        );
        $updatedContent = preg_replace(
            "/public String getDisplayName\s*\([^}]+}/im",
            sprintf("public String getDisplayName() {\n        return \"%s\";\n    }", $valueObject->displayName),
            $updatedContent
        );
        if ($content !== $updatedContent) {
            $outdatedImplementations []= $valueObject->implementationClass;
            file_put_contents($implementation, $updatedContent);
        }
    }

    if ($outdatedImplementations !== []) {
        echo 'Following inspections has outdated short/display name: ' . PHP_EOL . implode(',' . PHP_EOL, $outdatedImplementations) . PHP_EOL;
        exit(1);
    }