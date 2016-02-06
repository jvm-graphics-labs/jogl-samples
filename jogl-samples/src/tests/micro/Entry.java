/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.micro;

import glm.vec._2.Vec2;
import dev.Vec2i;

/**
 *
 * @author GBarbieri
 */
public class Entry {

    public String string;
    public Vec2i windowSize;
    public Vec2 titleSize;
    public int drawCount;

    public Entry(String string, Vec2i windowSize, Vec2 titleSize, int drawCount) {
        this.string = string;
        this.windowSize = windowSize;
        this.titleSize = titleSize;
        this.drawCount = drawCount;
    }
}
