<?php

class OpensslRsaPaddingOracle
{
    public function curlSllSpoofingCase1($ch)
    {
        $options = [
            CURLOPT_SSL_VERIFYHOST => false, // <-reported
            CURLOPT_SSL_VERIFYHOST => 0,     // <-reported
            CURLOPT_SSL_VERIFYHOST => null,  // <-reported
            CURLOPT_SSL_VERIFYHOST => true,  // <-reported
            CURLOPT_SSL_VERIFYHOST => 1,     // <-reported
            CURLOPT_SSL_VERIFYHOST => 2
        ];

        curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, false); // <-reported
        curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 0);     // <-reported
        curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, null);  // <-reported
        curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, true);  // <-reported
        curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 1);     // <-reported
        curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 2);

        $options = [
            CURLOPT_SSL_VERIFYPEER => 0,     // <-reported
            CURLOPT_SSL_VERIFYPEER => false, // <-reported
            CURLOPT_SSL_VERIFYPEER => null,  // <-reported
            CURLOPT_SSL_VERIFYPEER => true,
            CURLOPT_SSL_VERIFYPEER => 1
        ];

        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false); // <-reported
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, 0);     // <-reported
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, null);  // <-reported
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, true);
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, 1);

        $options = [
            CURLOPT_SSL_VERIFYPEER => false,
            CURLOPT_CAINFO         => ''
        ];
        $options = [
            CURLOPT_SSL_VERIFYPEER => false,
            CURLOPT_CAPATH         => ''
        ];
    }

    public function curlSllSpoofingCase2($ch)
    {
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
        curl_setopt($ch, CURLOPT_CAINFO, '');
    }

    public function curlSllSpoofingCase3($ch)
    {
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
        curl_setopt($ch, CURLOPT_CAPATH, '');
    }
}