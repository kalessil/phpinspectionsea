<?php

    $basePath    = realpath(__DIR__ . '/..');
    $sourcesPath = $basePath . '/src/main/java/com/kalessil/phpStorm/phpInspectionsEA';

    $incompleteDefinitions = [];
    /* @var \SplFileInfo $file */
    foreach (new \RecursiveIteratorIterator(new \RecursiveDirectoryIterator($sourcesPath . '/inspectors/')) as $file) {
        if ($file->getExtension() === 'java') {
            preg_match_all('/public void (visit\w+)\(/i', $inspectionContent = file_get_contents($file->getPathname()), $visitors);

            $visitorsWithMissingAnnotations = [];
            foreach ($visitors[1] as $visitor) {
                if (preg_match(sprintf('/@Override\s+public void %s\(@NotNull/is', $visitor), $inspectionContent) === 0) {
                    $visitorsWithMissingAnnotations []= $visitor;
                }
            }
            if ($visitorsWithMissingAnnotations !== []) {
                $incompleteDefinitions []= sprintf("%s: %s", $file->getFilename(), implode(', ', $visitorsWithMissingAnnotations));
            }
        }
    }

    if ($incompleteDefinitions !== []) {
        echo 'Following visitors declaration is incomplete (override, not-null annotations): ' . PHP_EOL . implode(PHP_EOL, $incompleteDefinitions) . PHP_EOL;
        exit(1);
    }

    echo 'Verify visitors definitions: [OK]', PHP_EOL;