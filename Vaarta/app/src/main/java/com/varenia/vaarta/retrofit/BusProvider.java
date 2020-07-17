package com.varenia.vaarta.retrofit;

import com.squareup.otto.Bus;

/**
 * Created by VCIMS-PC2 on 26-09-2017.
 */

public class BusProvider {

    private static final Bus BUS = new Bus();

    public static Bus getInstance(){
        return BUS;
    }

    public BusProvider(){}

}
