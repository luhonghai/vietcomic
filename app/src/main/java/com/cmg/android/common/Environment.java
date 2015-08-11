/*
 * Copyright (c) 2013. CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */

package com.cmg.android.common;

import android.content.res.Resources;

import com.cmg.android.plmobile.R;

/**
 * DOCME
 *
 * @author $Author$
 * @version $Revision$
 * @Creator Hai Lu
 * @Last changed: $LastChangedDate$
 */

public class Environment {
    public static final String DEV = "DEV";

    public static final String INT = "INT";

    public static final String PROD = "PROD";

    protected Environment() {

    }

    /**
     * check Environment
     *
     * @param env
     * @param res
     * @return
     */
    public static boolean isEnv(String env, Resources res) {
        return res.getString(R.string.environment).equals(env);
    }

    /**
     * Enable Google Analytics log for INT and PROD
     *
     * @param res
     * @return
     */
    public static boolean isEnableAnalytics(Resources res) {
        // Change to
        return true;
        //return isEnv(PROD, res) || isEnv(INT, res);
    }
}
