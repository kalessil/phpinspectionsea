<?php

    $fixturesPath = __DIR__ . '/../testData/fixtures';
    $testsPath    = __DIR__ . '/../src/test/java';

    $referencedFiles = [[]];
    foreach (new \RecursiveIteratorIterator(new \RecursiveDirectoryIterator($testsPath)) as $file) {
        /** @var \SplFileInfo $file */
        if ($file->getExtension() === 'java') {
            $content = file_get_contents($file->getFilename());
            if (preg_match_all('/"([^"]+\.php)"/', $content, $matches)) {
                $referencedFiles []= $matches[1];
            }
        }
    }
    $referencedFiles = array_unique(array_merge(...$referencedFiles));
    var_export($referencedFiles);

//    /** @var \SplFileInfo $file */
//    foreach (new \RecursiveIteratorIterator(new \RecursiveDirectoryIterator($fixturesPath)) as $file) {
//        if ($file->getExtension() === 'php') {
//            $valueObject->hasTests = true;
//            break;
//        }
//    }