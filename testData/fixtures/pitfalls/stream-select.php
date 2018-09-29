<?php

class CasesHolder {
    const THRESHOLD_CORRECT = 200000;
    const THRESHOLD_LOW     = 100000;

    public function cases_hlder() {
        return [
            stream_select ([], [], [], 0, <warning descr="Might cause high CPU usage connectivity issues (documentation advices using 200000 here, 200 ms).">self::THRESHOLD_LOW</warning>),
            stream_select ([], [], [], 0, <warning descr="Might cause high CPU usage connectivity issues (documentation advices using 200000 here, 200 ms).">100000</warning>),

            stream_select ([], [], [], 0),
            stream_select ([], [], [], 0, 200000),
            stream_select ([], [], [], 0, self::THRESHOLD_CORRECT),
        ];
    }
}