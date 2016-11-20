<?php

class OpensslRsaPaddingOracle
{
    public function curlSllSpoofingCase1($ch)
    {
        $options = [
            <error descr="CURLOPT_SSL_VERIFYHOST should be 2">CURLOPT_SSL_VERIFYHOST => false</error>,
            <error descr="CURLOPT_SSL_VERIFYHOST should be 2">CURLOPT_SSL_VERIFYHOST => 0</error>,
            <error descr="CURLOPT_SSL_VERIFYHOST should be 2">CURLOPT_SSL_VERIFYHOST => null</error>,
            <error descr="CURLOPT_SSL_VERIFYHOST should be 2">CURLOPT_SSL_VERIFYHOST => true</error>,
            <error descr="CURLOPT_SSL_VERIFYHOST should be 2">CURLOPT_SSL_VERIFYHOST => 1</error>,
            CURLOPT_SSL_VERIFYHOST => 2,
            CURLOPT_SSL_VERIFYHOST => '2'
        ];

        <error descr="CURLOPT_SSL_VERIFYHOST should be 2">curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, false)</error>;
        <error descr="CURLOPT_SSL_VERIFYHOST should be 2">curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 0)</error>;
        <error descr="CURLOPT_SSL_VERIFYHOST should be 2">curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, null)</error>;
        <error descr="CURLOPT_SSL_VERIFYHOST should be 2">curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, true)</error>;
        <error descr="CURLOPT_SSL_VERIFYHOST should be 2">curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 1)</error>;
        curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 2);
        curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, '2');

        $options = [
            <error descr="CURLOPT_SSL_VERIFYPEER should be 1">CURLOPT_SSL_VERIFYPEER => 0</error>,
            <error descr="CURLOPT_SSL_VERIFYPEER should be 1">CURLOPT_SSL_VERIFYPEER => false</error>,
            <error descr="CURLOPT_SSL_VERIFYPEER should be 1">CURLOPT_SSL_VERIFYPEER => null</error>,
            CURLOPT_SSL_VERIFYPEER => true,
            CURLOPT_SSL_VERIFYPEER => 1,
            CURLOPT_SSL_VERIFYPEER => '1'
        ];

        <error descr="CURLOPT_SSL_VERIFYPEER should be 1">curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false)</error>;
        <error descr="CURLOPT_SSL_VERIFYPEER should be 1">curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, 0)</error>;
        <error descr="CURLOPT_SSL_VERIFYPEER should be 1">curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, null)</error>;
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, true);
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, 1);
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, '1');
    }

}