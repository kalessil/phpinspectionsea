<?php

    $basePath         = __DIR__ . '/..';
    $sourcesPath      = $basePath . '/src/main/java/com/kalessil/phpStorm/phpInspectionsEA/inspectors';
    $manifestUltimate = simplexml_load_string(file_get_contents($basePath . '/src/main/resources/META-INF/plugin.xml'));
    if ($manifestUltimate === false) {
        throw new \RuntimeException('Could not load the manifest');
    }

    $missingDistractionTogglesFiles = [];
    $partialDistractionTogglesFiles = [];
    $inconsistentStrictnessToggles  = [];

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
            $searchFragment     = '.shouldSkipAnalysis';
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

            if ($visitors > 0) {
                preg_match_all('/StrictnessCategory\.STRICTNESS_CATEGORY_\w+/', $content, $strictnessToggles);
                $strictnessTypes = (array) $strictnessToggles[0];
                if (count(array_unique($strictnessTypes)) !== 1 || count($strictnessTypes) !== $visitors) {
                    $inconsistentStrictnessToggles[] = $file->getFilename();
                } else {
                    $xpath    = sprintf('//localInspection[contains(@implementationClass, "%s")]', str_replace('.java', '', $file->getFilename()));
                    $category = str_replace(' ', '_', strtoupper($manifestUltimate->xpath($xpath)[0]->attributes()->groupName));
                    if ($strictnessTypes[0] !== 'StrictnessCategory.STRICTNESS_CATEGORY_' . $category) {
                        $inconsistentStrictnessToggles[] = $file->getFilename();
                    }
                }
            }
        }
    }

    if (count($inconsistentStrictnessToggles) > 0) {
        echo 'Following files has inconsistent strictness toggles: ' . PHP_EOL . implode(PHP_EOL, $inconsistentStrictnessToggles) . PHP_EOL;
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
