package com.skw.java.util;

public class Math {
    public static void main(String[] args) {
        plus(2, 4);
        plus(6, 3);
    }

    public static int plus(int a, int b) {
        int ret = a+b;
        System.out.printf("Plus: %d+%d=%d\n", a, b, ret);
        return ret;
    }

    public static int minus(int a, int b) {
        int ret = a-b;
        System.out.printf("Minus: %d-%d=%d\n", a, b, ret);
        return ret;
    }
}
