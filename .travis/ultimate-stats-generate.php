<?php

    $basePath = __DIR__ . '/..';
    $manifestUltimate = simplexml_load_string(file_get_contents($basePath . '/src/main/resources/META-INF/plugin.xml'));
    $manifestExtended = simplexml_load_string(file_get_contents($basePath . '/src/main/resources/META-INF/plugin.master.xml'));
    if ($manifestExtended === false || $manifestUltimate === false) {
        throw new \RuntimeException('Could not load a manifest');
    }

    /* extract definitions*/
    $ultimateDefinitions = [];
    foreach ($manifestUltimate->xpath('//localInspection') as $definition) {
        $attributes                        = $definition->attributes();
        $container                         = new \stdClass();
        $container->groupName              = trim($attributes->groupName);
        $container->shortName              = trim($attributes->shortName);
        $container->displayName            = trim($attributes->displayName);

        $inspectionPath = sprintf('%s/src/main/java/%s.java', $basePath, str_replace('.', '/', $attributes->implementationClass));
        if (($inspectionContent = file_get_contents($inspectionPath)) === false) {
            throw new \RuntimeException('Could not load inspection code: ' . $attributes->implementationClass);
        }
        $container->toggle = (strpos($inspectionContent, '.areFeaturesEnabled') !== false);

        $className = substr($attributes->implementationClass, strrpos($attributes->implementationClass, '.') + 1);
        $ultimateDefinitions[$className] = $container;
    }

    $extendedDefinitions = [];
    foreach ($manifestExtended->xpath('//localInspection') as $definition) {
        $attributes                        = $definition->attributes();
        $container                         = new \stdClass();
        $container->toggle                 = false;
        $container->groupName              = trim($attributes->groupName);
        $container->shortName              = trim($attributes->shortName);
        $container->displayName            = trim($attributes->displayName);

        $className = substr($attributes->implementationClass, strrpos($attributes->implementationClass, '.') + 1);
        $extendedDefinitions[$className] = $container;
    }

    $statistics = [];
    foreach ($ultimateDefinitions as $className => $definition) {
        $extendedDefinition = isset($extendedDefinitions[$className]) ? $extendedDefinitions[$className] : [];
        if ((array)$definition != (array)$extendedDefinition) {
            $status = ($extendedDefinition === []) ? 'new' : 'enhanced';
            if (!isset($statistics[$definition->groupName])) {
                $statistics[$definition->groupName] = ['new' => 0, 'enhanced' => 0];
            }
            ++$statistics[$definition->groupName][$status];
            echo sprintf('%s: %s (%s)', $definition->groupName, $className, $status), PHP_EOL;
        }
    }
    echo PHP_EOL, PHP_EOL;
    $totalNew = $totalEnhanced = 0;
    foreach ($statistics as $group => $statistic) {
        $totalNew      += $statistic['new'];
        $totalEnhanced += $statistic['enhanced'];
        echo sprintf('%s: %s new, %s enhanced', $group, $statistic['new'], $statistic['enhanced']), PHP_EOL;
    }
    echo PHP_EOL, sprintf('Total: %s new, %s enhanced', $totalNew, $totalEnhanced), PHP_EOL;
