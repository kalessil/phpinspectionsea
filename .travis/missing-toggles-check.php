<?php

    $basePath    = __DIR__ . '/..';
    $sourcesPath = $basePath . '/src/main/java/com/kalessil/phpStorm/phpInspectionsEA/inspectors';

    $missingDistractionTogglesFiles = [];
    $partialDistractionTogglesFiles = [];
    $missingUltimateTogglesFiles    = [];

    /* @var \SplFileInfo $file */
    foreach (new \RecursiveIteratorIterator(new \RecursiveDirectoryIterator($sourcesPath)) as $file) {
        if ($file->getExtension() === 'java') {
            $content = file_get_contents($file->getPathname());

            $visitors       = 0;
            $lastPosition   = 0;
            $searchFragment = 'public void visit';
            while (($lastPosition = strpos($content, $searchFragment, $lastPosition)) !== false) {
                ++$visitors;
                $lastPosition += strlen($searchFragment);
            }

            /* distraction toggles */
            $distractionToggles = 0;
            $lastPosition       = 0;
            $searchFragment     = '.isContainingFileSkipped';
            while (($lastPosition = strpos($content, $searchFragment, $lastPosition)) !== false) {
                ++$distractionToggles;
                $lastPosition += strlen($searchFragment);
            }

            if ($visitors > 0 && $visitors != $distractionToggles) {
                if ($distractionToggles > 0) {
                    $partialDistractionTogglesFiles[] = $file->getFilename();
                } else {
                    $missingDistractionTogglesFiles[] = $file->getFilename();
                }
            }

            /* ultimate toggles */
            $ultimateToggles = 0;
            $lastPosition    = 0;
            $searchFragment  = '.areFeaturesEnabled';
            while (($lastPosition = strpos($content, $searchFragment, $lastPosition)) !== false) {
                ++$ultimateToggles;
                $lastPosition += strlen($searchFragment);
            }

            if ($visitors > 0 && $visitors != $ultimateToggles) {
                $missingUltimateTogglesFiles[] = $file->getFilename();
            }
        }
    }

    if (count($missingUltimateTogglesFiles) > 0) {
        echo 'Following files has inconsistent ultimate toggles: ' . PHP_EOL . implode(PHP_EOL, $missingUltimateTogglesFiles) . PHP_EOL;
        exit(-1);
    }
    if (count($partialDistractionTogglesFiles) > 0) {
        echo 'Following files has inconsistent distraction toggles: ' . PHP_EOL . implode(PHP_EOL, $partialDistractionTogglesFiles) . PHP_EOL;
        exit(-1);
    }
    if (count($missingDistractionTogglesFiles) > 0) {
        echo 'Following files are missing distraction toggles: ' . PHP_EOL . implode(PHP_EOL, $missingDistractionTogglesFiles) . PHP_EOL;
        exit(-1);
    }
