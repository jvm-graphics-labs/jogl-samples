/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package framework.structureData;

/**
 *
 * @author gbarbieri
 */
public class ValuesData {

    public int SUBPIXEL_BITS;
    public int MAX_CLIP_DISTANCES;
    public int MAX_CULL_DISTANCES;
    public int MAX_COMBINED_CLIP_AND_CULL_DISTANCES;
    public long MAX_ELEMENT_INDEX;
    public int MAX_ELEMENTS_INDICES;
    public int MAX_ELEMENTS_VERTICES;
    public int IMPLEMENTATION_COLOR_READ_FORMAT;
    public int IMPLEMENTATION_COLOR_READ_TYPE;
    public boolean PRIMITIVE_RESTART_FOR_PATCHES_SUPPORTED;

    public int MAX_3D_TEXTURE_SIZE;
    public int MAX_TEXTURE_SIZE;
    public int MAX_ARRAY_TEXTURE_LAYERS;
    public int MAX_CUBE_MAP_TEXTURE_SIZE;
    public int MAX_TEXTURE_LOD_BIAS;
    public int MAX_RENDERBUFFER_SIZE;

    public float MAX_VIEWPORT_DIMS;
    public int MAX_VIEWPORTS;
    public int VIEWPORT_SUBPIXEL_BITS;
    public float[] VIEWPORT_BOUNDS_RANGE = new float[2];

    public int LAYER_PROVOKING_VERTEX;
    public int VIEWPORT_INDEX_PROVOKING_VERTEX;

    public float POINT_SIZE_MAX;
    public float POINT_SIZE_MIN;
    public float[] POINT_SIZE_RANGE = new float[2];
    public float POINT_SIZE_GRANULARITY;

    public float[] ALIASED_LINE_WIDTH_RANGE = new float[2];
    public float[] SMOOTH_LINE_WIDTH_RANGE = new float[2];
    public float SMOOTH_LINE_WIDTH_GRANULARITY;

    public int MAX_VERTEX_ATTRIB_RELATIVE_OFFSET;
    public int MAX_VERTEX_ATTRIB_BINDINGS;

    public int TEXTURE_BUFFER_OFFSET_ALIGNMENT;
}
