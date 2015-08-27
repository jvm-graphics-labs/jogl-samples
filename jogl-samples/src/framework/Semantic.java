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

        public static final int POSITION = 0;
        public static final int NORMAL = 1;
        public static final int COLOR = 3;
        public static final int TEXCOORD = 4;
        public static final int DRAWID = 5;
    }

    public static class Frag {

        public static final int COLOR = 0;
        public static final int RED = 0;
        public static final int GREEN = 1;
        public static final int BLUE = 2;
        public static final int ALPHA = 0;
    }

    public static class Uniform {

        public static final int MATERIAL = 0;
        public static final int TRANSFORM0 = 1;
        public static final int TRANSFORM1 = 2;
        public static final int INDIRECTION = 3;
        public static final int CONSTANT = 0;
        public static final int PERFRAME = 1;
        public static final int PERPASS = 2;
        public static final int LIGHT = 3;
    }

    public static class Object {

        public static final int VAO = 0;
        public static final int VBO = 1;
        public static final int IBO = 2;
        public static final int SIZE = 3;
    }
}
