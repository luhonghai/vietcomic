/*
 * Copyright (c) 2013. CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */
package com.halosolutions.itranslator.constant;

/**
 * Created by longnguyen on 06/19/15.
 *
 */
public class AppInfo {
    /**
     * The login parameters should be specified in the following manner:
     *
     * public static final String SpeechKitServer = "ndev.server.name";
     *
     * public static final int SpeechKitPort = 1000;
     *
     * public static final String SpeechKitAppId = "ExampleSpeechKitSampleID";
     *
     * public static final byte[] SpeechKitApplicationKey =
     * {
     *     (byte)0x38, (byte)0x32, (byte)0x0e, (byte)0x46, (byte)0x4e, (byte)0x46, (byte)0x12, (byte)0x5c, (byte)0x50, (byte)0x1d,
     *     (byte)0x4a, (byte)0x39, (byte)0x4f, (byte)0x12, (byte)0x48, (byte)0x53, (byte)0x3e, (byte)0x5b, (byte)0x31, (byte)0x22,
     *     (byte)0x5d, (byte)0x4b, (byte)0x22, (byte)0x09, (byte)0x13, (byte)0x46, (byte)0x61, (byte)0x19, (byte)0x1f, (byte)0x2d,
     *     (byte)0x13, (byte)0x47, (byte)0x3d, (byte)0x58, (byte)0x30, (byte)0x29, (byte)0x56, (byte)0x04, (byte)0x20, (byte)0x33,
     *     (byte)0x27, (byte)0x0f, (byte)0x57, (byte)0x45, (byte)0x61, (byte)0x5f, (byte)0x25, (byte)0x0d, (byte)0x48, (byte)0x21,
     *     (byte)0x2a, (byte)0x62, (byte)0x46, (byte)0x64, (byte)0x54, (byte)0x4a, (byte)0x10, (byte)0x36, (byte)0x4f, (byte)0x64
     * };
     *
     * Please note that all the specified values are non-functional
     * and are provided solely as an illustrative example.
     *
     */

    /* Please contact Nuance to receive the necessary connection and login parameters */
    public static final String SpeechKitServer = "sandbox.nmdp.nuancemobility.net";

    public static final int SpeechKitPort = 443;

    public static final boolean SpeechKitSsl = false;

    public static final String SpeechKitAppId = "NMDPTRIAL_long_nguyen_c_mg_com20150614050117";

    public static final byte[] SpeechKitApplicationKey = {
            (byte)0x5f, (byte)0x13, (byte)0x0f, (byte)0x59, (byte)0x83, (byte)0xea, (byte)0xcc, (byte)0x3e, (byte)0x82, (byte)0xdc, (byte)0xa8, (byte)0xe4, (byte)0xd6, (byte)0xe1, (byte)0x54, (byte)0x52, (byte)0x94, (byte)0x32, (byte)0x7f, (byte)0x45, (byte)0x67, (byte)0x90, (byte)0xe2, (byte)0x02, (byte)0xd7, (byte)0xca, (byte)0x3b, (byte)0x59, (byte)0x66, (byte)0x06, (byte)0xdf, (byte)0xc5, (byte)0x19, (byte)0xee, (byte)0x1f, (byte)0x9c, (byte)0xd8, (byte)0xeb, (byte)0xdb, (byte)0x5b, (byte)0xc7, (byte)0x83, (byte)0x3f, (byte)0x9e, (byte)0x65, (byte)0x93, (byte)0xf0, (byte)0xf9, (byte)0xc5, (byte)0x70, (byte)0x3e, (byte)0x2c, (byte)0x00, (byte)0x20, (byte)0x2e, (byte)0xd7, (byte)0xea, (byte)0x69, (byte)0x30, (byte)0x51, (byte)0x6f, (byte)0x10, (byte)0x16, (byte)0x88
    };
}
