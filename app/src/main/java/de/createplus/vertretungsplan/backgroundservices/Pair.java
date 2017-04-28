package de.createplus.vertretungsplan.backgroundservices;

import java.util.Arrays;

/**
 * Created by Max Nuglisch on 05.04.2017.
 */

public class Pair {
    public final String[] a;
    public final String[] b;

    public Pair(String[] a, String[] b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public String toString() {
        return "A: "+ Arrays.toString(a)+"  B: "+ Arrays.toString(b);
    }
}