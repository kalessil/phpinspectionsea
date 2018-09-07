<?php

    $basePath     = realpath(__DIR__ . '/..');
    $fixturesPath = $basePath . '/testData/fixtures';
    $testsPath    = $basePath . '/src/test/java';

    $referencedFiles = [[]];
    foreach (new \RecursiveIteratorIterator(new \RecursiveDirectoryIterator($testsPath)) as $file) {
        if ($file->getExtension() === 'java') {
            $content = file_get_contents($file->getPathname());
            if (preg_match_all('/"([^"]+\.php)"/', $content, $matches)) {
                $referencedFiles []= $matches[1];
            }
        }
    }
    $referencedFiles = array_unique(array_merge(...$referencedFiles));

    $basePath     .= '/';
    $orphanedFiles = [];
    foreach (new \RecursiveIteratorIterator(new \RecursiveDirectoryIterator($fixturesPath)) as $file) {
        if ($file->getExtension() === 'php') {
            $normalizedPath = str_replace($basePath, '', $file->getPathname());
            if (!in_array($normalizedPath, $referencedFiles, true)) {
                $orphanedFiles []= $normalizedPath;
            }
        }
    }

    if (count($orphanedFiles) > 0) {
        echo 'Following fixtures are not used: ' . PHP_EOL . implode(',' . PHP_EOL, $orphanedFiles) . PHP_EOL;
        exit(-1);
    }