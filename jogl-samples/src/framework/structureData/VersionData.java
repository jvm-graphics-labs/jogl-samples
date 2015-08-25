/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package framework.structureData;

import framework.Profile;

/**
 *
 * @author gbarbieri
 */
public class VersionData {

    public VersionData(Profile profile) {
        PROFILE = profile;
    }

    public Profile PROFILE;
    public int MINOR_VERSION;
    public int MAJOR_VERSION;
    public int CONTEXT_FLAGS;
    public int NUM_EXTENSIONS;
    public String RENDERER;
    public String VENDOR;
    public String VERSION;
    public String SHADING_LANGUAGE_VERSION;
    public int NUM_SHADING_LANGUAGE_VERSIONS;
    public boolean GLSL100;
    public boolean GLSL110;
    public boolean GLSL120;
    public boolean GLSL130;
    public boolean GLSL140;
    public boolean GLSL150Core;
    public boolean GLSL150Comp;
    public boolean GLSL300ES;
    public boolean GLSL330Core;
    public boolean GLSL330Comp;
    public boolean GLSL400Core;
    public boolean GLSL400Comp;
    public boolean GLSL410Core;
    public boolean GLSL410Comp;
    public boolean GLSL420Core;
    public boolean GLSL420Comp;
    public boolean GLSL430Core;
    public boolean GLSL430Comp;
    public boolean GLSL440Core;
    public boolean GLSL440Comp;
}
