/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package framework;

import static com.jogamp.opengl.GL.GL_FALSE;
import static com.jogamp.opengl.GL.GL_TRUE;
import com.jogamp.opengl.GL2ES2;
import static com.jogamp.opengl.GL2ES2.GL_COMPILE_STATUS;
import static com.jogamp.opengl.GL2ES2.GL_INFO_LOG_LENGTH;
import static com.jogamp.opengl.GL2ES2.GL_LINK_STATUS;

/**
 *
 * @author GBarbieri
 */
public class Compiler {

    public static boolean check(GL2ES2 gl2es2, int shaderName) {

        boolean success = true;

        {
            int[] result = {GL_FALSE};
            gl2es2.glGetShaderiv(shaderName, GL_COMPILE_STATUS, result, 0);

            if (result[0] == GL_FALSE) {
                return false;
            }

            int[] infoLogLength = {0};
            gl2es2.glGetShaderiv(shaderName, GL_INFO_LOG_LENGTH, infoLogLength, 0);
            if (infoLogLength[0] > 0) {
                byte[] infoLog = new byte[infoLogLength[0]];
                gl2es2.glGetShaderInfoLog(shaderName, infoLogLength[0], null, 0, infoLog, 0);
                System.out.println(new String(infoLog));
            }

            success = success && result[0] == GL_TRUE;
        }

        return success;
    }

    public static boolean checkProgram(GL2ES2 gl2, int programName) {

        if (programName == 0) {
            return false;
        }

        int[] result = {GL_FALSE};
        gl2.glGetProgramiv(programName, GL_LINK_STATUS, result, 0);

        if (result[0] == GL_TRUE) {
            return true;
        }

        int[] infoLogLength = {0};
        gl2.glGetProgramiv(programName, GL_INFO_LOG_LENGTH, infoLogLength, 0);
        if (infoLogLength[0] > 0) {
            byte[] buffer = new byte[infoLogLength[0]];
            gl2.glGetProgramInfoLog(programName, infoLogLength[0], null, 0, buffer, 0);
            System.out.println(new String(buffer));
        }

        return result[0] == GL_TRUE;
    }
}
