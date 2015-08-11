/*
 * Copyright (c) 2013. CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */
package com.cmg.android.pension.animation;

/**
 * DOCME
 *
 * @author $Author$
 * @version $Revision$
 * @Creator LongNguyen
 * @Last changed: $LastChangedDate$
 */
public class Speed {
    public static final int DIRECTION_RIGHT = 1;
    public static final int DIRECTION_LEFT = -1;
    public static final int DIRECTION_UP = -1;
    public static final int DIRECTION_DOWN = 1;

    private float xv = 1;    // velocity value on the X axis
    private float yv = 1;    // velocity value on the Y axis

    private int xDirection = DIRECTION_RIGHT;
    private int yDirection = DIRECTION_DOWN;

    /**
     * initial speed for x and y
     */
    public Speed() {
        this.xv = 20;
        this.yv = 20;
    }

    /**
     * Constructor
     *
     * @param xv
     * @param yv
     */
    public Speed(float xv, float yv) {
        this.xv = xv;
        this.yv = yv;
    }

    /**
     * get X
     *
     * @return
     */
    public float getXv() {
        return xv;
    }

    /**
     * set X
     *
     * @param xv
     */
    public void setXv(float xv) {
        this.xv = xv;
    }

    /**
     * get Y
     *
     * @return
     */
    public float getYv() {
        return yv;
    }

    /**
     * set Y
     *
     * @param yv
     */
    public void setYv(float yv) {
        this.yv = yv;
    }

    /**
     * get direction X
     *
     * @return
     */
    public int getxDirection() {
        return xDirection;
    }

    /**
     * set direction X
     *
     * @param xDirection
     */
    public void setxDirection(int xDirection) {
        this.xDirection = xDirection;
    }

    /**
     * get direction Y
     *
     * @return
     */
    public int getyDirection() {
        return yDirection;
    }

    /**
     * set direction Y
     *
     * @param yDirection
     */
    public void setyDirection(int yDirection) {
        this.yDirection = yDirection;
    }

    /**
     * changes the direction on the X axis
     */
    public void toggleXDirection() {
        xDirection = xDirection * -1;
    }

    /**
     * changes the direction on the Y axis
     */
    public void toggleYDirection() {
        yDirection = yDirection * -1;
    }
}
