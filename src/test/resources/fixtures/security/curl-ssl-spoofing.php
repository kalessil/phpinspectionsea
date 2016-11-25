<?php

class OpensslRsaPaddingOracle
{
    public function curlSllSpoofingCase1($ch)
    {
        $options = [
            <error descr="Exposes a connection to MITM attacks. Use 2 (default) to stay safe.">CURLOPT_SSL_VERIFYHOST => false</error>,
            <error descr="Exposes a connection to MITM attacks. Use 2 (default) to stay safe.">CURLOPT_SSL_VERIFYHOST => 0</error>,
            <error descr="Exposes a connection to MITM attacks. Use 2 (default) to stay safe.">CURLOPT_SSL_VERIFYHOST => '0'</error>,
            <error descr="Exposes a connection to MITM attacks. Use 2 (default) to stay safe.">CURLOPT_SSL_VERIFYHOST => null</error>,
            <error descr="Exposes a connection to MITM attacks. Use 2 (default) to stay safe.">CURLOPT_SSL_VERIFYHOST => true</error>,
            <error descr="Exposes a connection to MITM attacks. Use 2 (default) to stay safe.">CURLOPT_SSL_VERIFYHOST => 1</error>,
            <error descr="Exposes a connection to MITM attacks. Use 2 (default) to stay safe.">CURLOPT_SSL_VERIFYHOST => '1'</error>,
            <error descr="Exposes a connection to MITM attacks. Use 2 (default) to stay safe.">CURLOPT_SSL_VERIFYHOST => $debug ? '0' : '1'</error>,
            CURLOPT_SSL_VERIFYHOST => 2,
            CURLOPT_SSL_VERIFYHOST => '2'
        ];

        <error descr="Exposes a connection to MITM attacks. Use 2 (default) to stay safe.">curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, false)</error>;
        <error descr="Exposes a connection to MITM attacks. Use 2 (default) to stay safe.">curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 0)</error>;
        <error descr="Exposes a connection to MITM attacks. Use 2 (default) to stay safe.">curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, '0')</error>;
        <error descr="Exposes a connection to MITM attacks. Use 2 (default) to stay safe.">curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, null)</error>;
        <error descr="Exposes a connection to MITM attacks. Use 2 (default) to stay safe.">curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, true)</error>;
        <error descr="Exposes a connection to MITM attacks. Use 2 (default) to stay safe.">curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 1)</error>;
        <error descr="Exposes a connection to MITM attacks. Use 2 (default) to stay safe.">curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, '1')</error>;
        <error descr="Exposes a connection to MITM attacks. Use 2 (default) to stay safe.">curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, $debug ? '0' : '1')</error>;
        curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 2);
        curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, $debug ? 0 : 2);
        curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, '2');

        $options = [
            <error descr="Exposes a connection to MITM attacks. Use true (default) to stay safe.">CURLOPT_SSL_VERIFYPEER => 0</error>,
            <error descr="Exposes a connection to MITM attacks. Use true (default) to stay safe.">CURLOPT_SSL_VERIFYPEER => '0'</error>,
            <error descr="Exposes a connection to MITM attacks. Use true (default) to stay safe.">CURLOPT_SSL_VERIFYPEER => false</error>,
            <error descr="Exposes a connection to MITM attacks. Use true (default) to stay safe.">CURLOPT_SSL_VERIFYPEER => null</error>,
            CURLOPT_SSL_VERIFYPEER => true,
            CURLOPT_SSL_VERIFYPEER => 1,
            CURLOPT_SSL_VERIFYPEER => '1'
        ];

        <error descr="Exposes a connection to MITM attacks. Use true (default) to stay safe.">curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false)</error>;
        <error descr="Exposes a connection to MITM attacks. Use true (default) to stay safe.">curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, 0)</error>;
        <error descr="Exposes a connection to MITM attacks. Use true (default) to stay safe.">curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, '0')</error>;
        <error descr="Exposes a connection to MITM attacks. Use true (default) to stay safe.">curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, null)</error>;
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, true);
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, 1);
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, $debug ? 0 : 1);
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, '1');
    }

}