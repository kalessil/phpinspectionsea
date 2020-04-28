<?php

    $basePath    = realpath(__DIR__ . '/..');
    $sourcesPath = $basePath . '/src/main/java/com/kalessil/phpStorm/phpInspectionsEA';

    $missingPrefixes = [];
    /* @var \SplFileInfo $file */
    foreach (new \RecursiveIteratorIterator(new \RecursiveDirectoryIterator($sourcesPath)) as $file) {
        if ($file->getExtension() === 'java') {
            $matches = preg_match('/public String getName\(\)\s*{\s*return\s+title;\s*}/ims', file_get_contents($file->getPathname()));
            if ($matches) {
                $missingPrefixes []= $file->getFilename();
            }
        }
    }

    if ($missingPrefixes !== []) {
        echo 'Following files has un-prefixed QF titles: ', PHP_EOL, implode(PHP_EOL, $missingPrefixes), PHP_EOL, count($missingPrefixes), ' in total', PHP_EOL;
        exit(1);
    }