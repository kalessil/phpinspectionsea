<?php

    $basePath    = realpath(__DIR__ . '/..');
    $sourcesPath = $basePath . '/src/main/java/com/kalessil/phpStorm/phpInspectionsEA';

    $missingPrefixes = [];
    /* @var \SplFileInfo $file */
    foreach (new \RecursiveIteratorIterator(new \RecursiveDirectoryIterator($sourcesPath)) as $file) {
        if ($file->getExtension() === 'java') {
            preg_match_all('/holder\.registerProblem\(([^;]+)\);/im', file_get_contents($file->getPathname()), $messaging);
            foreach ($messaging[1] as $message) {
                if (strpos($message, 'wrapReportedMessage') === false) {
                    $missingPrefixes []= $file->getFilename();
                    break;
                }
            }
        }
    }

    if ($missingPrefixes !== []) {
        echo 'Following files has un-prefixed messaging: ', PHP_EOL, implode(PHP_EOL, $missingPrefixes), PHP_EOL, count($missingPrefixes), ' in total', PHP_EOL;
        exit(1);
    }