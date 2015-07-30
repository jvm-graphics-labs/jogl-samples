/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package framework;

/**
 *
 * @author gbarbieri
 */
public class Semantic {

    public static class Attr {

        public static final int position = 0;
        public static final int normal = 1;
        public static final int color = 3;
        public static final int texCoord = 4;
        public static final int drawId = 5;
    }

    public static class Frag {

        public static final int color = 0;
        public static final int red = 0;
        public static final int green = 1;
        public static final int blue = 2;
        public static final int alpha = 0;
    }

    public static class Uniform {

        public static final int material = 0;
        public static final int transform0 = 1;
        public static final int transform1 = 2;
        public static final int indirection = 3;
        public static final int constant = 0;
        public static final int perFrame = 1;
        public static final int perPass = 2;
        public static final int light = 3;
    }

    public static class Object {

        public static final int vao = 0;
        public static final int vbo = 1;
        public static final int ibo = 2;
        public static final int size = 2;
    }
}
