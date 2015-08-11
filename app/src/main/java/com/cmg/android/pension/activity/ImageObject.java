/*
 * Copyright (c) 2013. CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */
package com.cmg.android.pension.activity;

import java.io.Serializable;

/**
 * DOCME
 *
 * @author $Author$
 * @version $Revision$
 * @Creator LongNguyen
 * @Last changed: $LastChangedDate$
 */
@SuppressWarnings("serial")
public class ImageObject implements Serializable {

    private int background;

    private int id;

    /**
     * Constructor
     */
    public ImageObject() {

    }

    /**
     * get background
     *
     * @return
     */
    public int getBackground() {
        return background;
    }

    /**
     * set background
     *
     * @param background
     */
    public void setBackground(int background) {
        this.background = background;
    }

    /**
     * get id
     *
     * @return
     */
    public int getId() {
        return id;
    }

    /**
     * set id
     *
     * @param id
     */
    public void setId(int id) {
        this.id = id;
    }
}