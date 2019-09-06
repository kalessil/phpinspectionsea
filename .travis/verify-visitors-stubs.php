<?php

    $basePath    = realpath(__DIR__ . '/..');
    $sourcesPath = $basePath . '/src/main/java/com/kalessil/phpStorm/phpInspectionsEA';

    preg_match_all('/public void (visit\w+)\(/i', file_get_contents($sourcesPath . '/openApi/GenericPhpElementVisitor.java'), $visitors);
    $stubbedVisitors = $visitors[1];
    $stubbedVisitors [] = 'visitPhpStatement'; /* stubbing breaks DeclareDirectiveCorrectnessInspector */

    $needsStubbing = [];
    /* @var \SplFileInfo $file */
    foreach (new \RecursiveIteratorIterator(new \RecursiveDirectoryIterator($sourcesPath . '/inspectors/')) as $file) {
        if ($file->getExtension() === 'java') {
            preg_match_all('/public void (visit\w+)\(/i', file_get_contents($file->getPathname()), $visitors);
            $missingStubs = array_diff($visitors[1], $stubbedVisitors);
            if ($missingStubs !== []) {
                $needsStubbing []= sprintf("%s: %s", $file->getFilename(), implode(', ', $missingStubs));
            }
        }
    }

    if ($needsStubbing !== []) {
        echo 'Following files has un-stubbed visitors: ' . PHP_EOL . implode(PHP_EOL, $needsStubbing) . PHP_EOL;
        exit(1);
    }
