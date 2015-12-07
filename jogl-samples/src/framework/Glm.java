/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package framework;

/**
 *
 * @author GBarbieri
 */
public class Glm {

    public static int ceilMultiple(int source, int multiple) {
        return source + (multiple - (source % multiple));
    }

    public static float[] convertLinearToSRGB(float[] colorLinear) {
        return compute_rgbToSrgb(colorLinear, 0.41666f);
    }

    public static float[] normalize(float[] a) {
        float dot = dot(a, a);
        float mod = (float) Math.sqrt(dot);
        return div(a, mod);
    }

    public static float dot(float[] a, float[] b) {
        float result = 0;
        for (int i = 0; i < a.length; i++) {
            result += a[i] * b[i];
        }
        return result;
    }

    private static float[] compute_rgbToSrgb(float[] colorLinear, float gammaCorrection) {

        float[] clampedColor = new float[colorLinear.length];
        for (int i = 0; i < clampedColor.length; i++) {
            clampedColor[i] = Math.max(0, colorLinear[i]);
            clampedColor[i] = Math.min(1, colorLinear[i]);
        }
        float[] a = pow(clampedColor, gammaCorrection);
        a = mult(a, 1.055f);
        a = subtr(a, 0.055f);
        float[] b = mult(clampedColor, 12.92f);
        float[] c = lessThan(clampedColor, 0.0031308f);
        return mix(a, b, c);
    }

    private static float[] pow(float[] base, float exponent) {
        float[] result = new float[base.length];
        for (int i = 0; i < base.length; i++) {
            result[i] = (float) Math.pow(base[i], exponent);
        }
        return result;
    }

    private static float[] mult(float[] a, float b) {
        float[] result = new float[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] * b;
        }
        return result;
    }

    private static float[] subtr(float[] a, float b) {
        float[] result = new float[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] - b;
        }
        return result;
    }

    private static float[] div(float[] a, float b) {
        float[] result = new float[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] / b;
        }
        return result;
    }

    private static float[] lessThan(float[] a, float b) {
        float[] result = new float[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] < b ? 1 : 0;
        }
        return result;
    }

    private static float[] mix(float[] x, float[] y, float[] a) {
        float[] result = new float[x.length];
        for (int i = 0; i < x.length; i++) {
            result[i] = x[i] + a[i] * (y[i] - x[i]);
        }
        return result;
    }

}
