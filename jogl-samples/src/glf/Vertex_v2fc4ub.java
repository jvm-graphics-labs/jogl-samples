/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glf;

import glm.vec._2.Vec2;
import dev.Vec4u8;

/**
 *
 * @author GBarbieri
 */
public class Vertex_v2fc4ub {

    public static final int SIZE = Vec2.SIZE + Vec4u8.SIZE;
            
    public Vec2 position;
    public Vec4u8 color;

    public Vertex_v2fc4ub(Vec2 position, Vec4u8 color) {
        this.position = position;
        this.color = color;
    }
}
