/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package framework;

import framework.structureData.LimitsData;
import framework.structureData.ValuesData;
import framework.structureData.VersionData;
import framework.structureData.DebugData;
import framework.structureData.ExtensionData;
import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2.*;
import com.jogamp.opengl.GL2ES3;
import com.jogamp.opengl.GL4;
import static com.jogamp.opengl.GL4.*;
import static framework.Profile.*;
import framework.structureData.FormatsData;

/**
 *
 * @author gbarbieri
 */
public class Caps {

    public Caps(GL gl, Profile profile) {

        initVersion(gl, profile);
        initExtensions(gl);
        initDebug(gl);
        initLimits(gl);
        initValues(gl);
    }

    private VersionData versionData;
    private ExtensionData extensionData;
    private DebugData debugData;
    private LimitsData limitsData;
    private ValuesData valuesData;
    private FormatsData formatsData;
    
    private int[] tmp = new int[1];
    private long[] tmp64 = new long[1];
    private float[] tmpF = new float[2];

    private boolean check(int majorVersionRequire, int minorVersionRequire) {
        return (versionData.MAJOR_VERSION * 100 + versionData.MINOR_VERSION * 10)
                >= (majorVersionRequire * 100 + minorVersionRequire * 10);
    }

    private void initVersion(GL gl, Profile profile) {

        versionData = new VersionData(profile);

        gl.glGetIntegerv(GL_MINOR_VERSION, tmp, 0);
        versionData.MINOR_VERSION = tmp[0];
        gl.glGetIntegerv(GL_MAJOR_VERSION, tmp, 0);
        versionData.MAJOR_VERSION = tmp[0];

        versionData.RENDERER = gl.glGetString(GL_RENDERER);
        versionData.VENDOR = gl.glGetString(GL_VENDOR);
        versionData.VERSION = gl.glGetString(GL_VERSION);
        versionData.SHADING_LANGUAGE_VERSION = gl.glGetString(GL_SHADING_LANGUAGE_VERSION);

        if (check(4, 3) || extensionData.KHR_debug) {

            gl.glGetIntegerv(GL_CONTEXT_FLAGS, tmp, 0);
            versionData.CONTEXT_FLAGS = tmp[0];
        }
        if (check(3, 0)) {

            gl.glGetIntegerv(GL_NUM_EXTENSIONS, tmp, 0);
            versionData.NUM_EXTENSIONS = tmp[0];
        }
        if (check(4, 3)) {

            GL4 gl4 = (GL4) gl;

            gl.glGetIntegerv(GL_NUM_SHADING_LANGUAGE_VERSIONS, tmp, 0);
            versionData.NUM_SHADING_LANGUAGE_VERSIONS = tmp[0];

            for (int i = 0; i < versionData.NUM_SHADING_LANGUAGE_VERSIONS; i++) {

                String version = gl4.glGetStringi(GL_SHADING_LANGUAGE_VERSION, i);

                switch (version) {
                    case "100":
                        versionData.GLSL100 = true;
                        break;
                    case "110":
                        versionData.GLSL110 = true;
                        break;
                    case "120":
                        versionData.GLSL120 = true;
                        break;
                    case "130":
                        versionData.GLSL130 = true;
                        break;
                    case "140":
                        versionData.GLSL140 = true;
                        break;
                    case "150 core":
                        versionData.GLSL150Core = true;
                        break;
                    case "150 compatibility":
                        versionData.GLSL150Comp = true;
                        break;
                    case "300 es":
                        versionData.GLSL300ES = true;
                        break;
                    case "330 core":
                        versionData.GLSL330Core = true;
                        break;
                    case "330 compatibility":
                        versionData.GLSL330Comp = true;
                        break;
                    case "400 core":
                        versionData.GLSL400Core = true;
                        break;
                    case "400 compatibility":
                        versionData.GLSL400Comp = true;
                        break;
                    case "410 core":
                        versionData.GLSL410Core = true;
                        break;
                    case "410 compatibility":
                        versionData.GLSL410Comp = true;
                        break;
                    case "420 core":
                        versionData.GLSL420Core = true;
                        break;
                    case "420 compatibility":
                        versionData.GLSL420Comp = true;
                        break;
                    case "430 core":
                        versionData.GLSL430Core = true;
                        break;
                    case "440 compatibility":
                        versionData.GLSL440Comp = true;
                        break;
                    case "440 core":
                        versionData.GLSL440Core = true;
                        break;
                }
            }
        }
    }

    private void initExtensions(GL gl) {

        extensionData = new ExtensionData();

        GL2ES3 gl2es3 = (GL2ES3) gl;

        gl.glGetIntegerv(GL_NUM_EXTENSIONS, tmp, 0);
        versionData.NUM_EXTENSIONS = tmp[0];

        if (versionData.PROFILE == CORE || versionData.PROFILE == COMPATIBILITY) {

            for (int i = 0; i < versionData.NUM_EXTENSIONS; i++) {

                String extension = gl2es3.glGetStringi(GL_EXTENSIONS, i);

                switch (extension) {
                    case "GL_ARB_multitexture":
                        extensionData.ARB_multitexture = true;
                        break;
                    case "GL_ARB_transpose_matrix":
                        extensionData.ARB_transpose_matrix = true;
                        break;
                    case "GL_ARB_multisample":
                        extensionData.ARB_multisample = true;
                        break;
                    case "GL_ARB_texture_env_add":
                        extensionData.ARB_texture_env_add = true;
                        break;
                    case "GL_ARB_texture_cube_map":
                        extensionData.ARB_texture_cube_map = true;
                        break;
                    case "GL_ARB_texture_compression":
                        extensionData.ARB_texture_compression = true;
                        break;
                    case "GL_ARB_texture_border_clamp":
                        extensionData.ARB_texture_border_clamp = true;
                        break;
                    case "GL_ARB_point_parameters":
                        extensionData.ARB_point_parameters = true;
                        break;
                    case "GL_ARB_vertex_blend":
                        extensionData.ARB_vertex_blend = true;
                        break;
                    case "GL_ARB_matrix_palette":
                        extensionData.ARB_matrix_palette = true;
                        break;
                    case "GL_ARB_texture_env_combine":
                        extensionData.ARB_texture_env_combine = true;
                        break;
                    case "GL_ARB_texture_env_crossbar":
                        extensionData.ARB_texture_env_crossbar = true;
                        break;
                    case "GL_ARB_texture_env_dot3":
                        extensionData.ARB_texture_env_dot3 = true;
                        break;
                    case "GL_ARB_texture_mirrored_repeat":
                        extensionData.ARB_texture_mirrored_repeat = true;
                        break;
                    case "GL_ARB_depth_texture":
                        extensionData.ARB_depth_texture = true;
                        break;
                    case "GL_ARB_shadow":
                        extensionData.ARB_shadow = true;
                        break;
                    case "GL_ARB_shadow_ambient":
                        extensionData.ARB_shadow_ambient = true;
                        break;
                    case "GL_ARB_window_pos":
                        extensionData.ARB_window_pos = true;
                        break;
                    case "GL_ARB_vertex_program":
                        extensionData.ARB_vertex_program = true;
                        break;
                    case "GL_ARB_fragment_program":
                        extensionData.ARB_fragment_program = true;
                        break;
                    case "GL_ARB_vertex_buffer_object":
                        extensionData.ARB_vertex_buffer_object = true;
                        break;
                    case "GL_ARB_occlusion_query":
                        extensionData.ARB_occlusion_query = true;
                        break;
                    case "GL_ARB_shader_objects":
                        extensionData.ARB_shader_objects = true;
                        break;
                    case "GL_ARB_vertex_shader":
                        extensionData.ARB_vertex_shader = true;
                        break;
                    case "GL_ARB_fragment_shader":
                        extensionData.ARB_fragment_shader = true;
                        break;
                    case "GL_ARB_shading_language_100":
                        extensionData.ARB_shading_language_100 = true;
                        break;
                    case "GL_ARB_texture_non_power_of_two":
                        extensionData.ARB_texture_non_power_of_two = true;
                        break;
                    case "GL_ARB_point_sprite":
                        extensionData.ARB_point_sprite = true;
                        break;
                    case "GL_ARB_fragment_program_shadow":
                        extensionData.ARB_fragment_program_shadow = true;
                        break;
                    case "GL_ARB_draw_buffers":
                        extensionData.ARB_draw_buffers = true;
                        break;
                    case "GL_ARB_texture_rectangle":
                        extensionData.ARB_texture_rectangle = true;
                        break;
                    case "GL_ARB_color_buffer_float":
                        extensionData.ARB_color_buffer_float = true;
                        break;
                    case "GL_ARB_half_float_pixel":
                        extensionData.ARB_half_float_pixel = true;
                        break;
                    case "GL_ARB_texture_float":
                        extensionData.ARB_texture_float = true;
                        break;
                    case "GL_ARB_pixel_buffer_object":
                        extensionData.ARB_pixel_buffer_object = true;
                        break;
                    case "GL_ARB_depth_buffer_float":
                        extensionData.ARB_depth_buffer_float = true;
                        break;
                    case "GL_ARB_draw_instanced":
                        extensionData.ARB_draw_instanced = true;
                        break;
                    case "GL_ARB_framebuffer_object":
                        extensionData.ARB_framebuffer_object = true;
                        break;
                    case "GL_ARB_framebuffer_sRGB":
                        extensionData.ARB_framebuffer_sRGB = true;
                        break;
                    case "GL_ARB_geometry_shader4":
                        extensionData.ARB_geometry_shader4 = true;
                        break;
                    case "GL_ARB_half_float_vertex":
                        extensionData.ARB_half_float_vertex = true;
                        break;
                    case "GL_ARB_instanced_arrays":
                        extensionData.ARB_instanced_arrays = true;
                        break;
                    case "GL_ARB_map_buffer_range":
                        extensionData.ARB_map_buffer_range = true;
                        break;
                    case "GL_ARB_texture_buffer_object":
                        extensionData.ARB_texture_buffer_object = true;
                        break;
                    case "GL_ARB_texture_compression_rgtc":
                        extensionData.ARB_texture_compression_rgtc = true;
                        break;
                    case "GL_ARB_texture_rg":
                        extensionData.ARB_texture_rg = true;
                        break;
                    case "GL_ARB_vertex_array_object":
                        extensionData.ARB_vertex_array_object = true;
                        break;
                    case "GL_ARB_uniform_buffer_object":
                        extensionData.ARB_uniform_buffer_object = true;
                        break;
                    case "GL_ARB_compatibility":
                        extensionData.ARB_compatibility = true;
                        break;
                    case "GL_ARB_copy_buffer":
                        extensionData.ARB_copy_buffer = true;
                        break;
                    case "GL_ARB_shader_texture_lod":
                        extensionData.ARB_shader_texture_lod = true;
                        break;
                    case "GL_ARB_depth_clamp":
                        extensionData.ARB_depth_clamp = true;
                        break;
                    case "GL_ARB_draw_elements_base_vertex":
                        extensionData.ARB_draw_elements_base_vertex = true;
                        break;
                    case "GL_ARB_fragment_coord_conventions":
                        extensionData.ARB_fragment_coord_conventions = true;
                        break;
                    case "GL_ARB_provoking_vertex":
                        extensionData.ARB_provoking_vertex = true;
                        break;
                    case "GL_ARB_seamless_cube_map":
                        extensionData.ARB_seamless_cube_map = true;
                        break;
                    case "GL_ARB_sync":
                        extensionData.ARB_sync = true;
                        break;
                    case "GL_ARB_texture_multisample":
                        extensionData.ARB_texture_multisample = true;
                        break;
                    case "GL_ARB_vertex_array_bgra":
                        extensionData.ARB_vertex_array_bgra = true;
                        break;
                    case "GL_ARB_draw_buffers_blend":
                        extensionData.ARB_draw_buffers_blend = true;
                        break;
                    case "GL_ARB_sample_shading":
                        extensionData.ARB_sample_shading = true;
                        break;
                    case "GL_ARB_texture_cube_map_array":
                        extensionData.ARB_texture_cube_map_array = true;
                        break;
                    case "GL_ARB_texture_gather":
                        extensionData.ARB_texture_gather = true;
                        break;
                    case "GL_ARB_texture_query_lod":
                        extensionData.ARB_texture_query_lod = true;
                        break;
                    case "GL_ARB_shading_language_include":
                        extensionData.ARB_shading_language_include = true;
                        break;
                    case "GL_ARB_texture_compression_bptc":
                        extensionData.ARB_texture_compression_bptc = true;
                        break;
                    case "GL_ARB_blend_func_extended":
                        extensionData.ARB_blend_func_extended = true;
                        break;
                    case "GL_ARB_explicit_attrib_location":
                        extensionData.ARB_explicit_attrib_location = true;
                        break;
                    case "GL_ARB_occlusion_query2":
                        extensionData.ARB_occlusion_query2 = true;
                        break;
                    case "GL_ARB_sampler_objects":
                        extensionData.ARB_sampler_objects = true;
                        break;
                    case "GL_ARB_shader_bit_encoding":
                        extensionData.ARB_shader_bit_encoding = true;
                        break;
                    case "GL_ARB_texture_rgb10_a2ui":
                        extensionData.ARB_texture_rgb10_a2ui = true;
                        break;
                    case "GL_ARB_texture_swizzle":
                        extensionData.ARB_texture_swizzle = true;
                        break;
                    case "GL_ARB_timer_query":
                        extensionData.ARB_timer_query = true;
                        break;
                    case "GL_ARB_vertex_type_2_10_10_10_rev":
                        extensionData.ARB_vertex_type_2_10_10_10_rev = true;
                        break;
                    case "GL_ARB_draw_indirect":
                        extensionData.ARB_draw_indirect = true;
                        break;
                    case "GL_ARB_gpu_shader5":
                        extensionData.ARB_gpu_shader5 = true;
                        break;
                    case "GL_ARB_gpu_shader_fp64":
                        extensionData.ARB_gpu_shader_fp64 = true;
                        break;
                    case "GL_ARB_shader_subroutine":
                        extensionData.ARB_shader_subroutine = true;
                        break;
                    case "GL_ARB_tessellation_shader":
                        extensionData.ARB_tessellation_shader = true;
                        break;
                    case "GL_ARB_texture_buffer_object_rgb32":
                        extensionData.ARB_texture_buffer_object_rgb32 = true;
                        break;
                    case "GL_ARB_transform_feedback2":
                        extensionData.ARB_transform_feedback2 = true;
                        break;
                    case "GL_ARB_transform_feedback3":
                        extensionData.ARB_transform_feedback3 = true;
                        break;
                    case "GL_ARB_ES2_compatibility":
                        extensionData.ARB_ES2_compatibility = true;
                        break;
                    case "GL_ARB_get_program_binary":
                        extensionData.ARB_get_program_binary = true;
                        break;
                    case "GL_ARB_separate_shader_objects":
                        extensionData.ARB_separate_shader_objects = true;
                        break;
                    case "GL_ARB_shader_precision":
                        extensionData.ARB_shader_precision = true;
                        break;
                    case "GL_ARB_vertex_attrib_64bit":
                        extensionData.ARB_vertex_attrib_64bit = true;
                        break;
                    case "GL_ARB_viewport_array":
                        extensionData.ARB_viewport_array = true;
                        break;
                    case "GL_ARB_cl_event":
                        extensionData.ARB_cl_event = true;
                        break;
                    case "GL_ARB_debug_output":
                        extensionData.ARB_debug_output = true;
                        break;
                    case "GL_ARB_robustness":
                        extensionData.ARB_robustness = true;
                        break;
                    case "GL_ARB_shader_stencil_export":
                        extensionData.ARB_shader_stencil_export = true;
                        break;
                    case "GL_ARB_base_instance":
                        extensionData.ARB_base_instance = true;
                        break;
                    case "GL_ARB_shading_language_420pack":
                        extensionData.ARB_shading_language_420pack = true;
                        break;
                    case "GL_ARB_transform_feedback_instanced":
                        extensionData.ARB_transform_feedback_instanced = true;
                        break;
                    case "GL_ARB_compressed_texture_pixel_storage":
                        extensionData.ARB_compressed_texture_pixel_storage = true;
                        break;
                    case "GL_ARB_conservative_depth":
                        extensionData.ARB_conservative_depth = true;
                        break;
                    case "GL_ARB_internalformat_query":
                        extensionData.ARB_internalformat_query = true;
                        break;
                    case "GL_ARB_map_buffer_alignment":
                        extensionData.ARB_map_buffer_alignment = true;
                        break;
                    case "GL_ARB_shader_atomic_counters":
                        extensionData.ARB_shader_atomic_counters = true;
                        break;
                    case "GL_ARB_shader_image_load_store":
                        extensionData.ARB_shader_image_load_store = true;
                        break;
                    case "GL_ARB_shading_language_packing":
                        extensionData.ARB_shading_language_packing = true;
                        break;
                    case "GL_ARB_texture_storage":
                        extensionData.ARB_texture_storage = true;
                        break;
                    case "GL_KHR_texture_compression_astc_hdr":
                        extensionData.KHR_texture_compression_astc_hdr = true;
                        break;
                    case "GL_KHR_texture_compression_astc_ldr":
                        extensionData.KHR_texture_compression_astc_ldr = true;
                        break;
                    case "GL_KHR_debug":
                        extensionData.KHR_debug = true;
                        break;
                    case "GL_ARB_arrays_of_arrays":
                        extensionData.ARB_arrays_of_arrays = true;
                        break;
                    case "GL_ARB_clear_buffer_object":
                        extensionData.ARB_clear_buffer_object = true;
                        break;
                    case "GL_ARB_compute_shader":
                        extensionData.ARB_compute_shader = true;
                        break;
                    case "GL_ARB_copy_image":
                        extensionData.ARB_copy_image = true;
                        break;
                    case "GL_ARB_texture_view":
                        extensionData.ARB_texture_view = true;
                        break;
                    case "GL_ARB_vertex_attrib_binding":
                        extensionData.ARB_vertex_attrib_binding = true;
                        break;
                    case "GL_ARB_robustness_isolation":
                        extensionData.ARB_robustness_isolation = true;
                        break;
                    case "GL_ARB_ES3_compatibility":
                        extensionData.ARB_ES3_compatibility = true;
                        break;
                    case "GL_ARB_explicit_uniform_location":
                        extensionData.ARB_explicit_uniform_location = true;
                        break;
                    case "GL_ARB_fragment_layer_viewport":
                        extensionData.ARB_fragment_layer_viewport = true;
                        break;
                    case "GL_ARB_framebuffer_no_attachments":
                        extensionData.ARB_framebuffer_no_attachments = true;
                        break;
                    case "GL_ARB_internalformat_query2":
                        extensionData.ARB_internalformat_query2 = true;
                        break;
                    case "GL_ARB_invalidate_subdata":
                        extensionData.ARB_invalidate_subdata = true;
                        break;
                    case "GL_ARB_multi_draw_indirect":
                        extensionData.ARB_multi_draw_indirect = true;
                        break;
                    case "GL_ARB_program_interface_query":
                        extensionData.ARB_program_interface_query = true;
                        break;
                    case "GL_ARB_robust_buffer_access_behavior":
                        extensionData.ARB_robust_buffer_access_behavior = true;
                        break;
                    case "GL_ARB_shader_image_size":
                        extensionData.ARB_shader_image_size = true;
                        break;
                    case "GL_ARB_shader_storage_buffer_object":
                        extensionData.ARB_shader_storage_buffer_object = true;
                        break;
                    case "GL_ARB_stencil_texturing":
                        extensionData.ARB_stencil_texturing = true;
                        break;
                    case "GL_ARB_texture_buffer_range":
                        extensionData.ARB_texture_buffer_range = true;
                        break;
                    case "GL_ARB_texture_query_levels":
                        extensionData.ARB_texture_query_levels = true;
                        break;
                    case "GL_ARB_texture_storage_multisample":
                        extensionData.ARB_texture_storage_multisample = true;
                        break;
                    case "GL_ARB_buffer_storage":
                        extensionData.ARB_buffer_storage = true;
                        break;
                    case "GL_ARB_clear_texture":
                        extensionData.ARB_clear_texture = true;
                        break;
                    case "GL_ARB_enhanced_layouts":
                        extensionData.ARB_enhanced_layouts = true;
                        break;
                    case "GL_ARB_multi_bind":
                        extensionData.ARB_multi_bind = true;
                        break;
                    case "GL_ARB_query_buffer_object":
                        extensionData.ARB_query_buffer_object = true;
                        break;
                    case "GL_ARB_texture_mirror_clamp_to_edge":
                        extensionData.ARB_texture_mirror_clamp_to_edge = true;
                        break;
                    case "GL_ARB_texture_stencil8":
                        extensionData.ARB_texture_stencil8 = true;
                        break;
                    case "GL_ARB_vertex_type_10f_11f_11f_rev":
                        extensionData.ARB_vertex_type_10f_11f_11f_rev = true;
                        break;
                    case "GL_ARB_bindless_texture":
                        extensionData.ARB_bindless_texture = true;
                        break;
                    case "GL_ARB_compute_variable_group_size":
                        extensionData.ARB_compute_variable_group_size = true;
                        break;
                    case "GL_ARB_indirect_parameters":
                        extensionData.ARB_indirect_parameters = true;
                        break;
                    case "GL_ARB_seamless_cubemap_per_texture":
                        extensionData.ARB_seamless_cubemap_per_texture = true;
                        break;
                    case "GL_ARB_shader_draw_parameters":
                        extensionData.ARB_shader_draw_parameters = true;
                        break;
                    case "GL_ARB_shader_group_vote":
                        extensionData.ARB_shader_group_vote = true;
                        break;
                    case "GL_ARB_sparse_texture":
                        extensionData.ARB_sparse_texture = true;
                        break;
                    case "GL_ARB_ES3_1_compatibility":
                        extensionData.ARB_ES3_1_compatibility = true;
                        break;
                    case "GL_ARB_clip_control":
                        extensionData.ARB_clip_control = true;
                        break;
                    case "GL_ARB_conditional_render_inverted":
                        extensionData.ARB_conditional_render_inverted = true;
                        break;
                    case "GL_ARB_derivative_control":
                        extensionData.ARB_derivative_control = true;
                        break;
                    case "GL_ARB_direct_state_access":
                        extensionData.ARB_direct_state_access = true;
                        break;
                    case "GL_ARB_get_texture_sub_image":
                        extensionData.ARB_get_texture_sub_image = true;
                        break;
                    case "GL_ARB_shader_texture_image_samples":
                        extensionData.ARB_shader_texture_image_samples = true;
                        break;
                    case "GL_ARB_texture_barrier":
                        extensionData.ARB_texture_barrier = true;
                        break;
                    case "GL_KHR_context_flush_control":
                        extensionData.KHR_context_flush_control = true;
                        break;
                    case "GL_KHR_robust_buffer_access_behavior":
                        extensionData.KHR_robust_buffer_access_behavior = true;
                        break;
                    case "GL_KHR_robustness":
                        extensionData.KHR_robustness = true;
                        break;
                    case "GL_ARB_pipeline_statistics_query":
                        extensionData.ARB_pipeline_statistics_query = true;
                        break;
                    case "GL_ARB_sparse_buffer":
                        extensionData.ARB_sparse_buffer = true;
                        break;
                    case "GL_ARB_transform_feedback_overflow_query":
                        extensionData.ARB_transform_feedback_overflow_query = true;
                        break;
                    // EXT
                    case "GL_EXT_texture_compression_s3tc":
                        extensionData.EXT_texture_compression_s3tc = true;
                        break;
                    case "GL_EXT_texture_compression_latc":
                        extensionData.EXT_texture_compression_latc = true;
                        break;
                    case "GL_EXT_transform_feedback":
                        extensionData.EXT_transform_feedback = true;
                        break;
                    case "GL_EXT_direct_state_access":
                        extensionData.EXT_direct_state_access = true;
                        break;
                    case "GL_EXT_texture_filter_anisotropic":
                        extensionData.EXT_texture_filter_anisotropic = true;
                        break;
                    case "GL_EXT_texture_array":
                        extensionData.EXT_texture_array = true;
                        break;
                    case "GL_EXT_texture_snorm":
                        extensionData.EXT_texture_snorm = true;
                        break;
                    case "GL_EXT_texture_sRGB_decode":
                        extensionData.EXT_texture_sRGB_decode = true;
                        break;
                    case "GL_EXT_framebuffer_multisample_blit_scaled":
                        extensionData.EXT_framebuffer_multisample_blit_scaled = true;
                        break;
                    case "GL_EXT_shader_integer_mix":
                        extensionData.EXT_shader_integer_mix = true;
                        break;
                    case "GL_EXT_polygon_offset_clamp":
                        extensionData.EXT_polygon_offset_clamp = true;
                        break;
                    // NV
                    case "GL_NV_explicit_multisample":
                        extensionData.NV_explicit_multisample = true;
                        break;
                    case "GL_NV_shader_buffer_load":
                        extensionData.NV_shader_buffer_load = true;
                        break;
                    case "GL_NV_vertex_buffer_unified_memory":
                        extensionData.NV_vertex_buffer_unified_memory = true;
                        break;
                    case "GL_NV_shader_buffer_store":
                        extensionData.NV_shader_buffer_store = true;
                        break;
                    case "GL_NV_bindless_multi_draw_indirect":
                        extensionData.NV_bindless_multi_draw_indirect = true;
                        break;
                    case "GL_NV_blend_equation_advanced":
                        extensionData.NV_blend_equation_advanced = true;
                        break;
                    case "GL_NV_deep_texture3D":
                        extensionData.NV_deep_texture3D = true;
                        break;
                    case "GL_NV_shader_thread_group":
                        extensionData.NV_shader_thread_group = true;
                        break;
                    case "GL_NV_shader_thread_shuffle":
                        extensionData.NV_shader_thread_shuffle = true;
                        break;
                    case "GL_NV_shader_atomic_int64":
                        extensionData.NV_shader_atomic_int64 = true;
                        break;
                    case "GL_NV_bindless_multi_draw_indirect_count":
                        extensionData.NV_bindless_multi_draw_indirect_count = true;
                        break;
                    case "GL_NV_uniform_buffer_unified_memory":
                        extensionData.NV_uniform_buffer_unified_memory = true;
                        break;
                    // AMD
                    case "GL_ATI_texture_compression_3dc":
                        extensionData.ATI_texture_compression_3dc = true;
                        break;
                    case "GL_AMD_depth_clamp_separate":
                        extensionData.AMD_depth_clamp_separate = true;
                        break;
                    case "GL_AMD_stencil_operation_extended":
                        extensionData.AMD_stencil_operation_extended = true;
                        break;
                    case "GL_AMD_vertex_shader_viewport_index":
                        extensionData.AMD_vertex_shader_viewport_index = true;
                        break;
                    case "GL_AMD_vertex_shader_layer":
                        extensionData.AMD_vertex_shader_layer = true;
                        break;
                    case "GL_AMD_shader_trinary_minmax":
                        extensionData.AMD_shader_trinary_minmax = true;
                        break;
                    case "GL_AMD_interleaved_elements":
                        extensionData.AMD_interleaved_elements = true;
                        break;
                    case "GL_AMD_shader_atomic_counter_ops":
                        extensionData.AMD_shader_atomic_counter_ops = true;
                        break;
                    case "GL_AMD_shader_stencil_value_export":
                        extensionData.AMD_shader_stencil_value_export = true;
                        break;
                    case "GL_AMD_transform_feedback4":
                        extensionData.AMD_transform_feedback4 = true;
                        break;
                    case "GL_AMD_gpu_shader_int64":
                        extensionData.AMD_gpu_shader_int64 = true;
                        break;
                    case "GL_AMD_gcn_shader":
                        extensionData.AMD_gcn_shader = true;
                        break;
                    // Intel
                    case "GL_INTEL_map_texture":
                        extensionData.INTEL_map_texture = true;
                        break;
                    case "GL_INTEL_fragment_shader_ordering":
                        extensionData.INTEL_fragment_shader_ordering = true;
                        break;
                    case "GL_INTEL_performance_query":
                        extensionData.INTEL_performance_query = true;
                        break;
                }
            }
        }
    }

    private void initDebug(GL gl) {

        debugData = new DebugData();

        gl.glGetIntegerv(GL_CONTEXT_FLAGS, tmp, 0);
        debugData.CONTEXT_FLAGS = tmp[0];
        gl.glGetIntegerv(GL_MAX_DEBUG_GROUP_STACK_DEPTH, tmp, 0);
        debugData.MAX_DEBUG_GROUP_STACK_DEPTH = tmp[0];
        gl.glGetIntegerv(GL_MAX_LABEL_LENGTH, tmp, 0);
        debugData.MAX_LABEL_LENGTH = tmp[0];
        gl.glGetIntegerv(GL_MAX_SERVER_WAIT_TIMEOUT, tmp, 0);
        debugData.MAX_SERVER_WAIT_TIMEOUT = tmp[0];
    }

    private void initLimits(GL gl) {

        limitsData = new LimitsData();

        if (check(4, 3) || extensionData.ARB_compute_shader) {

            GL4 gl4 = (GL4) gl;

            gl4.glGetIntegerv(GL_MAX_COMPUTE_TEXTURE_IMAGE_UNITS, tmp, 0);
            limitsData.MAX_COMPUTE_TEXTURE_IMAGE_UNITS = tmp[0];
            gl4.glGetIntegerv(GL_MAX_COMPUTE_UNIFORM_COMPONENTS, tmp, 0);
            limitsData.MAX_COMPUTE_UNIFORM_COMPONENTS = tmp[0];
            gl4.glGetIntegerv(GL_MAX_COMPUTE_SHARED_MEMORY_SIZE, tmp, 0);
            limitsData.MAX_COMPUTE_SHARED_MEMORY_SIZE = tmp[0];
            gl4.glGetIntegerv(GL_MAX_COMPUTE_WORK_GROUP_INVOCATIONS, tmp, 0);
            limitsData.MAX_COMPUTE_WORK_GROUP_INVOCATIONS = tmp[0];
            gl4.glGetIntegeri_v(GL_MAX_COMPUTE_WORK_GROUP_COUNT, 0, tmp, 0);
            limitsData.MAX_COMPUTE_WORK_GROUP_COUNT = tmp[0];
            gl4.glGetIntegeri_v(GL_MAX_COMPUTE_WORK_GROUP_SIZE, 0, tmp, 0);
            limitsData.MAX_COMPUTE_WORK_GROUP_SIZE = tmp[0];
        }

        if (check(4, 3) || (extensionData.ARB_compute_shader && extensionData.ARB_uniform_buffer_object)) {

            gl.glGetIntegerv(GL_MAX_COMPUTE_UNIFORM_BLOCKS, tmp, 0);
            limitsData.MAX_COMPUTE_UNIFORM_BLOCKS = tmp[0];
            gl.glGetIntegerv(GL_MAX_COMBINED_COMPUTE_UNIFORM_COMPONENTS, tmp, 0);
            limitsData.MAX_COMBINED_COMPUTE_UNIFORM_COMPONENTS = tmp[0];
        }

        if (check(4, 3) || (extensionData.ARB_compute_shader && extensionData.ARB_shader_image_load_store)) {

            gl.glGetIntegerv(GL_MAX_COMPUTE_IMAGE_UNIFORMS, tmp, 0);
            limitsData.MAX_COMPUTE_IMAGE_UNIFORMS = tmp[0];
        }

        if (check(4, 3) || (extensionData.ARB_compute_shader && extensionData.ARB_shader_atomic_counters)) {

            gl.glGetIntegerv(GL_MAX_COMPUTE_ATOMIC_COUNTERS, tmp, 0);
            limitsData.MAX_COMPUTE_ATOMIC_COUNTERS = tmp[0];
            gl.glGetIntegerv(GL_MAX_COMPUTE_ATOMIC_COUNTER_BUFFERS, tmp, 0);
            limitsData.MAX_COMPUTE_ATOMIC_COUNTER_BUFFERS = tmp[0];
        }

        if (check(4, 3) || (extensionData.ARB_compute_shader && extensionData.ARB_shader_storage_buffer_object)) {

            gl.glGetIntegerv(GL_MAX_COMPUTE_SHADER_STORAGE_BLOCKS, tmp, 0);
            limitsData.MAX_COMPUTE_SHADER_STORAGE_BLOCKS = tmp[0];
        }

        if (check(2, 1) || extensionData.ARB_vertex_shader) {

            gl.glGetIntegerv(GL_MAX_VERTEX_ATTRIBS, tmp, 0);
            limitsData.MAX_VERTEX_ATTRIBS = tmp[0];
            gl.glGetIntegerv(GL_MAX_VERTEX_OUTPUT_COMPONENTS, tmp, 0);
            limitsData.MAX_VERTEX_OUTPUT_COMPONENTS = tmp[0];
            gl.glGetIntegerv(GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS, tmp, 0);
            limitsData.MAX_VERTEX_TEXTURE_IMAGE_UNITS = tmp[0];
            gl.glGetIntegerv(GL_MAX_VERTEX_UNIFORM_COMPONENTS, tmp, 0);
            limitsData.MAX_VERTEX_UNIFORM_COMPONENTS = tmp[0];
            gl.glGetIntegerv(GL_MAX_VERTEX_UNIFORM_VECTORS, tmp, 0);
            limitsData.MAX_VERTEX_UNIFORM_VECTORS = tmp[0];
        }

        if (check(3, 2) || (extensionData.ARB_vertex_shader && extensionData.ARB_uniform_buffer_object)) {

            gl.glGetIntegerv(GL_MAX_VERTEX_UNIFORM_BLOCKS, tmp, 0);
            limitsData.MAX_VERTEX_UNIFORM_BLOCKS = tmp[0];
            gl.glGetIntegerv(GL_MAX_COMBINED_VERTEX_UNIFORM_COMPONENTS, tmp, 0);
            limitsData.MAX_COMBINED_VERTEX_UNIFORM_COMPONENTS = tmp[0];
        }

        if (check(4, 2) || (extensionData.ARB_vertex_shader && extensionData.ARB_shader_atomic_counters)) {

            gl.glGetIntegerv(GL_MAX_VERTEX_ATOMIC_COUNTERS, tmp, 0);
            limitsData.MAX_VERTEX_ATOMIC_COUNTERS = tmp[0];
        }

        if (check(4, 3) || (extensionData.ARB_vertex_shader && extensionData.ARB_shader_storage_buffer_object)) {

            gl.glGetIntegerv(GL_MAX_VERTEX_SHADER_STORAGE_BLOCKS, tmp, 0);
            limitsData.MAX_VERTEX_SHADER_STORAGE_BLOCKS = tmp[0];
        }

        if (check(4, 0) || extensionData.ARB_tessellation_shader) {

            gl.glGetIntegerv(GL_MAX_TESS_CONTROL_INPUT_COMPONENTS, tmp, 0);
            limitsData.MAX_TESS_CONTROL_INPUT_COMPONENTS = tmp[0];
            gl.glGetIntegerv(GL_MAX_TESS_CONTROL_OUTPUT_COMPONENTS, tmp, 0);
            limitsData.MAX_TESS_CONTROL_OUTPUT_COMPONENTS = tmp[0];
            gl.glGetIntegerv(GL_MAX_TESS_CONTROL_TEXTURE_IMAGE_UNITS, tmp, 0);
            limitsData.MAX_TESS_CONTROL_TEXTURE_IMAGE_UNITS = tmp[0];
            gl.glGetIntegerv(GL_MAX_TESS_CONTROL_UNIFORM_COMPONENTS, tmp, 0);
            limitsData.MAX_TESS_CONTROL_UNIFORM_COMPONENTS = tmp[0];

            gl.glGetIntegerv(GL_MAX_TESS_EVALUATION_INPUT_COMPONENTS, tmp, 0);
            limitsData.MAX_TESS_EVALUATION_INPUT_COMPONENTS = tmp[0];
            gl.glGetIntegerv(GL_MAX_TESS_EVALUATION_OUTPUT_COMPONENTS, tmp, 0);
            limitsData.MAX_TESS_EVALUATION_OUTPUT_COMPONENTS = tmp[0];
            gl.glGetIntegerv(GL_MAX_TESS_EVALUATION_TEXTURE_IMAGE_UNITS, tmp, 0);
            limitsData.MAX_TESS_EVALUATION_TEXTURE_IMAGE_UNITS = tmp[0];
            gl.glGetIntegerv(GL_MAX_TESS_EVALUATION_UNIFORM_COMPONENTS, tmp, 0);
            limitsData.MAX_TESS_EVALUATION_UNIFORM_COMPONENTS = tmp[0];
        }

        if (check(4, 0) || (extensionData.ARB_tessellation_shader && extensionData.ARB_uniform_buffer_object)) {

            gl.glGetIntegerv(GL_MAX_TESS_CONTROL_UNIFORM_BLOCKS, tmp, 0);
            limitsData.MAX_TESS_CONTROL_UNIFORM_BLOCKS = tmp[0];
            gl.glGetIntegerv(GL_MAX_COMBINED_TESS_CONTROL_UNIFORM_COMPONENTS, tmp, 0);
            limitsData.MAX_COMBINED_TESS_CONTROL_UNIFORM_COMPONENTS = tmp[0];
        }

        if (check(4, 2) || (extensionData.ARB_tessellation_shader && extensionData.ARB_shader_atomic_counters)) {

            gl.glGetIntegerv(GL_MAX_TESS_CONTROL_ATOMIC_COUNTERS, tmp, 0);
            limitsData.MAX_TESS_CONTROL_ATOMIC_COUNTERS = tmp[0];
        }

        if (check(4, 3) || (extensionData.ARB_tessellation_shader && extensionData.ARB_shader_storage_buffer_object)) {

            gl.glGetIntegerv(GL_MAX_TESS_CONTROL_SHADER_STORAGE_BLOCKS, tmp, 0);
            limitsData.MAX_TESS_CONTROL_SHADER_STORAGE_BLOCKS = tmp[0];
        }

        if (check(4, 0) || (extensionData.ARB_tessellation_shader && extensionData.ARB_uniform_buffer_object)) {

            gl.glGetIntegerv(GL_MAX_TESS_EVALUATION_UNIFORM_BLOCKS, tmp, 0);
            limitsData.MAX_TESS_EVALUATION_UNIFORM_BLOCKS = tmp[0];
            gl.glGetIntegerv(GL_MAX_COMBINED_TESS_EVALUATION_UNIFORM_COMPONENTS, tmp, 0);
            limitsData.MAX_COMBINED_TESS_EVALUATION_UNIFORM_COMPONENTS = tmp[0];
        }

        if (check(4, 2) || (extensionData.ARB_tessellation_shader && extensionData.ARB_shader_atomic_counters)) {

            gl.glGetIntegerv(GL_MAX_TESS_EVALUATION_ATOMIC_COUNTERS, tmp, 0);
            limitsData.MAX_TESS_EVALUATION_ATOMIC_COUNTERS = tmp[0];
        }

        if (check(4, 3) || (extensionData.ARB_tessellation_shader && extensionData.ARB_shader_storage_buffer_object)) {

            gl.glGetIntegerv(GL_MAX_TESS_EVALUATION_SHADER_STORAGE_BLOCKS, tmp, 0);
            limitsData.MAX_TESS_EVALUATION_SHADER_STORAGE_BLOCKS = tmp[0];
        }

        if (check(3, 2) || extensionData.ARB_geometry_shader4) {

            gl.glGetIntegerv(GL_MAX_GEOMETRY_INPUT_COMPONENTS, tmp, 0);
            limitsData.MAX_GEOMETRY_INPUT_COMPONENTS = tmp[0];
            gl.glGetIntegerv(GL_MAX_GEOMETRY_OUTPUT_COMPONENTS, tmp, 0);
            limitsData.MAX_GEOMETRY_OUTPUT_COMPONENTS = tmp[0];
            gl.glGetIntegerv(GL_MAX_GEOMETRY_TEXTURE_IMAGE_UNITS, tmp, 0);
            limitsData.MAX_GEOMETRY_TEXTURE_IMAGE_UNITS = tmp[0];
            gl.glGetIntegerv(GL_MAX_GEOMETRY_UNIFORM_COMPONENTS, tmp, 0);
            limitsData.MAX_GEOMETRY_UNIFORM_COMPONENTS = tmp[0];
        }

        if (check(3, 2) || (extensionData.ARB_geometry_shader4 && extensionData.ARB_uniform_buffer_object)) {

            gl.glGetIntegerv(GL_MAX_GEOMETRY_UNIFORM_BLOCKS, tmp, 0);
            limitsData.MAX_GEOMETRY_UNIFORM_BLOCKS = tmp[0];
            gl.glGetIntegerv(GL_MAX_COMBINED_GEOMETRY_UNIFORM_COMPONENTS, tmp, 0);
            limitsData.MAX_COMBINED_GEOMETRY_UNIFORM_COMPONENTS = tmp[0];
        }

        if (check(4, 0) || (extensionData.ARB_geometry_shader4 && extensionData.ARB_transform_feedback3)) {

            gl.glGetIntegerv(GL_MAX_VERTEX_STREAMS, tmp, 0);
            limitsData.MAX_VERTEX_STREAMS = tmp[0];
        }
        if (check(4, 2) || (extensionData.ARB_geometry_shader4 && extensionData.ARB_shader_atomic_counters)) {

            gl.glGetIntegerv(GL_MAX_GEOMETRY_ATOMIC_COUNTERS, tmp, 0);
            limitsData.MAX_GEOMETRY_ATOMIC_COUNTERS = tmp[0];
        }

        if (check(4, 3) || (extensionData.ARB_geometry_shader4 && extensionData.ARB_shader_storage_buffer_object)) {

            gl.glGetIntegerv(GL_MAX_GEOMETRY_SHADER_STORAGE_BLOCKS, tmp, 0);
            limitsData.MAX_GEOMETRY_SHADER_STORAGE_BLOCKS = tmp[0];
        }

        if (check(2, 1)) {

            gl.glGetIntegerv(GL_MAX_DRAW_BUFFERS, tmp, 0);
            limitsData.MAX_DRAW_BUFFERS = tmp[0];
        }

        if (check(2, 1) || extensionData.ARB_fragment_shader) {

            gl.glGetIntegerv(GL_MAX_FRAGMENT_INPUT_COMPONENTS, tmp, 0);
            limitsData.MAX_FRAGMENT_INPUT_COMPONENTS = tmp[0];
            gl.glGetIntegerv(GL_MAX_FRAGMENT_UNIFORM_COMPONENTS, tmp, 0);
            limitsData.MAX_FRAGMENT_UNIFORM_COMPONENTS = tmp[0];
            gl.glGetIntegerv(GL_MAX_FRAGMENT_UNIFORM_VECTORS, tmp, 0);
            limitsData.MAX_FRAGMENT_UNIFORM_VECTORS = tmp[0];
        }

        if (check(3, 2) || (extensionData.ARB_fragment_shader && extensionData.ARB_uniform_buffer_object)) {

            gl.glGetIntegerv(GL_MAX_FRAGMENT_UNIFORM_BLOCKS, tmp, 0);
            limitsData.MAX_FRAGMENT_UNIFORM_BLOCKS = tmp[0];
            gl.glGetIntegerv(GL_MAX_COMBINED_FRAGMENT_UNIFORM_COMPONENTS, tmp, 0);
            limitsData.MAX_COMBINED_FRAGMENT_UNIFORM_COMPONENTS = tmp[0];
        }

        if (check(3, 3) || (extensionData.ARB_blend_func_extended)) {

            gl.glGetIntegerv(GL_MAX_DUAL_SOURCE_DRAW_BUFFERS, tmp, 0);
            limitsData.MAX_DUAL_SOURCE_DRAW_BUFFERS = tmp[0];
        }

        if (check(4, 2) || (extensionData.ARB_fragment_shader && extensionData.ARB_shader_atomic_counters)) {

            gl.glGetIntegerv(GL_MAX_FRAGMENT_ATOMIC_COUNTERS, tmp, 0);
            limitsData.MAX_FRAGMENT_ATOMIC_COUNTERS = tmp[0];
        }

        if (check(4, 3) || (extensionData.ARB_fragment_shader && extensionData.ARB_shader_storage_buffer_object)) {

            gl.glGetIntegerv(GL_MAX_FRAGMENT_SHADER_STORAGE_BLOCKS, tmp, 0);
            limitsData.MAX_FRAGMENT_SHADER_STORAGE_BLOCKS = tmp[0];
        }

        if (check(3, 0) || (extensionData.ARB_framebuffer_object)) {

            gl.glGetIntegerv(GL_MAX_COLOR_ATTACHMENTS, tmp, 0);
            limitsData.MAX_COLOR_ATTACHMENTS = tmp[0];
        }

        if (check(4, 3) || (extensionData.ARB_framebuffer_no_attachments)) {

            gl.glGetIntegerv(GL_MAX_FRAMEBUFFER_HEIGHT, tmp, 0);
            limitsData.MAX_FRAMEBUFFER_HEIGHT = tmp[0];
            gl.glGetIntegerv(GL_MAX_FRAMEBUFFER_WIDTH, tmp, 0);
            limitsData.MAX_FRAMEBUFFER_WIDTH = tmp[0];
            gl.glGetIntegerv(GL_MAX_FRAMEBUFFER_LAYERS, tmp, 0);
            limitsData.MAX_FRAMEBUFFER_LAYERS = tmp[0];
            gl.glGetIntegerv(GL_MAX_FRAMEBUFFER_SAMPLES, tmp, 0);
            limitsData.MAX_FRAMEBUFFER_SAMPLES = tmp[0];
        }

        if (check(4, 0) || (extensionData.ARB_transform_feedback3)) {

            gl.glGetIntegerv(GL_MAX_TRANSFORM_FEEDBACK_BUFFERS, tmp, 0);
            limitsData.MAX_TRANSFORM_FEEDBACK_BUFFERS = tmp[0];
        }

        if (check(4, 2) || (extensionData.ARB_map_buffer_alignment)) {

            gl.glGetIntegerv(GL_MIN_MAP_BUFFER_ALIGNMENT, tmp, 0);
            limitsData.MIN_MAP_BUFFER_ALIGNMENT = tmp[0];
        }

        if (extensionData.NV_deep_texture3D) {
            gl.glGetIntegerv(GL_MAX_DEEP_3D_TEXTURE_WIDTH_HEIGHT_NV, tmp, 0);
            limitsData.MAX_DEEP_3D_TEXTURE_WIDTH_HEIGHT_NV = tmp[0];
            gl.glGetIntegerv(GL_MAX_DEEP_3D_TEXTURE_DEPTH_NV, tmp, 0);
            limitsData.MAX_DEEP_3D_TEXTURE_DEPTH_NV = tmp[0];
        }

        if (check(2, 1)) {

            gl.glGetIntegerv(GL_MAX_TEXTURE_IMAGE_UNITS, tmp, 0);
            limitsData.MAX_TEXTURE_IMAGE_UNITS = tmp[0];
            gl.glGetIntegerv(GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS, tmp, 0);
            limitsData.MAX_COMBINED_TEXTURE_IMAGE_UNITS = tmp[0];
            gl.glGetIntegerv(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, tmp, 0);
            limitsData.MAX_TEXTURE_MAX_ANISOTROPY_EXT = tmp[0];
        }

        if (check(3, 0) || (extensionData.ARB_texture_buffer_object)) {

            gl.glGetIntegerv(GL_MAX_TEXTURE_BUFFER_SIZE, tmp, 0);
            limitsData.MAX_TEXTURE_BUFFER_SIZE = tmp[0];
        }

        if (check(3, 2) || (extensionData.ARB_texture_multisample)) {

            gl.glGetIntegerv(GL_MAX_SAMPLE_MASK_WORDS, tmp, 0);
            limitsData.MAX_SAMPLE_MASK_WORDS = tmp[0];
            gl.glGetIntegerv(GL_MAX_COLOR_TEXTURE_SAMPLES, tmp, 0);
            limitsData.MAX_COLOR_TEXTURE_SAMPLES = tmp[0];
            gl.glGetIntegerv(GL_MAX_DEPTH_TEXTURE_SAMPLES, tmp, 0);
            limitsData.MAX_DEPTH_TEXTURE_SAMPLES = tmp[0];
            gl.glGetIntegerv(GL_MAX_INTEGER_SAMPLES, tmp, 0);
            limitsData.MAX_INTEGER_SAMPLES = tmp[0];
        }

        if (check(3, 3) || (extensionData.ARB_texture_rectangle)) {

            gl.glGetIntegerv(GL_MAX_RECTANGLE_TEXTURE_SIZE, tmp, 0);
            limitsData.MAX_RECTANGLE_TEXTURE_SIZE = tmp[0];
        }

        if (check(2, 2) && versionData.PROFILE == COMPATIBILITY) {

            gl.glGetIntegerv(GL_MAX_VARYING_COMPONENTS, tmp, 0);
            limitsData.MAX_VARYING_COMPONENTS = tmp[0];
            gl.glGetIntegerv(GL_MAX_VARYING_VECTORS, tmp, 0);
            limitsData.MAX_VARYING_VECTORS = tmp[0];
            gl.glGetIntegerv(GL_MAX_VARYING_FLOATS, tmp, 0);
            limitsData.MAX_VARYING_FLOATS = tmp[0];
        }

        if (check(3, 2)) {

            gl.glGetIntegerv(GL_MAX_COMBINED_UNIFORM_BLOCKS, tmp, 0);
            limitsData.MAX_COMBINED_UNIFORM_BLOCKS = tmp[0];
            gl.glGetIntegerv(GL_MAX_UNIFORM_BUFFER_BINDINGS, tmp, 0);
            limitsData.MAX_UNIFORM_BUFFER_BINDINGS = tmp[0];
            gl.glGetIntegerv(GL_MAX_UNIFORM_BLOCK_SIZE, tmp, 0);
            limitsData.MAX_UNIFORM_BLOCK_SIZE = tmp[0];
            gl.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, tmp, 0);
            limitsData.UNIFORM_BUFFER_OFFSET_ALIGNMENT = tmp[0];
        }

        if (check(4, 0)) {

            gl.glGetIntegerv(GL_MAX_PATCH_VERTICES, tmp, 0);
            limitsData.MAX_PATCH_VERTICES = tmp[0];
            gl.glGetIntegerv(GL_MAX_TESS_GEN_LEVEL, tmp, 0);
            limitsData.MAX_TESS_GEN_LEVEL = tmp[0];
            gl.glGetIntegerv(GL_MAX_SUBROUTINES, tmp, 0);
            limitsData.MAX_SUBROUTINES = tmp[0];
            gl.glGetIntegerv(GL_MAX_SUBROUTINE_UNIFORM_LOCATIONS, tmp, 0);
            limitsData.MAX_SUBROUTINE_UNIFORM_LOCATIONS = tmp[0];
            gl.glGetIntegerv(GL_MAX_COMBINED_ATOMIC_COUNTERS, tmp, 0);
            limitsData.MAX_COMBINED_ATOMIC_COUNTERS = tmp[0];
            gl.glGetIntegerv(GL_MAX_COMBINED_SHADER_STORAGE_BLOCKS, tmp, 0);
            limitsData.MAX_COMBINED_SHADER_STORAGE_BLOCKS = tmp[0];
            gl.glGetIntegerv(GL_MAX_PROGRAM_TEXEL_OFFSET, tmp, 0);
            limitsData.MAX_PROGRAM_TEXEL_OFFSET = tmp[0];
            gl.glGetIntegerv(GL_MIN_PROGRAM_TEXEL_OFFSET, tmp, 0);
            limitsData.MIN_PROGRAM_TEXEL_OFFSET = tmp[0];
        }

        if (check(4, 1)) {

            gl.glGetIntegerv(GL_NUM_PROGRAM_BINARY_FORMATS, tmp, 0);
            limitsData.NUM_PROGRAM_BINARY_FORMATS = tmp[0];
            gl.glGetIntegerv(GL_NUM_SHADER_BINARY_FORMATS, tmp, 0);
            limitsData.NUM_SHADER_BINARY_FORMATS = tmp[0];
            gl.glGetIntegerv(GL_PROGRAM_BINARY_FORMATS, tmp, 0);
            limitsData.PROGRAM_BINARY_FORMATS = tmp[0];
        }

        if (check(4, 2)) {

            gl.glGetIntegerv(GL_MAX_COMBINED_SHADER_OUTPUT_RESOURCES, tmp, 0);
            limitsData.MAX_COMBINED_SHADER_OUTPUT_RESOURCES = tmp[0];
            gl.glGetIntegerv(GL_MAX_SHADER_STORAGE_BUFFER_BINDINGS, tmp, 0);
            limitsData.MAX_SHADER_STORAGE_BUFFER_BINDINGS = tmp[0];
            gl.glGetIntegerv(GL_MAX_SHADER_STORAGE_BLOCK_SIZE, tmp, 0);
            limitsData.MAX_SHADER_STORAGE_BLOCK_SIZE = tmp[0];
            gl.glGetIntegerv(GL_MAX_COMBINED_SHADER_OUTPUT_RESOURCES, tmp, 0);
            limitsData.MAX_COMBINED_SHADER_OUTPUT_RESOURCES = tmp[0];
            gl.glGetIntegerv(GL_SHADER_STORAGE_BUFFER_OFFSET_ALIGNMENT, tmp, 0);
            limitsData.SHADER_STORAGE_BUFFER_OFFSET_ALIGNMENT = tmp[0];
        }

        if (check(4, 3)) {

            gl.glGetIntegerv(GL_MAX_COMBINED_SHADER_OUTPUT_RESOURCES, tmp, 0);
            limitsData.MAX_COMBINED_SHADER_OUTPUT_RESOURCES = tmp[0];
        }

        if (check(4, 3) || extensionData.ARB_explicit_uniform_location) {

            gl.glGetIntegerv(GL_MAX_UNIFORM_LOCATIONS, tmp, 0);
            limitsData.MAX_UNIFORM_LOCATIONS = tmp[0];
        }
    }

    private void initValues(GL gl) {

        valuesData = new ValuesData();

        if (check(2, 1)) {

            gl.glGetIntegerv(GL_MAX_ELEMENTS_INDICES, tmp, 0);
            valuesData.MAX_ELEMENTS_INDICES = tmp[0];
            gl.glGetIntegerv(GL_MAX_ELEMENTS_VERTICES, tmp, 0);
            valuesData.MAX_ELEMENTS_VERTICES = tmp[0];
        }

        if (check(4, 3) || (extensionData.ARB_vertex_attrib_binding)) {

            gl.glGetIntegerv(GL_MAX_VERTEX_ATTRIB_RELATIVE_OFFSET, tmp, 0);
            valuesData.MAX_VERTEX_ATTRIB_RELATIVE_OFFSET = tmp[0];
            gl.glGetIntegerv(GL_MAX_VERTEX_ATTRIB_BINDINGS, tmp, 0);
            valuesData.MAX_VERTEX_ATTRIB_BINDINGS = tmp[0];
        }

        if (check(4, 3) || (extensionData.ARB_ES3_compatibility)) {

            GL4 gl4 = (GL4) gl;

            gl4.glGetInteger64v(GL_MAX_ELEMENT_INDEX, tmp64, 0);
            valuesData.MAX_ELEMENT_INDEX = tmp64[0];
        }

        if (versionData.PROFILE == COMPATIBILITY) {

            gl.glGetFloatv(GL_POINT_SIZE_MIN, tmpF, 0);
            valuesData.POINT_SIZE_MIN = tmpF[0];
            gl.glGetFloatv(GL_POINT_SIZE_MAX, tmpF, 0);
            valuesData.POINT_SIZE_MAX = tmpF[0];
        }

        gl.glGetFloatv(GL_POINT_SIZE_RANGE, tmpF, 0);
        valuesData.POINT_SIZE_RANGE[0] = tmpF[0];
        gl.glGetFloatv(GL_POINT_SIZE_GRANULARITY, tmpF, 0);
        valuesData.POINT_SIZE_GRANULARITY = tmpF[0];
        gl.glGetFloatv(GL_ALIASED_LINE_WIDTH_RANGE, tmpF, 0);
        valuesData.ALIASED_LINE_WIDTH_RANGE[0] = tmpF[0];
        gl.glGetFloatv(GL_SMOOTH_LINE_WIDTH_RANGE, tmpF, 0);
        valuesData.SMOOTH_LINE_WIDTH_RANGE[0] = tmpF[0];
        gl.glGetFloatv(GL_SMOOTH_LINE_WIDTH_GRANULARITY, tmpF, 0);
        valuesData.SMOOTH_LINE_WIDTH_GRANULARITY = tmpF[0];

        if (check(2, 1)) {

            gl.glGetIntegerv(GL_SUBPIXEL_BITS, tmp, 0);
            valuesData.SUBPIXEL_BITS = tmp[0];
            gl.glGetFloatv(GL_MAX_VIEWPORT_DIMS, tmpF, 0);
            valuesData.MAX_VIEWPORT_DIMS = tmpF[0];
        }

        if (check(3, 0)) {

            gl.glGetIntegerv(GL_MAX_CLIP_DISTANCES, tmp, 0);
            valuesData.MAX_CLIP_DISTANCES = tmp[0];
        }

        if (check(4, 5) || (extensionData.ARB_cull_distance)) {

            gl.glGetIntegerv(GL_MAX_CULL_DISTANCES, tmp, 0);
            valuesData.MAX_CULL_DISTANCES = tmp[0];
            gl.glGetIntegerv(GL_MAX_COMBINED_CLIP_AND_CULL_DISTANCES, tmp, 0);
            valuesData.MAX_COMBINED_CLIP_AND_CULL_DISTANCES = tmp[0];
        }

        if (check(4, 1) || (extensionData.ARB_viewport_array)) {

            gl.glGetIntegerv(GL_MAX_VIEWPORTS, tmp, 0);
            valuesData.MAX_VIEWPORTS = tmp[0];
            gl.glGetIntegerv(GL_VIEWPORT_SUBPIXEL_BITS, tmp, 0);
            valuesData.VIEWPORT_SUBPIXEL_BITS = tmp[0];
            gl.glGetFloatv(GL_VIEWPORT_BOUNDS_RANGE, tmpF, 0);
            valuesData.VIEWPORT_BOUNDS_RANGE[0] = tmpF[0];
            gl.glGetIntegerv(GL_LAYER_PROVOKING_VERTEX, tmp, 0);
            valuesData.LAYER_PROVOKING_VERTEX = tmp[0];
            gl.glGetIntegerv(GL_VIEWPORT_INDEX_PROVOKING_VERTEX, tmp, 0);
            valuesData.VIEWPORT_INDEX_PROVOKING_VERTEX = tmp[0];
        }

        if (check(4, 1) || (extensionData.ARB_ES2_compatibility)) {

            gl.glGetIntegerv(GL_IMPLEMENTATION_COLOR_READ_FORMAT, tmp, 0);
            valuesData.IMPLEMENTATION_COLOR_READ_FORMAT = tmp[0];
            gl.glGetIntegerv(GL_IMPLEMENTATION_COLOR_READ_TYPE, tmp, 0);
            valuesData.IMPLEMENTATION_COLOR_READ_TYPE = tmp[0];
        }

        if (check(2, 1)) {

            gl.glGetIntegerv(GL_MAX_TEXTURE_LOD_BIAS, tmp, 0);
            valuesData.MAX_TEXTURE_LOD_BIAS = tmp[0];
            gl.glGetIntegerv(GL_MAX_TEXTURE_SIZE, tmp, 0);
            valuesData.MAX_TEXTURE_SIZE = tmp[0];
            gl.glGetIntegerv(GL_MAX_3D_TEXTURE_SIZE, tmp, 0);
            valuesData.MAX_3D_TEXTURE_SIZE = tmp[0];
            gl.glGetIntegerv(GL_MAX_CUBE_MAP_TEXTURE_SIZE, tmp, 0);
            valuesData.MAX_CUBE_MAP_TEXTURE_SIZE = tmp[0];
        }

        if (check(3, 0) || (extensionData.EXT_texture_array)) {

            gl.glGetIntegerv(GL_MAX_ARRAY_TEXTURE_LAYERS, tmp, 0);
            valuesData.MAX_ARRAY_TEXTURE_LAYERS = tmp[0];
        }

        if (check(4, 3) || (extensionData.ARB_texture_buffer_object)) {

            gl.glGetIntegerv(GL_TEXTURE_BUFFER_OFFSET_ALIGNMENT, tmp, 0);
            valuesData.TEXTURE_BUFFER_OFFSET_ALIGNMENT = tmp[0];
        }
    }
    
    private void initFormats(GL gl) {
        
        gl.glGetIntegerv(GL_NUM_COMPRESSED_TEXTURE_FORMATS, tmp, 0);

	int[] COMPRESSED_TEXTURE_FORMATS = new int[tmp[0]];
	gl.glGetIntegerv(GL_COMPRESSED_TEXTURE_FORMATS, COMPRESSED_TEXTURE_FORMATS, 0);

	for(int i = 0; i < tmp[0]; ++i)
	{
		switch(COMPRESSED_TEXTURE_FORMATS[i])
		{
		case GL_COMPRESSED_RGB_S3TC_DXT1_EXT:
			formatsData.COMPRESSED_RGB_S3TC_DXT1_EXT = true;
			break;
		case GL_COMPRESSED_RGBA_S3TC_DXT1_EXT:
			formatsData.COMPRESSED_RGBA_S3TC_DXT1_EXT = true;
			break;
		case GL_COMPRESSED_RGBA_S3TC_DXT3_EXT:
			formatsData.COMPRESSED_RGBA_S3TC_DXT3_EXT = true;
			break;
		case GL_COMPRESSED_RGBA_S3TC_DXT5_EXT:
			formatsData.COMPRESSED_RGBA_S3TC_DXT5_EXT = true;
			break;
		case GL_COMPRESSED_SRGB_S3TC_DXT1:
			formatsData.COMPRESSED_SRGB_S3TC_DXT1_EXT = true;
			break;
		case GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT1:
			formatsData.COMPRESSED_SRGB_ALPHA_S3TC_DXT1_EXT = true;
			break;
		case GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT3:
			formatsData.COMPRESSED_SRGB_ALPHA_S3TC_DXT3_EXT = true;
			break;
		case GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT5:
			formatsData.COMPRESSED_SRGB_ALPHA_S3TC_DXT5_EXT = true;
			break;

		case GL_COMPRESSED_RED_RGTC1:
			formatsData.COMPRESSED_RED_RGTC1 = true;
			break;
		case GL_COMPRESSED_SIGNED_RED_RGTC1:
			formatsData.COMPRESSED_SIGNED_RED_RGTC1 = true;
			break;
		case GL_COMPRESSED_RG_RGTC2:
			formatsData.COMPRESSED_RG_RGTC2 = true;
			break;
		case GL_COMPRESSED_SIGNED_RG_RGTC2:
			formatsData.COMPRESSED_SIGNED_RG_RGTC2 = true;
			break;
		case GL_COMPRESSED_RGBA_BPTC_UNORM:
			formatsData.COMPRESSED_RGBA_BPTC_UNORM = true;
			break;
		case GL_COMPRESSED_SRGB_ALPHA_BPTC_UNORM:
			formatsData.COMPRESSED_SRGB_ALPHA_BPTC_UNORM = true;
			break;
		case GL_COMPRESSED_RGB_BPTC_SIGNED_FLOAT:
			formatsData.COMPRESSED_RGB_BPTC_SIGNED_FLOAT = true;
			break;
		case GL_COMPRESSED_RGB_BPTC_UNSIGNED_FLOAT:
			formatsData.COMPRESSED_RGB_BPTC_UNSIGNED_FLOAT = true;
			break;
		case GL_COMPRESSED_R11_EAC:
			formatsData.COMPRESSED_R11_EAC = true;
			break;
		case GL_COMPRESSED_SIGNED_R11_EAC:
			formatsData.COMPRESSED_SIGNED_R11_EAC = true;
			break;
		case GL_COMPRESSED_RG11_EAC:
			formatsData.COMPRESSED_RG11_EAC = true;
			break;
		case GL_COMPRESSED_SIGNED_RG11_EAC:
			formatsData.COMPRESSED_SIGNED_RG11_EAC = true;
			break;
		case GL_COMPRESSED_RGB8_ETC2:
			formatsData.COMPRESSED_RGB8_ETC2 = true;
			break;
		case GL_COMPRESSED_SRGB8_ETC2:
			formatsData.COMPRESSED_SRGB8_ETC2 = true;
			break;
		case GL_COMPRESSED_RGB8_PUNCHTHROUGH_ALPHA1_ETC2:
			formatsData.COMPRESSED_RGB8_PUNCHTHROUGH_ALPHA1_ETC2 = true;
			break;
		case GL_COMPRESSED_SRGB8_PUNCHTHROUGH_ALPHA1_ETC2:
			formatsData.COMPRESSED_SRGB8_PUNCHTHROUGH_ALPHA1_ETC2 = true;
			break;
		case GL_COMPRESSED_RGBA8_ETC2_EAC:
			formatsData.COMPRESSED_RGBA8_ETC2_EAC = true;
			break;
		case GL_COMPRESSED_SRGB8_ALPHA8_ETC2_EAC:
			formatsData.COMPRESSED_SRGB8_ALPHA8_ETC2_EAC = true;
			break;

		case GL_COMPRESSED_RGBA_ASTC_4x4_KHR:
			formatsData.COMPRESSED_RGBA_ASTC_4x4_KHR = true;
			break;
		case GL_COMPRESSED_RGBA_ASTC_5x4_KHR:
			formatsData.COMPRESSED_RGBA_ASTC_5x4_KHR = true;
			break;
		case GL_COMPRESSED_RGBA_ASTC_5x5_KHR:
			formatsData.COMPRESSED_RGBA_ASTC_5x5_KHR = true;
			break;
		case GL_COMPRESSED_RGBA_ASTC_6x5_KHR:
			formatsData.COMPRESSED_RGBA_ASTC_6x5_KHR = true;
			break;
		case GL_COMPRESSED_RGBA_ASTC_6x6_KHR:
			formatsData.COMPRESSED_RGBA_ASTC_6x6_KHR = true;
			break;
		case GL_COMPRESSED_RGBA_ASTC_8x5_KHR:
			formatsData.COMPRESSED_RGBA_ASTC_8x5_KHR = true;
			break;
		case GL_COMPRESSED_RGBA_ASTC_8x6_KHR:
			formatsData.COMPRESSED_RGBA_ASTC_8x6_KHR = true;
			break;
		case GL_COMPRESSED_RGBA_ASTC_8x8_KHR:
			formatsData.COMPRESSED_RGBA_ASTC_8x8_KHR = true;
			break;
		case GL_COMPRESSED_RGBA_ASTC_10x5_KHR:
			formatsData.COMPRESSED_RGBA_ASTC_10x5_KHR = true;
			break;
		case GL_COMPRESSED_RGBA_ASTC_10x6_KHR:
			formatsData.COMPRESSED_RGBA_ASTC_10x6_KHR = true;
			break;
		case GL_COMPRESSED_RGBA_ASTC_10x8_KHR:
			formatsData.COMPRESSED_RGBA_ASTC_10x8_KHR = true;
			break;
		case GL_COMPRESSED_RGBA_ASTC_10x10_KHR:
			formatsData.COMPRESSED_RGBA_ASTC_10x10_KHR = true;
			break;
		case GL_COMPRESSED_RGBA_ASTC_12x10_KHR:
			formatsData.COMPRESSED_RGBA_ASTC_12x10_KHR = true;
			break;
		case GL_COMPRESSED_RGBA_ASTC_12x12_KHR:
			formatsData.COMPRESSED_RGBA_ASTC_12x12_KHR = true;
			break;

		case GL_COMPRESSED_SRGB8_ALPHA8_ASTC_4x4_KHR:
			formatsData.COMPRESSED_SRGB8_ALPHA8_ASTC_4x4_KHR = true;
			break;
		case GL_COMPRESSED_SRGB8_ALPHA8_ASTC_5x4_KHR:
			formatsData.COMPRESSED_SRGB8_ALPHA8_ASTC_5x4_KHR = true;
			break;
		case GL_COMPRESSED_SRGB8_ALPHA8_ASTC_5x5_KHR:
			formatsData.COMPRESSED_SRGB8_ALPHA8_ASTC_5x5_KHR = true;
			break;
		case GL_COMPRESSED_SRGB8_ALPHA8_ASTC_6x5_KHR:
			formatsData.COMPRESSED_SRGB8_ALPHA8_ASTC_6x5_KHR = true;
			break;
		case GL_COMPRESSED_SRGB8_ALPHA8_ASTC_6x6_KHR:
			formatsData.COMPRESSED_SRGB8_ALPHA8_ASTC_6x6_KHR = true;
			break;
		case GL_COMPRESSED_SRGB8_ALPHA8_ASTC_8x5_KHR:
			formatsData.COMPRESSED_SRGB8_ALPHA8_ASTC_8x5_KHR = true;
			break;
		case GL_COMPRESSED_SRGB8_ALPHA8_ASTC_8x6_KHR:
			formatsData.COMPRESSED_SRGB8_ALPHA8_ASTC_8x6_KHR = true;
			break;
		case GL_COMPRESSED_SRGB8_ALPHA8_ASTC_8x8_KHR:
			formatsData.COMPRESSED_SRGB8_ALPHA8_ASTC_8x8_KHR = true;
			break;
		case GL_COMPRESSED_SRGB8_ALPHA8_ASTC_10x5_KHR:
			formatsData.COMPRESSED_SRGB8_ALPHA8_ASTC_10x5_KHR = true;
			break;
		case GL_COMPRESSED_SRGB8_ALPHA8_ASTC_10x6_KHR:
			formatsData.COMPRESSED_SRGB8_ALPHA8_ASTC_10x6_KHR = true;
			break;
		case GL_COMPRESSED_SRGB8_ALPHA8_ASTC_10x8_KHR:
			formatsData.COMPRESSED_SRGB8_ALPHA8_ASTC_10x8_KHR = true;
			break;
		case GL_COMPRESSED_SRGB8_ALPHA8_ASTC_10x10_KHR:
			formatsData.COMPRESSED_SRGB8_ALPHA8_ASTC_10x10_KHR = true;
			break;
		case GL_COMPRESSED_SRGB8_ALPHA8_ASTC_12x10_KHR:
			formatsData.COMPRESSED_SRGB8_ALPHA8_ASTC_12x10_KHR = true;
			break;
		case GL_COMPRESSED_SRGB8_ALPHA8_ASTC_12x12_KHR:
			formatsData.COMPRESSED_SRGB8_ALPHA8_ASTC_12x12_KHR = true;
			break;

		case GL_COMPRESSED_LUMINANCE_LATC1_EXT:
			formatsData.COMPRESSED_LUMINANCE_LATC1_EXT = true;
			break;
		case GL_COMPRESSED_SIGNED_LUMINANCE_LATC1_EXT:
			formatsData.COMPRESSED_SIGNED_LUMINANCE_LATC1_EXT = true;
			break;
		case GL_COMPRESSED_LUMINANCE_ALPHA_LATC2_EXT:
			formatsData.COMPRESSED_LUMINANCE_ALPHA_LATC2_EXT = true;
			break;
		case GL_COMPRESSED_SIGNED_LUMINANCE_ALPHA_LATC2_EXT:
			formatsData.COMPRESSED_SIGNED_LUMINANCE_ALPHA_LATC2_EXT = true;
			break;
//		case GL_COMPRESSED_LUMINANCE_ALPHA_3DC_ATI:
		case GL_COMPRESSED_LUMINANCE_ALPHA:
			formatsData.COMPRESSED_LUMINANCE_ALPHA_3DC_ATI = true;
			break;
//		case GL_COMPRESSED_RGB_FXT1:
//			formatsData.COMPRESSED_RGB_FXT1_3DFX = true;
//			break;
//		case GL_COMPRESSED_RGBA_FXT1_3DFX:
//			FormatsData.COMPRESSED_RGBA_FXT1_3DFX = true;
//			break;
//		case GL_PALETTE4_RGB8_OES:
//			FormatsData.PALETTE4_RGB8_OES = true;
//			break;
//		case GL_PALETTE4_RGBA8_OES:
//			FormatsData.PALETTE4_RGBA8_OES = true;
//			break;
//		case GL_PALETTE4_R5_G6_B5_OES:
//			FormatsData.PALETTE4_R5_G6_B5_OES = true;
//			break;
//		case GL_PALETTE4_RGBA4_OES:
//			FormatsData.PALETTE4_RGBA4_OES = true;
//			break;
//		case GL_PALETTE4_RGB5_A1_OES:
//			FormatsData.PALETTE4_RGB5_A1_OES = true;
//			break;
//		case GL_PALETTE8_RGB8_OES:
//			FormatsData.PALETTE8_RGB8_OES = true;
//			break;
//		case GL_PALETTE8_RGBA8_OES:
//			FormatsData.PALETTE8_RGBA8_OES = true;
//			break;
//		case GL_PALETTE8_R5_G6_B5_OES:
//			FormatsData.PALETTE8_R5_G6_B5_OES = true;
//			break;
//		case GL_PALETTE8_RGBA4_OES:
//			FormatsData.PALETTE8_RGBA4_OES = true;
//			break;
//		case GL_PALETTE8_RGB5_A1_OES:
//			FormatsData.PALETTE8_RGB5_A1_OES = true;
//			break;
//		case GL_ETC1_RGB8_OES:
//			FormatsData.ETC1_RGB8_OES = true;
//			break;

		default:
			// Unknown formats
			break;
		}
	}
    }
}
