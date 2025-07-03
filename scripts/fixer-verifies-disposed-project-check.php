<?php

    $basePath    = realpath(__DIR__ . '/..');
    $sourcesPath = $basePath . '/src/main/java';

    $missingChecks = [];
    /** @var \SplFileInfo $file */
    foreach (new \RecursiveIteratorIterator(new \RecursiveDirectoryIterator($sourcesPath)) as $file) {
        if ($file->getExtension() === 'java') {
            $content      = file_get_contents($file->getPathname());
            $fixersCount  = 0;

            $lastPosition   = 0;
            $searchFragment = 'public void applyFix';
            while (($lastPosition = strpos($content, $searchFragment, $lastPosition)) !== false) {
                ++$fixersCount;
                $lastPosition += strlen($searchFragment);
            }

            if ($fixersCount > 0) {
                $checksCount    = 0;

                $lastPosition   = 0;
                $searchFragment = 'project.isDisposed()';
                while (($lastPosition = strpos($content, $searchFragment, $lastPosition)) !== false) {
                    ++$checksCount;
                    $lastPosition += strlen($searchFragment);
                }

                if ($checksCount < $fixersCount) {
                    $missingChecks []= $file->getFilename();
                }
            }
        }
    }

    if ($missingChecks !== []) {
        echo 'Some fixers are not checking disposal state: ' . PHP_EOL . implode(',' . PHP_EOL, $missingChecks) . PHP_EOL;
        exit(1);
    }

    echo 'Fixers verify project disposal state: [OK]', PHP_EOL;