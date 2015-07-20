<?php

    class exportableAsString
    {
        public function __toString()
        {
            return '';
        }
    }

    $arr = array();
    echo $arr['0']; //ok
    echo $arr[0];   //ok
    echo $arr[new exportableAsString()]; // needs to be fixed, this is legal

    echo $arr[array()];      //reported
    echo $arr{'0'};          //reported
    echo $arr{0};            //reported

    $obj = new exportableAsString();
    echo $obj[0];            // reported
    echo $obj{0};            // reported

