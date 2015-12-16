/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package framework;

import com.jogamp.opengl.math.FloatUtil;

/**
 *
 * @author GBarbieri
 */
public class Glm {

    private static float[] abs(float[] x) {
        float[] result = new float[x.length];
        for (int i = 0; i < x.length; i++) {
            result[i] = Math.abs(x[i]);
        }
        return result;
    }

    private static float[] add(float[] a, float b) {
        float[] result = new float[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] + b;
        }
        return result;
    }

    private static float[] add(float[] a, float[] b) {
        float[] result = new float[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] + b[i];
        }
        return result;
    }

    public static int ceilMultiple(int source, int multiple) {
        return source + (multiple - (source % multiple));
    }

    private static float[] clamp(float[] x, float minVal, float maxVal) {
        float[] result = new float[x.length];
        for (int i = 0; i < x.length; i++) {
            result[i] = Math.min(Math.max(x[i], minVal), maxVal);
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

    public static float[] convertLinearToSRGB(float[] colorLinear) {
        return compute_rgbToSrgb(colorLinear, 0.41666f);
    }

    public static float[] div(float[] a, float b) {
        float[] result = new float[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] / b;
        }
        return result;
    }

    public static float dot(float[] a, float[] b) {
        float result = 0;
        for (int i = 0; i < a.length; i++) {
            result += a[i] * b[i];
        }
        return result;
    }

    private static float floor(float x) {
        return (float) Math.floor(x);
    }

    private static float[] floor(float[] v) {
        float[] result = new float[v.length];
        for (int i = 0; i < v.length; i++) {
            result[i] = (float) Math.floor(v[i]);
        }
        return result;
    }

    private static float[] fract(float[] x) {
        return subtr(x, floor(x));
    }

    private static float[] grad4(float j, float[] ip) {
        float[] a = mult(new float[]{ip[0], ip[1], ip[2]}, j);
        float[] b = fract(a);
        float[] c = mult(b, 7);
        float[] d = floor(c);
        float[] e = mult(d, ip[2]);
        float[] pXYZ = subtr(e, 1);
        float pW = 1.5f - dot(abs(pXYZ), new float[]{1, 1, 1});
        float[] s = lessThan(new float[]{pXYZ[0], pXYZ[1], pXYZ[2], pW}, 0);
        pXYZ = add(pXYZ, mult(subtr(mult(new float[]{s[0], s[1], s[2]}, 2), 1), s[3]));
        return new float[]{pXYZ[0], pXYZ[1], pXYZ[2], pW};
    }

    public static float[] hsvColor(float[] rgbColor) {

        float[] hsv = {rgbColor[0], rgbColor[1], rgbColor[2]};
        float min = Math.min(Math.min(rgbColor[0], rgbColor[1]), rgbColor[2]);
        float max = Math.max(Math.max(rgbColor[0], rgbColor[1]), rgbColor[2]);
        float delta = max - min;

        hsv[2] = max;

        if (max != 0) {

            hsv[1] = delta / hsv[2];
            float h = 0;

            if (rgbColor[0] == max) {
                // between yellow & magenta
                h = 0 + 60 * (rgbColor[1] - rgbColor[2]) / delta;
            } else if (rgbColor[1] == max) {
                // between cyan & yellow
                h = 120 + 60 * (rgbColor[2] - rgbColor[0]) / delta;
            } else {
                // between magenta & cyan
                h = 240 + 60 * (rgbColor[0] - rgbColor[1]) / delta;
            }
        } else {
            // If r = g = b = 0 then s = 0, h is undefined
            hsv[1] = 0;
            hsv[0] = 0;
        }
        return hsv;
    }

    private static float[] lessThan(float[] a, float b) {
        float[] result = new float[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] < b ? 1 : 0;
        }
        return result;
    }

    private static float[] lessThan(float[] a, float[] b) {
        float[] result = new float[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] < b[i] ? 1 : 0;
        }
        return result;
    }

    public static float[] max(float[] a, float[] b) {
        float[] result = new float[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = Math.max(a[i], b[i]);
        }
        return result;
    }

    public static float[] min(float[] a, float[] b) {
        float[] result = new float[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = Math.min(a[i], b[i]);
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

    private static float[] mod(float[] x, float y) {
        float[] result = new float[x.length];
        for (int i = 0; i < x.length; i++) {
            result[i] = x[i] % y;
        }
        return result;
    }

    private static float mod289(float x) {
        return x - floor(x * 1 / 289) * 289;
    }

    private static float[] mod289(float[] x) {
        float[] result = new float[x.length];
        for (int i = 0; i < x.length; i++) {
            result[i] = x[i] - floor(x[i] * 1 / 289) * 289;
        }
        return result;
    }

    public static float[] mult(float[] a, float b) {
        float[] result = new float[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] * b;
        }
        return result;
    }

    public static float[] mult(float[] a, float[] b) {
        float[] result = new float[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] * b[i];
        }
        return result;
    }

    public static float[] normalize(float[] a) {
        float dot = dot(a, a);
        float mod = (float) Math.sqrt(dot);
        return div(a, mod);
    }

    private static float[] pow(float[] base, float exponent) {
        float[] result = new float[base.length];
        for (int i = 0; i < base.length; i++) {
            result[i] = (float) Math.pow(base[i], exponent);
        }
        return result;
    }

    private static float permute(float x) {
        return mod289((x * 34 + 1) * x);
    }

    private static float[] permute(float[] x) {
        return mod289(mult(add(mult(x, 34), 1), x));
    }

    public static float[] project(float[] obj, float[] model, float[] proj, float[] viewport) {

        float[] tmp = {obj[0], obj[1], obj[2], 1};
        float[] partial = new float[tmp.length];
        FloatUtil.multMatrixVec(model, tmp, partial);
        FloatUtil.multMatrixVec(proj, partial, tmp);

        tmp[0] = tmp[0] / tmp[3] * 0.5f + 0.5f;
        tmp[1] = tmp[1] / tmp[3] * 0.5f + 0.5f;
        tmp[2] = tmp[2] / tmp[3] * 0.5f + 0.5f;
        tmp[3] = 1.0f;

        tmp[0] = tmp[0] * (viewport[2]) + viewport[0];
        tmp[1] = tmp[1] * (viewport[3]) + viewport[1];

        return new float[]{tmp[0], tmp[1], tmp[2]};
    }

    public static float[] rgbColor(float[] hsvColor) {

        float[] hsv = hsvColor;
        float[] rgbColor = new float[3];

        if (hsv[1] == 0) {
            // achromatic (grey)
            rgbColor[0] = rgbColor[1] = rgbColor[2] = hsv[2];
        } else {
            float sector = floor(hsv[0] / 60);
            float frac = (hsv[0] / 60) - sector;
            // factorial part of h
            float o = hsv[2] * (1 - hsv[1]);
            float p = hsv[2] * (1 - hsv[1] * frac);
            float q = hsv[2] * (1 - hsv[1] * (1 - frac));

            switch ((int) sector) {

                default:
                case 0:
                    rgbColor[0] = hsv[2];
                    rgbColor[1] = q;
                    rgbColor[2] = o;
                    break;
                case 1:
                    rgbColor[0] = p;
                    rgbColor[1] = hsv[2];
                    rgbColor[2] = o;
                    break;
                case 2:
                    rgbColor[0] = o;
                    rgbColor[1] = hsv[2];
                    rgbColor[2] = q;
                    break;
                case 3:
                    rgbColor[0] = o;
                    rgbColor[1] = p;
                    rgbColor[2] = hsv[2];
                    break;
                case 4:
                    rgbColor[0] = q;
                    rgbColor[1] = o;
                    rgbColor[2] = hsv[2];
                    break;
                case 5:
                    rgbColor[0] = hsv[2];
                    rgbColor[1] = o;
                    rgbColor[2] = p;
                    break;
            }
        }

        return rgbColor;
    }

    public static float[] saturation(float s, float[] color) {

        float[] result = FloatUtil.multMatrixVec(saturation(s), new float[]{color[0], color[1], color[2], 0}, new float[4]);

        return new float[]{result[0], result[1], result[2]};
    }

    private static float[] saturation(float s) {

        float[] rgbw = {0.2126f, 0.7152f, 0.0722f};

        float col0 = (1 - s) * rgbw[0];
        float col1 = (1 - s) * rgbw[1];
        float col2 = (1 - s) * rgbw[2];

        float[] result = FloatUtil.makeIdentity(new float[16]);
        result[0 * 4 + 0] = col0 + s;
        result[0 * 4 + 1] = col0;
        result[0 * 4 + 2] = col0;
        result[1 * 4 + 0] = col1;
        result[1 * 4 + 1] = col1 + s;
        result[1 * 4 + 2] = col1;
        result[2 * 4 + 0] = col2;
        result[2 * 4 + 1] = col2;
        result[2 * 4 + 2] = col2 + s;
        return result;
    }

    public static float simplex(float[] v) {

        float[] c = {
            +0.138196601125011f, // (5 - sqrt(5))/20  G4
            +0.276393202250021f, // 2 * G4
            +0.414589803375032f, // 3 * G4
            -0.447213595499958f}; // -1 + 4 * G4

        // (sqrt(5) - 1)/4 = F4, used once below
        float f4 = 0.309016994374947451f;

        // First corner
        float[] i = floor(add(v, dot(v, new float[]{f4, f4, f4, f4})));
        float[] x0 = add(subtr(v, i), dot(i, new float[]{c[0], c[0], c[0], c[0]}));

        // Other corners
        // Rank sorting originally contributed by Bill Licea-Kane, AMD (formerly ATI)
        float[] i0;
        float[] isX = step(new float[]{x0[1], x0[2], x0[3]}, new float[]{x0[0], x0[0], x0[0]});
        float[] isYZ = step(new float[]{x0[2], x0[3], x0[3]}, new float[]{x0[1], x0[1], x0[2]});
        //  i0.x = dot(isX, vec3(1.0));
        //i0.x = isX.x + isX.y + isX.z;
        //i0.yzw = static_cast<T>(1) - isX;
        i0 = new float[]{isX[0] + isX[1] + isX[2], subtr(1, isX)[0], subtr(1, isX)[1], subtr(1, isX)[2]};
        //  i0.y += dot(isYZ.xy, vec2(1.0));
        i0[1] += isYZ[0] + isYZ[1];
        //i0.zw += 1.0 - tvec2<T, P>(isYZ.x, isYZ.y);
        i0[2] += 1 - isYZ[0];
        i0[3] += 1 - isYZ[1];
        i0[2] += isYZ[2];
        i0[3] += 1 - isYZ[2];

        // i0 now contains the unique values 0,1,2,3 in each channel
        float[] i3 = clamp(i0, 0, 1);
        float[] i2 = clamp(subtr(i0, 1), 0, 1);
        float[] i1 = clamp(subtr(i0, 2), 0, 1);

        //  x0 = x0 - 0.0 + 0.0 * C.xxxx
        //  x1 = x0 - i1  + 0.0 * C.xxxx
        //  x2 = x0 - i2  + 0.0 * C.xxxx
        //  x3 = x0 - i3  + 0.0 * C.xxxx
        //  x4 = x0 - 1.0 + 4.0 * C.xxxx
        float[] x1 = add(subtr(x0, i1), c[0]);
        float[] x2 = add(subtr(x0, i2), c[1]);
        float[] x3 = add(subtr(x0, i3), c[2]);
        float[] x4 = add(x0, c[3]);

        // Permutations
        i = mod(i, 289);
        float j0 = permute(permute(permute(permute(i[3]) + i[2]) + i[1]) + i[0]);
        float[] j1 = permute(add(permute(add(permute(add(permute(add(new float[]{i1[3], i2[3], i3[3], 1}, i[3])),
                add(new float[]{i1[2], i2[2], i3[2], 1}, i[2]))), add(new float[]{i1[1], i2[1], i3[1], 1}, i[1]))),
                add(new float[]{i1[0], i2[0], i3[0], 1}, i[0])));

        // Gradients: 7x7x6 points over a cube, mapped onto a 4-cross polytope
        // 7*7*6 = 294, which is close to the ring size 17*17 = 289.
        float[] ip = {1f / 294, 1f / 49, 1f / 7, 0};

        float[] p0 = grad4(j0, ip);
        float[] p1 = grad4(j1[0], ip);
        float[] p2 = grad4(j1[1], ip);
        float[] p3 = grad4(j1[2], ip);
        float[] p4 = grad4(j1[3], ip);

        // Normalise gradients
        float[] norm = taylorInvSqrt(new float[]{dot(p0, p0), dot(p1, p1), dot(p2, p2), dot(p3, p3)});
        p0 = mult(p0, norm[0]);
        p1 = mult(p1, norm[1]);
        p2 = mult(p2, norm[2]);
        p3 = mult(p3, norm[3]);
        p4 = mult(p4, taylorInvSqrt(dot(p4, p4)));

        // Mix contributions from the five corners
        float[] m0 = max(subtr(0.6f, new float[]{dot(x0, x0), dot(x1, x1), dot(x2, x2)}), new float[]{0, 0, 0});
        float[] m1 = max(subtr(0.6f, new float[]{dot(x3, x3), dot(x4, x4)}), new float[]{0, 0});

        m0 = mult(m0, m0);
        m1 = mult(m1, m1);

        return 49 * (dot(mult(m0, m0), new float[]{dot(p0, x0), dot(p1, x1), dot(p2, x2)})
                + dot(mult(m1, m1), new float[]{dot(p3, x3), dot(p4, x4)}));
    }

    private static float[] step(float[] edge, float[] x) {
        float[] one = new float[edge.length];
        float[] zero = new float[edge.length];
        for (int i = 0; i < edge.length; i++) {
            one[i] = 1;
            zero[i] = 0;
        }
        return mix(one, zero, lessThan(x, edge));
    }

    private static float[] subtr(float a, float[] b) {
        float[] result = new float[b.length];
        for (int i = 0; i < b.length; i++) {
            result[i] = a - b[i];
        }
        return result;
    }

    private static float[] subtr(float[] a, float[] b) {
        float[] result = new float[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] - b[i];
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

    private static float taylorInvSqrt(float r) {
        return 1.79284291400159f - 0.85373472095314f * r;
    }

    private static float[] taylorInvSqrt(float[] r) {
        return subtr(1.79284291400159f, mult(r, 0.85373472095314f));
    }

    public static float[] yawPitchRoll(float yaw, float pitch, float roll) {

        float tmp_ch = (float) Math.cos(yaw);
        float tmp_sh = (float) Math.sin(yaw);
        float tmp_cp = (float) Math.cos(pitch);
        float tmp_sp = (float) Math.sin(pitch);
        float tmp_cb = (float) Math.cos(roll);
        float tmp_sb = (float) Math.sin(roll);

        float[] result = new float[9];

        result[0] = tmp_ch * tmp_cb + tmp_sh * tmp_sp * tmp_sb;
        result[1] = tmp_sb * tmp_cp;
        result[2] = -tmp_sh * tmp_cb + tmp_ch * tmp_sp * tmp_sb;
//        result[3] = 0;
        result[3] = -tmp_ch * tmp_sb + tmp_sh * tmp_sp * tmp_cb;
        result[4] = tmp_cb * tmp_cp;
        result[5] = tmp_sb * tmp_sh + tmp_ch * tmp_sp * tmp_cb;
//        result[7] = 0;
        result[6] = tmp_sh * tmp_cp;
        result[7] = -tmp_sp;
        result[8] = tmp_ch * tmp_cp;
//        result[11] = 0;
//        result[12] = 0;
//        result[13] = 0;
//        result[14] = 0;
//        result[15] = 1;

        return result;
    }
}
