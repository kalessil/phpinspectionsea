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
        $container->toggle = (strpos($inspectionContent, 'return new FeaturedPhpElementVisitor()') !== false);

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

    /* gather statistics */
    $statistics = [];
    foreach ($ultimateDefinitions as $className => $definition) {
        $extendedDefinition = isset($extendedDefinitions[$className]) ? $extendedDefinitions[$className] : [];
        if ((array)$definition != (array)$extendedDefinition) {
            $status = ($extendedDefinition === []) ? 'new' : ($definition->toggle ? 'enhanced' : 'relocated');
            if (!isset($statistics[$definition->groupName])) {
                $statistics[$definition->groupName] = ['new' => 0, 'enhanced' => 0, 'relocated' => 0];
            }
            if ($status === 'new' && !$definition->toggle) {
                echo sprintf('%s misses ultimate toggles: ', $className), PHP_EOL;
                exit(1);
            }

            ++$statistics[$definition->groupName][$status];
            echo sprintf('%s: %s (%s)', $definition->groupName, $className, $status), PHP_EOL;
        }
    }
    ksort($statistics);

    /* do summary reporting and ensure plugin information is up to date */
    echo PHP_EOL, PHP_EOL;
    $totalNew = $totalEnhanced = 0;
    foreach ($statistics as $group => $statistic) {
        $totalNew      += $statistic['new'];
        $totalEnhanced += $statistic['enhanced'];
        echo sprintf('%s: %s new and %s enhanced inspections', $group, $statistic['new'], $statistic['enhanced']), PHP_EOL;
    }
    $statsInDescription = sprintf('%s new and %s enhanced', $totalNew, $totalEnhanced);
    echo PHP_EOL, sprintf('Total: %s', $statsInDescription), PHP_EOL, PHP_EOL;

    $descriptionFile = $basePath . '/src/main/resources/META-INF/description.html';
    if (($descriptionContent = file_get_contents($descriptionFile)) === false) {
        throw new \RuntimeException('Could not load plugin description');
    }
    if (substr_count($descriptionContent, $statsInDescription) !== 2) {
        echo sprintf('description.html is outdated ("%s" is missing)', $statsInDescription), PHP_EOL;
        exit(1);
    }

    /* ensure sidebar component provides correct numbers */
    $totalInspectionsCount = count($ultimateDefinitions);
    $basicInspectionsCount = $totalInspectionsCount - ($totalNew + $totalEnhanced);
    $sidebarComponentFile  = $basePath . '/src/main/java/com/kalessil/phpStorm/phpInspectionsEA/EAUltimateSidebarComponent.java';
    if (($sidebarComponentContent = file_get_contents($sidebarComponentFile)) === false) {
        throw new \RuntimeException('Could not load sidebar component content');
    }
    $sidebarComponentOutdated = strpos($sidebarComponentContent, sprintf('final int total = %s', $totalInspectionsCount)) === false ||
                                strpos($sidebarComponentContent, sprintf('final int basic = %s', $basicInspectionsCount)) === false;
    if ($sidebarComponentOutdated) {
        echo sprintf('EAUltimateSidebarComponent is outdated (total should be %s, basic should be %s)', $totalInspectionsCount, $basicInspectionsCount), PHP_EOL;
        exit(1);
    }
