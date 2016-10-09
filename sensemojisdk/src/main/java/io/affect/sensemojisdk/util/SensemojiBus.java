package io.affect.sensemojisdk.util;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * Created by Kevin on 1/12/2016.
 */
public class SensemojiBus {
    public static Bus bus = new Bus(ThreadEnforcer.MAIN);
}
