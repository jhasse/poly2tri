# OpenGL Declarations 
#
# Copyright (C) 2006,2007,2008 PySoy Group
#
#  This program is free software; you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation; either version 3 of the License, or
#  (at your option) any later version.
#
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  You should have received a copy of the GNU General Public License
#  along with this program; if not, see http://www.gnu.org/licenses

cdef extern from "GL/glew.h" :
  ctypedef enum:
    # Boolean Values
    GL_FALSE
    GL_TRUE

    # Data types
    GL_BYTE
    GL_UNSIGNED_BYTE
    GL_SHORT
    GL_UNSIGNED_SHORT
    GL_INT
    GL_UNSIGNED_INT
    GL_FLOAT
    GL_2_BYTES
    GL_3_BYTES
    GL_4_BYTES
    GL_DOUBLE

    # Primitives
    GL_POINTS
    GL_LINES
    GL_LINE_LOOP
    GL_LINE_STRIP
    GL_TRIANGLES
    GL_TRIANGLE_STRIP
    GL_TRIANGLE_FAN
    GL_QUADS
    GL_QUAD_STRIP
    GL_POLYGON

    # Vertex Arrays
    GL_VERTEX_ARRAY
    GL_NORMAL_ARRAY
    GL_COLOR_ARRAY
    GL_INDEX_ARRAY
    GL_TEXTURE_COORD_ARRAY
    GL_EDGE_FLAG_ARRAY
    GL_VERTEX_ARRAY_SIZE
    GL_VERTEX_ARRAY_TYPE
    GL_VERTEX_ARRAY_STRIDE
    GL_NORMAL_ARRAY_TYPE
    GL_NORMAL_ARRAY_STRIDE
    GL_COLOR_ARRAY_SIZE
    GL_COLOR_ARRAY_TYPE
    GL_COLOR_ARRAY_STRIDE
    GL_INDEX_ARRAY_TYPE
    GL_INDEX_ARRAY_STRIDE
    GL_TEXTURE_COORD_ARRAY_SIZE
    GL_TEXTURE_COORD_ARRAY_TYPE
    GL_TEXTURE_COORD_ARRAY_STRIDE
    GL_EDGE_FLAG_ARRAY_STRIDE
    GL_VERTEX_ARRAY_POINTER
    GL_NORMAL_ARRAY_POINTER
    GL_COLOR_ARRAY_POINTER
    GL_INDEX_ARRAY_POINTER
    GL_TEXTURE_COORD_ARRAY_POINTER
    GL_EDGE_FLAG_ARRAY_POINTER

    # Vertex Buffer Objects
    GL_BUFFER_SIZE_ARB
    GL_BUFFER_USAGE_ARB
    GL_ARRAY_BUFFER_ARB
    GL_ELEMENT_ARRAY_BUFFER_ARB
    GL_ARRAY_BUFFER_BINDING_ARB
    GL_ELEMENT_ARRAY_BUFFER_BINDING_ARB
    GL_VERTEX_ARRAY_BUFFER_BINDING_ARB
    GL_NORMAL_ARRAY_BUFFER_BINDING_ARB
    GL_COLOR_ARRAY_BUFFER_BINDING_ARB
    GL_INDEX_ARRAY_BUFFER_BINDING_ARB
    GL_TEXTURE_COORD_ARRAY_BUFFER_BINDING_ARB
    GL_EDGE_FLAG_ARRAY_BUFFER_BINDING_ARB
    GL_SECONDARY_COLOR_ARRAY_BUFFER_BINDING_ARB
    GL_FOG_COORDINATE_ARRAY_BUFFER_BINDING_ARB
    GL_WEIGHT_ARRAY_BUFFER_BINDING_ARB
    GL_VERTEX_ATTRIB_ARRAY_BUFFER_BINDING_ARB
    GL_READ_ONLY_ARB
    GL_WRITE_ONLY_ARB
    GL_READ_WRITE_ARB
    GL_BUFFER_ACCESS_ARB
    GL_BUFFER_MAPPED_ARB
    GL_BUFFER_MAP_POINTER_ARB
    GL_STREAM_DRAW_ARB
    GL_STREAM_READ_ARB
    GL_STREAM_COPY_ARB
    GL_STATIC_DRAW_ARB
    GL_STATIC_READ_ARB
    GL_STATIC_COPY_ARB
    GL_DYNAMIC_DRAW_ARB
    GL_DYNAMIC_READ_ARB
    GL_DYNAMIC_COPY_ARB

    # Matrix Mode
    GL_MATRIX_MODE
    GL_MODELVIEW
    GL_PROJECTION
    GL_TEXTURE

    # Points
    GL_POINT_SMOOTH
    GL_POINT_SIZE
    GL_POINT_SIZE_GRANULARITY
    GL_POINT_SIZE_RANGE

    # Lines
    GL_LINE_SMOOTH
    GL_LINE_STIPPLE
    GL_LINE_STIPPLE_PATTERN
    GL_LINE_STIPPLE_REPEAT
    GL_LINE_WIDTH
    GL_LINE_WIDTH_GRANULARITY
    GL_LINE_WIDTH_RANGE

    # Polygons
    GL_POINT
    GL_LINE
    GL_FILL
    GL_CW
    GL_CCW
    GL_FRONT
    GL_BACK
    GL_POLYGON_MODE
    GL_POLYGON_SMOOTH
    GL_POLYGON_STIPPLE
    GL_EDGE_FLAG
    GL_CULL_FACE
    GL_CULL_FACE_MODE
    GL_FRONT_FACE
    GL_POLYGON_OFFSET_FACTOR
    GL_POLYGON_OFFSET_UNITS
    GL_POLYGON_OFFSET_POINT
    GL_POLYGON_OFFSET_LINE
    GL_POLYGON_OFFSET_FILL

    # Display Lists
    GL_COMPILE
    GL_COMPILE_AND_EXECUTE
    GL_LIST_BASE
    GL_LIST_INDEX
    GL_LIST_MODE
    
    # Depth buffer
    GL_NEVER
    GL_LESS
    GL_EQUAL
    GL_LEQUAL
    GL_GREATER
    GL_NOTEQUAL
    GL_GEQUAL
    GL_ALWAYS
    GL_DEPTH_TEST
    GL_DEPTH_BITS
    GL_DEPTH_CLEAR_VALUE
    GL_DEPTH_FUNC
    GL_DEPTH_RANGE
    GL_DEPTH_WRITEMASK
    GL_DEPTH_COMPONENT

    # Lighting
    GL_LIGHTING
    GL_LIGHT0
    GL_LIGHT1
    GL_LIGHT2
    GL_LIGHT3
    GL_LIGHT4
    GL_LIGHT5
    GL_LIGHT6
    GL_LIGHT7
    GL_SPOT_EXPONENT
    GL_SPOT_CUTOFF
    GL_CONSTANT_ATTENUATION
    GL_LINEAR_ATTENUATION
    GL_QUADRATIC_ATTENUATION
    GL_AMBIENT
    GL_DIFFUSE
    GL_SPECULAR
    GL_SHININESS
    GL_EMISSION
    GL_POSITION
    GL_SPOT_DIRECTION
    GL_AMBIENT_AND_DIFFUSE
    GL_COLOR_INDEXES
    GL_LIGHT_MODEL_TWO_SIDE
    GL_LIGHT_MODEL_LOCAL_VIEWER
    GL_LIGHT_MODEL_AMBIENT
    GL_FRONT_AND_BACK
    GL_SHADE_MODEL
    GL_FLAT
    GL_SMOOTH
    GL_COLOR_MATERIAL
    GL_COLOR_MATERIAL_FACE
    GL_COLOR_MATERIAL_PARAMETER
    GL_NORMALIZE

    # User clipping planes
    GL_CLIP_PLANE0
    GL_CLIP_PLANE1
    GL_CLIP_PLANE2
    GL_CLIP_PLANE3
    GL_CLIP_PLANE4
    GL_CLIP_PLANE5

    # Accumulation buffer
    GL_ACCUM_RED_BITS
    GL_ACCUM_GREEN_BITS
    GL_ACCUM_BLUE_BITS
    GL_ACCUM_ALPHA_BITS
    GL_ACCUM_CLEAR_VALUE
    GL_ACCUM
    GL_ADD
    GL_LOAD
    GL_MULT
    GL_RETURN

    # Alpha testing
    GL_ALPHA_TEST
    GL_ALPHA_TEST_REF
    GL_ALPHA_TEST_FUNC

    # Blending
    GL_BLEND
    GL_BLEND_SRC
    GL_BLEND_DST
    GL_ZERO
    GL_ONE
    GL_SRC_COLOR
    GL_ONE_MINUS_SRC_COLOR
    GL_SRC_ALPHA
    GL_ONE_MINUS_SRC_ALPHA
    GL_DST_ALPHA
    GL_ONE_MINUS_DST_ALPHA
    GL_DST_COLOR
    GL_ONE_MINUS_DST_COLOR
    GL_SRC_ALPHA_SATURATE

    # Render Mode
    GL_FEEDBACK
    GL_RENDER
    GL_SELECT

    # Feedback
    GL_2D
    GL_3D
    GL_3D_COLOR
    GL_3D_COLOR_TEXTURE
    GL_4D_COLOR_TEXTURE
    GL_POINT_TOKEN
    GL_LINE_TOKEN
    GL_LINE_RESET_TOKEN
    GL_POLYGON_TOKEN
    GL_BITMAP_TOKEN
    GL_DRAW_PIXEL_TOKEN
    GL_COPY_PIXEL_TOKEN
    GL_PASS_THROUGH_TOKEN
    GL_FEEDBACK_BUFFER_POINTER
    GL_FEEDBACK_BUFFER_SIZE
    GL_FEEDBACK_BUFFER_TYPE

    # Selection
    GL_SELECTION_BUFFER_POINTER
    GL_SELECTION_BUFFER_SIZE

    # Fog
    GL_FOG
    GL_FOG_MODE
    GL_FOG_DENSITY
    GL_FOG_COLOR
    GL_FOG_INDEX
    GL_FOG_START
    GL_FOG_END
    GL_LINEAR
    GL_EXP
    GL_EXP2

    # Logic Ops
    GL_LOGIC_OP
    GL_INDEX_LOGIC_OP
    GL_COLOR_LOGIC_OP
    GL_LOGIC_OP_MODE
    GL_CLEAR
    GL_SET
    GL_COPY
    GL_COPY_INVERTED
    GL_NOOP
    GL_INVERT
    GL_AND
    GL_NAND
    GL_OR
    GL_NOR
    GL_XOR
    GL_EQUIV
    GL_AND_REVERSE
    GL_AND_INVERTED
    GL_OR_REVERSE
    GL_OR_INVERTED

    # Stencil
    GL_STENCIL_BITS
    GL_STENCIL_TEST
    GL_STENCIL_CLEAR_VALUE
    GL_STENCIL_FUNC
    GL_STENCIL_VALUE_MASK
    GL_STENCIL_FAIL
    GL_STENCIL_PASS_DEPTH_FAIL
    GL_STENCIL_PASS_DEPTH_PASS
    GL_STENCIL_REF
    GL_STENCIL_WRITEMASK
    GL_STENCIL_INDEX
    GL_KEEP
    GL_REPLACE
    GL_INCR
    GL_DECR

    # Buffers, Pixel Drawing/Reading
    GL_NONE
    GL_LEFT
    GL_RIGHT
    #GL_FRONT
    #GL_BACK
    #GL_FRONT_AND_BACK
    GL_FRONT_LEFT
    GL_FRONT_RIGHT
    GL_BACK_LEFT
    GL_BACK_RIGHT
    GL_AUX0
    GL_AUX1
    GL_AUX2
    GL_AUX3
    GL_COLOR_INDEX
    GL_RED
    GL_GREEN
    GL_BLUE
    GL_ALPHA
    GL_LUMINANCE
    GL_LUMINANCE_ALPHA
    GL_ALPHA_BITS
    GL_RED_BITS
    GL_GREEN_BITS
    GL_BLUE_BITS
    GL_INDEX_BITS
    GL_SUBPIXEL_BITS
    GL_AUX_BUFFERS
    GL_READ_BUFFER
    GL_DRAW_BUFFER
    GL_DOUBLEBUFFER
    GL_STEREO
    GL_BITMAP
    GL_COLOR
    GL_DEPTH
    GL_STENCIL
    GL_DITHER
    GL_RGB
    GL_RGBA

    # Implementation limits
    GL_MAX_LIST_NESTING
    GL_MAX_EVAL_ORDER
    GL_MAX_LIGHTS
    GL_MAX_CLIP_PLANES
    GL_MAX_TEXTURE_SIZE
    GL_MAX_PIXEL_MAP_TABLE
    GL_MAX_ATTRIB_STACK_DEPTH
    GL_MAX_MODELVIEW_STACK_DEPTH
    GL_MAX_NAME_STACK_DEPTH
    GL_MAX_PROJECTION_STACK_DEPTH
    GL_MAX_TEXTURE_STACK_DEPTH
    GL_MAX_VIEWPORT_DIMS
    GL_MAX_CLIENT_ATTRIB_STACK_DEPTH

    # Gets
    GL_ATTRIB_STACK_DEPTH
    GL_CLIENT_ATTRIB_STACK_DEPTH
    GL_COLOR_CLEAR_VALUE
    GL_COLOR_WRITEMASK
    GL_CURRENT_INDEX
    GL_CURRENT_COLOR
    GL_CURRENT_NORMAL
    GL_CURRENT_RASTER_COLOR
    GL_CURRENT_RASTER_DISTANCE
    GL_CURRENT_RASTER_INDEX
    GL_CURRENT_RASTER_POSITION
    GL_CURRENT_RASTER_TEXTURE_COORDS
    GL_CURRENT_RASTER_POSITION_VALID
    GL_CURRENT_TEXTURE_COORDS
    GL_INDEX_CLEAR_VALUE
    GL_INDEX_MODE
    GL_INDEX_WRITEMASK
    GL_MODELVIEW_MATRIX
    GL_MODELVIEW_STACK_DEPTH
    GL_NAME_STACK_DEPTH
    GL_PROJECTION_MATRIX
    GL_PROJECTION_STACK_DEPTH
    GL_RENDER_MODE
    GL_RGBA_MODE
    GL_TEXTURE_MATRIX
    GL_TEXTURE_STACK_DEPTH
    GL_VIEWPORT

    # Evaluators
    GL_AUTO_NORMAL
    GL_MAP1_COLOR_4
    GL_MAP1_INDEX
    GL_MAP1_NORMAL
    GL_MAP1_TEXTURE_COORD_1
    GL_MAP1_TEXTURE_COORD_2
    GL_MAP1_TEXTURE_COORD_3
    GL_MAP1_TEXTURE_COORD_4
    GL_MAP1_VERTEX_3
    GL_MAP1_VERTEX_4
    GL_MAP2_COLOR_4
    GL_MAP2_INDEX
    GL_MAP2_NORMAL
    GL_MAP2_TEXTURE_COORD_1
    GL_MAP2_TEXTURE_COORD_2
    GL_MAP2_TEXTURE_COORD_3
    GL_MAP2_TEXTURE_COORD_4
    GL_MAP2_VERTEX_3
    GL_MAP2_VERTEX_4
    GL_MAP1_GRID_DOMAIN
    GL_MAP1_GRID_SEGMENTS
    GL_MAP2_GRID_DOMAIN
    GL_MAP2_GRID_SEGMENTS
    GL_COEFF
    GL_ORDER
    GL_DOMAIN

    # Hints
    GL_PERSPECTIVE_CORRECTION_HINT
    GL_POINT_SMOOTH_HINT
    GL_LINE_SMOOTH_HINT
    GL_POLYGON_SMOOTH_HINT
    GL_FOG_HINT
    GL_DONT_CARE
    GL_FASTEST
    GL_NICEST

    # Scissor box
    GL_SCISSOR_BOX
    GL_SCISSOR_TEST

    # Pixel Mode / Transfer
    GL_MAP_COLOR
    GL_MAP_STENCIL
    GL_INDEX_SHIFT
    GL_INDEX_OFFSET
    GL_RED_SCALE
    GL_RED_BIAS
    GL_GREEN_SCALE
    GL_GREEN_BIAS
    GL_BLUE_SCALE
    GL_BLUE_BIAS
    GL_ALPHA_SCALE
    GL_ALPHA_BIAS
    GL_DEPTH_SCALE
    GL_DEPTH_BIAS
    GL_PIXEL_MAP_S_TO_S_SIZE
    GL_PIXEL_MAP_I_TO_I_SIZE
    GL_PIXEL_MAP_I_TO_R_SIZE
    GL_PIXEL_MAP_I_TO_G_SIZE
    GL_PIXEL_MAP_I_TO_B_SIZE
    GL_PIXEL_MAP_I_TO_A_SIZE
    GL_PIXEL_MAP_R_TO_R_SIZE
    GL_PIXEL_MAP_G_TO_G_SIZE
    GL_PIXEL_MAP_B_TO_B_SIZE
    GL_PIXEL_MAP_A_TO_A_SIZE
    GL_PIXEL_MAP_S_TO_S
    GL_PIXEL_MAP_I_TO_I
    GL_PIXEL_MAP_I_TO_R
    GL_PIXEL_MAP_I_TO_G
    GL_PIXEL_MAP_I_TO_B
    GL_PIXEL_MAP_I_TO_A
    GL_PIXEL_MAP_R_TO_R
    GL_PIXEL_MAP_G_TO_G
    GL_PIXEL_MAP_B_TO_B
    GL_PIXEL_MAP_A_TO_A
    GL_PACK_ALIGNMENT
    GL_PACK_LSB_FIRST
    GL_PACK_ROW_LENGTH
    GL_PACK_SKIP_PIXELS
    GL_PACK_SKIP_ROWS
    GL_PACK_SWAP_BYTES
    GL_UNPACK_ALIGNMENT
    GL_UNPACK_LSB_FIRST
    GL_UNPACK_ROW_LENGTH
    GL_UNPACK_SKIP_PIXELS
    GL_UNPACK_SKIP_ROWS
    GL_UNPACK_SWAP_BYTES
    GL_ZOOM_X
    GL_ZOOM_Y

    # Texture mapping
    GL_TEXTURE_ENV
    GL_TEXTURE_ENV_MODE
    GL_TEXTURE_1D
    GL_TEXTURE_2D
    GL_TEXTURE_WRAP_S
    GL_TEXTURE_WRAP_T
    GL_TEXTURE_MAG_FILTER
    GL_TEXTURE_MIN_FILTER
    GL_TEXTURE_ENV_COLOR
    GL_TEXTURE_GEN_S
    GL_TEXTURE_GEN_T
    GL_TEXTURE_GEN_MODE
    GL_TEXTURE_BORDER_COLOR
    GL_TEXTURE_WIDTH
    GL_TEXTURE_HEIGHT
    GL_TEXTURE_BORDER
    GL_TEXTURE_COMPONENTS
    GL_TEXTURE_RED_SIZE
    GL_TEXTURE_GREEN_SIZE
    GL_TEXTURE_BLUE_SIZE
    GL_TEXTURE_ALPHA_SIZE
    GL_TEXTURE_LUMINANCE_SIZE
    GL_TEXTURE_INTENSITY_SIZE
    GL_NEAREST_MIPMAP_NEAREST
    GL_NEAREST_MIPMAP_LINEAR
    GL_LINEAR_MIPMAP_NEAREST
    GL_LINEAR_MIPMAP_LINEAR
    GL_OBJECT_LINEAR
    GL_OBJECT_PLANE
    GL_EYE_LINEAR
    GL_EYE_PLANE
    GL_SPHERE_MAP
    GL_DECAL
    GL_MODULATE
    GL_NEAREST
    GL_REPEAT
    GL_CLAMP
    GL_S
    GL_T
    GL_R
    GL_Q
    GL_TEXTURE_GEN_R
    GL_TEXTURE_GEN_Q

    # Utility
    GL_VENDOR
    GL_RENDERER
    GL_VERSION
    GL_EXTENSIONS

    # Errors
    GL_NO_ERROR
    GL_INVALID_ENUM
    GL_INVALID_VALUE
    GL_INVALID_OPERATION
    GL_STACK_OVERFLOW
    GL_STACK_UNDERFLOW
    GL_OUT_OF_MEMORY

    # glPush/PopAttrib bits
    GL_CURRENT_BIT
    GL_POINT_BIT
    GL_LINE_BIT
    GL_POLYGON_BIT
    GL_POLYGON_STIPPLE_BIT
    GL_PIXEL_MODE_BIT
    GL_LIGHTING_BIT
    GL_FOG_BIT
    GL_DEPTH_BUFFER_BIT
    GL_ACCUM_BUFFER_BIT
    GL_STENCIL_BUFFER_BIT
    GL_VIEWPORT_BIT
    GL_TRANSFORM_BIT
    GL_ENABLE_BIT
    GL_COLOR_BUFFER_BIT
    GL_HINT_BIT
    GL_EVAL_BIT
    GL_LIST_BIT
    GL_TEXTURE_BIT
    GL_SCISSOR_BIT
    GL_ALL_ATTRIB_BITS

    ##### OpenGL 1.1 #####

    GL_PROXY_TEXTURE_1D
    GL_PROXY_TEXTURE_2D
    GL_TEXTURE_PRIORITY
    GL_TEXTURE_RESIDENT
    GL_TEXTURE_BINDING_1D
    GL_TEXTURE_BINDING_2D
    GL_TEXTURE_INTERNAL_FORMAT
    GL_ALPHA4
    GL_ALPHA8
    GL_ALPHA12
    GL_ALPHA16
    GL_LUMINANCE4
    GL_LUMINANCE8
    GL_LUMINANCE12
    GL_LUMINANCE16
    GL_LUMINANCE4_ALPHA4
    GL_LUMINANCE6_ALPHA2
    GL_LUMINANCE8_ALPHA8
    GL_LUMINANCE12_ALPHA4
    GL_LUMINANCE12_ALPHA12
    GL_LUMINANCE16_ALPHA16
    GL_INTENSITY
    GL_INTENSITY4
    GL_INTENSITY8
    GL_INTENSITY12
    GL_INTENSITY16
    GL_R3_G3_B2
    GL_RGB4
    GL_RGB5
    GL_RGB8
    GL_RGB10
    GL_RGB12
    GL_RGB16
    GL_RGBA2
    GL_RGBA4
    GL_RGB5_A1
    GL_RGBA8
    GL_RGB10_A2
    GL_RGBA12
    GL_RGBA16
    GL_CLIENT_PIXEL_STORE_BIT
    GL_CLIENT_VERTEX_ARRAY_BIT
    GL_ALL_CLIENT_ATTRIB_BITS
    GL_CLIENT_ALL_ATTRIB_BITS

    ##### OpenGL 1.2 #####

    GL_RESCALE_NORMAL
    GL_CLAMP_TO_EDGE
    GL_MAX_ELEMENTS_VERTICES
    GL_MAX_ELEMENTS_INDICES
    GL_BGR
    GL_BGRA
    GL_UNSIGNED_BYTE_3_3_2
    GL_UNSIGNED_BYTE_2_3_3_REV
    GL_UNSIGNED_SHORT_5_6_5
    GL_UNSIGNED_SHORT_5_6_5_REV
    GL_UNSIGNED_SHORT_4_4_4_4
    GL_UNSIGNED_SHORT_4_4_4_4_REV
    GL_UNSIGNED_SHORT_5_5_5_1
    GL_UNSIGNED_SHORT_1_5_5_5_REV
    GL_UNSIGNED_INT_8_8_8_8
    GL_UNSIGNED_INT_8_8_8_8_REV
    GL_UNSIGNED_INT_10_10_10_2
    GL_UNSIGNED_INT_2_10_10_10_REV
    GL_LIGHT_MODEL_COLOR_CONTROL
    GL_SINGLE_COLOR
    GL_SEPARATE_SPECULAR_COLOR
    GL_TEXTURE_MIN_LOD
    GL_TEXTURE_MAX_LOD
    GL_TEXTURE_BASE_LEVEL
    GL_TEXTURE_MAX_LEVEL
    GL_SMOOTH_POINT_SIZE_RANGE
    GL_SMOOTH_POINT_SIZE_GRANULARITY
    GL_SMOOTH_LINE_WIDTH_RANGE
    GL_SMOOTH_LINE_WIDTH_GRANULARITY
    GL_ALIASED_POINT_SIZE_RANGE
    GL_ALIASED_LINE_WIDTH_RANGE
    GL_PACK_SKIP_IMAGES
    GL_PACK_IMAGE_HEIGHT
    GL_UNPACK_SKIP_IMAGES
    GL_UNPACK_IMAGE_HEIGHT
    GL_TEXTURE_3D
    GL_PROXY_TEXTURE_3D
    GL_TEXTURE_DEPTH
    GL_TEXTURE_WRAP_R
    GL_MAX_3D_TEXTURE_SIZE
    GL_TEXTURE_BINDING_3D

    # GL_ARB_imaging
    GL_CONSTANT_COLOR
    GL_ONE_MINUS_CONSTANT_COLOR
    GL_CONSTANT_ALPHA
    GL_ONE_MINUS_CONSTANT_ALPHA
    GL_COLOR_TABLE
    GL_POST_CONVOLUTION_COLOR_TABLE
    GL_POST_COLOR_MATRIX_COLOR_TABLE
    GL_PROXY_COLOR_TABLE
    GL_PROXY_POST_CONVOLUTION_COLOR_TABLE
    GL_PROXY_POST_COLOR_MATRIX_COLOR_TABLE
    GL_COLOR_TABLE_SCALE
    GL_COLOR_TABLE_BIAS
    GL_COLOR_TABLE_FORMAT
    GL_COLOR_TABLE_WIDTH
    GL_COLOR_TABLE_RED_SIZE
    GL_COLOR_TABLE_GREEN_SIZE
    GL_COLOR_TABLE_BLUE_SIZE
    GL_COLOR_TABLE_ALPHA_SIZE
    GL_COLOR_TABLE_LUMINANCE_SIZE
    GL_COLOR_TABLE_INTENSITY_SIZE
    GL_CONVOLUTION_1D
    GL_CONVOLUTION_2D
    GL_SEPARABLE_2D
    GL_CONVOLUTION_BORDER_MODE
    GL_CONVOLUTION_FILTER_SCALE
    GL_CONVOLUTION_FILTER_BIAS
    GL_REDUCE
    GL_CONVOLUTION_FORMAT
    GL_CONVOLUTION_WIDTH
    GL_CONVOLUTION_HEIGHT
    GL_MAX_CONVOLUTION_WIDTH
    GL_MAX_CONVOLUTION_HEIGHT
    GL_POST_CONVOLUTION_RED_SCALE
    GL_POST_CONVOLUTION_GREEN_SCALE
    GL_POST_CONVOLUTION_BLUE_SCALE
    GL_POST_CONVOLUTION_ALPHA_SCALE
    GL_POST_CONVOLUTION_RED_BIAS
    GL_POST_CONVOLUTION_GREEN_BIAS
    GL_POST_CONVOLUTION_BLUE_BIAS
    GL_POST_CONVOLUTION_ALPHA_BIAS
    GL_CONSTANT_BORDER
    GL_REPLICATE_BORDER
    GL_CONVOLUTION_BORDER_COLOR
    GL_COLOR_MATRIX
    GL_COLOR_MATRIX_STACK_DEPTH
    GL_MAX_COLOR_MATRIX_STACK_DEPTH
    GL_POST_COLOR_MATRIX_RED_SCALE
    GL_POST_COLOR_MATRIX_GREEN_SCALE
    GL_POST_COLOR_MATRIX_BLUE_SCALE
    GL_POST_COLOR_MATRIX_ALPHA_SCALE
    GL_POST_COLOR_MATRIX_RED_BIAS
    GL_POST_COLOR_MATRIX_GREEN_BIAS
    GL_POST_COLOR_MATRIX_BLUE_BIAS
    GL_POST_COLOR_MATRIX_ALPHA_BIAS
    GL_HISTOGRAM
    GL_PROXY_HISTOGRAM
    GL_HISTOGRAM_WIDTH
    GL_HISTOGRAM_FORMAT
    GL_HISTOGRAM_RED_SIZE
    GL_HISTOGRAM_GREEN_SIZE
    GL_HISTOGRAM_BLUE_SIZE
    GL_HISTOGRAM_ALPHA_SIZE
    GL_HISTOGRAM_LUMINANCE_SIZE
    GL_HISTOGRAM_SINK
    GL_MINMAX
    GL_MINMAX_FORMAT
    GL_MINMAX_SINK
    GL_TABLE_TOO_LARGE
    GL_BLEND_EQUATION
    GL_MIN
    GL_MAX
    GL_FUNC_ADD
    GL_FUNC_SUBTRACT
    GL_FUNC_REVERSE_SUBTRACT
    GL_BLEND_COLOR

    ##### OpenGL 1.3 #####

    # multitexture
    GL_TEXTURE0
    GL_TEXTURE1
    GL_TEXTURE2
    GL_TEXTURE3
    GL_TEXTURE4
    GL_TEXTURE5
    GL_TEXTURE6
    GL_TEXTURE7
    GL_TEXTURE8
    GL_TEXTURE9
    GL_TEXTURE10
    GL_TEXTURE11
    GL_TEXTURE12
    GL_TEXTURE13
    GL_TEXTURE14
    GL_TEXTURE15
    GL_TEXTURE16
    GL_TEXTURE17
    GL_TEXTURE18
    GL_TEXTURE19
    GL_TEXTURE20
    GL_TEXTURE21
    GL_TEXTURE22
    GL_TEXTURE23
    GL_TEXTURE24
    GL_TEXTURE25
    GL_TEXTURE26
    GL_TEXTURE27
    GL_TEXTURE28
    GL_TEXTURE29
    GL_TEXTURE30
    GL_TEXTURE31
    GL_ACTIVE_TEXTURE
    GL_CLIENT_ACTIVE_TEXTURE
    GL_MAX_TEXTURE_UNITS

    # texture_cube_map
    GL_NORMAL_MAP
    GL_REFLECTION_MAP
    GL_TEXTURE_CUBE_MAP
    GL_TEXTURE_BINDING_CUBE_MAP
    GL_TEXTURE_CUBE_MAP_POSITIVE_X
    GL_TEXTURE_CUBE_MAP_NEGATIVE_X
    GL_TEXTURE_CUBE_MAP_POSITIVE_Y
    GL_TEXTURE_CUBE_MAP_NEGATIVE_Y
    GL_TEXTURE_CUBE_MAP_POSITIVE_Z
    GL_TEXTURE_CUBE_MAP_NEGATIVE_Z
    GL_PROXY_TEXTURE_CUBE_MAP
    GL_MAX_CUBE_MAP_TEXTURE_SIZE
    
    # texture_compression
    GL_COMPRESSED_ALPHA
    GL_COMPRESSED_LUMINANCE
    GL_COMPRESSED_LUMINANCE_ALPHA
    GL_COMPRESSED_INTENSITY
    GL_COMPRESSED_RGB
    GL_COMPRESSED_RGBA
    GL_TEXTURE_COMPRESSION_HINT
    GL_TEXTURE_COMPRESSED_IMAGE_SIZE
    GL_TEXTURE_COMPRESSED
    GL_NUM_COMPRESSED_TEXTURE_FORMATS
    GL_COMPRESSED_TEXTURE_FORMATS

    # multisample
    GL_MULTISAMPLE
    GL_SAMPLE_ALPHA_TO_COVERAGE
    GL_SAMPLE_ALPHA_TO_ONE
    GL_SAMPLE_COVERAGE
    GL_SAMPLE_BUFFERS
    GL_SAMPLES
    GL_SAMPLE_COVERAGE_VALUE
    GL_SAMPLE_COVERAGE_INVERT
    GL_MULTISAMPLE_BIT

    # transpose_matrix
    GL_TRANSPOSE_MODELVIEW_MATRIX
    GL_TRANSPOSE_PROJECTION_MATRIX
    GL_TRANSPOSE_TEXTURE_MATRIX
    GL_TRANSPOSE_COLOR_MATRIX

    # texture_env_combine
    GL_COMBINE
    GL_COMBINE_RGB
    GL_COMBINE_ALPHA
    GL_SOURCE0_RGB
    GL_SOURCE1_RGB
    GL_SOURCE2_RGB
    GL_SOURCE0_ALPHA
    GL_SOURCE1_ALPHA
    GL_SOURCE2_ALPHA
    GL_OPERAND0_RGB
    GL_OPERAND1_RGB
    GL_OPERAND2_RGB
    GL_OPERAND0_ALPHA
    GL_OPERAND1_ALPHA
    GL_OPERAND2_ALPHA
    GL_RGB_SCALE
    GL_ADD_SIGNED
    GL_INTERPOLATE
    GL_SUBTRACT
    GL_CONSTANT
    GL_PRIMARY_COLOR
    GL_PREVIOUS

    # texture_env_dot3
    GL_DOT3_RGB
    GL_DOT3_RGBA

    # texture_border_clamp
    GL_CLAMP_TO_BORDER


    # GL_ARB_multitexture (ARB extension 1 and OpenGL 1.2.1)
    GL_TEXTURE0_ARB
    GL_TEXTURE1_ARB
    GL_TEXTURE2_ARB
    GL_TEXTURE3_ARB
    GL_TEXTURE4_ARB
    GL_TEXTURE5_ARB
    GL_TEXTURE6_ARB
    GL_TEXTURE7_ARB
    GL_TEXTURE8_ARB
    GL_TEXTURE9_ARB
    GL_TEXTURE10_ARB
    GL_TEXTURE11_ARB
    GL_TEXTURE12_ARB
    GL_TEXTURE13_ARB
    GL_TEXTURE14_ARB
    GL_TEXTURE15_ARB
    GL_TEXTURE16_ARB
    GL_TEXTURE17_ARB
    GL_TEXTURE18_ARB
    GL_TEXTURE19_ARB
    GL_TEXTURE20_ARB
    GL_TEXTURE21_ARB
    GL_TEXTURE22_ARB
    GL_TEXTURE23_ARB
    GL_TEXTURE24_ARB
    GL_TEXTURE25_ARB
    GL_TEXTURE26_ARB
    GL_TEXTURE27_ARB
    GL_TEXTURE28_ARB
    GL_TEXTURE29_ARB
    GL_TEXTURE30_ARB
    GL_TEXTURE31_ARB
    GL_ACTIVE_TEXTURE_ARB
    GL_CLIENT_ACTIVE_TEXTURE_ARB
    GL_MAX_TEXTURE_UNITS_ARB

    # GLU quadratic flags
    GLU_SMOOTH
    GLU_FLAT
    GLU_NONE

    # GLEW response flags
    GLEW_OK
    GLEW_NO_ERROR
    GLEW_ERROR_NO_GL_VERSION
    GLEW_ERROR_GL_VERSION_10_ONLY
    GLEW_ERROR_GLX_VERSION_11_ONLY

    # GLEW rendering flags
    GLEW_EXT_vertex_array
    GLEW_ARB_vertex_buffer_object

  # Data types
  ctypedef unsigned int GLenum
  ctypedef unsigned char GLboolean
  ctypedef unsigned int GLbitfield
  ctypedef void GLvoid
  ctypedef signed char GLbyte
  ctypedef short GLshort
  ctypedef int GLint
  ctypedef unsigned char GLubyte
  ctypedef unsigned short GLushort
  ctypedef unsigned int GLuint
  ctypedef int GLsizei
  ctypedef float GLfloat
  ctypedef float GLclampf
  ctypedef double GLdouble
  ctypedef double GLclampd
  ctypedef char GLchar
  ctypedef unsigned int GLhandleARB
  ctypedef long GLintptrARB
  ctypedef long GLsizeiptrARB
  ctypedef void GLUquadricObj


  # Miscellaneous
  cdef void       glClearIndex         ( GLfloat c )
  cdef void       glClearColor         ( GLclampf, GLclampf, GLclampf, 
                                         GLclampf )
  cdef void       glClear              ( GLbitfield )
  cdef void       glIndexMask          ( GLuint )
  cdef void       glColorMask          ( GLboolean, GLboolean, GLboolean, 
                                         GLboolean )
  cdef void       glAlphaFunc          ( GLenum, GLclampf )
  cdef void       glBlendFunc          ( GLenum, GLenum )
  cdef void       glLogicOp            ( GLenum )
  cdef void       glCullFace           ( GLenum )
  cdef void       glFrontFace          ( GLenum )
  cdef void       glPointSize          ( GLfloat )
  cdef void       glLineWidth          ( GLfloat )
  cdef void       glLineStipple        ( GLint, GLushort )
  cdef void       glPolygonMode        ( GLenum, GLenum )
  cdef void       glPolygonOffset      ( GLfloat, GLfloat )
  cdef void       glPolygonStipple     ( GLubyte* )
  cdef void       glGetPolygonStipple  ( GLubyte* )
  cdef void       glEdgeFlag           ( GLboolean )
  cdef void       glEdgeFlagv          ( GLboolean* )
  cdef void       glScissor            ( GLint, GLint, GLsizei, GLsizei )
  cdef void       glClipPlane          ( GLenum, GLdouble* )
  cdef void       glGetClipPlane       ( GLenum plane, GLdouble *equation )
  cdef void       glDrawBuffer         ( GLenum )
  cdef void       glReadBuffer         ( GLenum )
  cdef void       glEnable             ( GLenum )
  cdef void       glDisable            ( GLenum )
  cdef GLboolean  glIsEnabled          ( GLenum cap )
  cdef void       glEnableClientState  ( GLenum )
  cdef void       glDisableClientState ( GLenum )
  cdef void       glGetBooleanv        ( GLenum, GLboolean* )
  cdef void       glGetDoublev         ( GLenum, GLdouble* )
  cdef void       glGetFloatv          ( GLenum, GLfloat* )
  cdef void       glGetIntegerv        ( GLenum, GLint* )
  cdef void       glPushAttrib         ( GLbitfield )
  cdef void       glPopAttrib          ( )
  cdef void       glPushClientAttrib   ( GLbitfield )  # OpenGL 1.1 
  cdef void       glPopClientAttrib    ( )             # OpenGL 1.1 
  cdef GLint      glRenderMode         ( GLenum )
  cdef GLenum     glGetError           ( )
  cdef GLubyte   *glGetString          ( GLenum )
  cdef void       glFinish             ( )
  cdef void       glFlush              ( )
  cdef void       glHint               ( GLenum, GLenum )

  # Depth Buffer
  cdef void glClearDepth         ( GLclampd )
  cdef void glDepthFunc          ( GLenum )

  # Transformations
  cdef void glMatrixMode( GLenum )
  cdef void glOrtho( GLdouble left, GLdouble right, \
                     GLdouble bottom, GLdouble top, \
					 GLdouble near_val, GLdouble far_val )
  cdef void glViewport( GLint, GLint, GLsizei, GLsizei height )
  cdef void glPushMatrix()
  cdef void glPopMatrix()
  cdef void glLoadIdentity()
  cdef void glLoadMatrixf( GLfloat* )
  cdef void glMultMatrixf( GLfloat* )
  cdef void glScalef( GLfloat, GLfloat, GLfloat )
  cdef void glTranslatef( GLfloat, GLfloat, GLfloat )
    
  # Legacy Functions
  cdef void glBegin( GLenum )
  cdef void glEnd()
  cdef void glVertex2f( GLfloat, GLfloat y )
  cdef void glVertex3f( GLfloat, GLfloat, GLfloat z )
  cdef void glNormal3f( GLfloat, GLfloat, GLfloat z )
  cdef void glColor3f(  GLfloat r, GLfloat g, GLfloat b )
  cdef void glColor3ub( GLubyte r, GLubyte g, GLubyte b )
  cdef void glColor4ub( GLubyte r, GLubyte g, GLubyte b, GLubyte a )

  # Vertex Array Functions
  cdef void       glVertexPointer           ( GLint, GLenum, GLsizei, GLvoid* )
  cdef void       glNormalPointer           (        GLenum, GLsizei, GLvoid* )
  cdef void       glColorPointer            ( GLint, GLenum, GLsizei, GLvoid* ) 
  cdef void       glTexCoordPointer         ( GLint, GLenum, GLsizei, GLvoid* )
  cdef void       glDrawArrays              ( GLenum, GLint, GLsizei )
  cdef void       glDrawElements            ( GLenum, GLsizei, GLenum, GLvoid* )

  # VBO Functions
  cdef void       glBindBufferARB           ( GLenum, GLuint )
  cdef void       glDeleteBuffersARB        ( GLsizei, GLuint* )
  cdef void       glGenBuffersARB           ( GLsizei, GLuint* )
  cdef void       glBufferDataARB           ( GLenum, GLsizeiptrARB, GLvoid*, 
                                              GLenum )
  cdef void       glBufferSubDataARB        ( GLenum, GLintptrARB, 
                                              GLsizeiptrARB, GLvoid* )
  cdef void       glGetBufferSubDataARB     ( GLenum, GLintptrARB, 
                                              GLsizeiptrARB, GLvoid* )
  cdef void      *glMapBufferARB            ( GLenum, GLenum )
  cdef void       glGetBufferParameterivARB ( GLenum, GLenum, GLint* )
  cdef void       glGetBufferPointervARB    ( GLenum, GLenum, GLvoid** )
  cdef GLboolean  glIsBufferARB        ( GLuint )
  cdef GLboolean  glUnmapBufferARB     ( GLenum )

  # Lighting
  cdef void  glShadeModel    ( GLenum )
  cdef void  glLightf        ( GLenum, GLenum, GLfloat )
  cdef void  glLighti        ( GLenum, GLenum, GLint )
  cdef void  glLightfv       ( GLenum, GLenum, GLfloat* )
  cdef void  glLightiv       ( GLenum, GLenum, GLint* )
  cdef void  glGetLightfv    ( GLenum, GLenum, GLfloat* )
  cdef void  glGetLightiv    ( GLenum, GLenum, GLint* )
  cdef void  glLightModelf   ( GLenum, GLfloat )
  cdef void  glLightModeli   ( GLenum, GLint )
  cdef void  glLightModelfv  ( GLenum, GLfloat* )
  cdef void  glLightModeliv  ( GLenum, GLint* )
  cdef void  glMaterialf     ( GLenum, GLenum, GLfloat )
  cdef void  glMateriali     ( GLenum, GLenum, GLint )
  cdef void  glMaterialfv    ( GLenum, GLenum, GLfloat* )
  cdef void  glMaterialiv    ( GLenum, GLenum, GLint* )
  cdef void  glGetMaterialfv ( GLenum, GLenum, GLfloat* )
  cdef void  glGetMaterialiv ( GLenum, GLenum, GLint* )
  cdef void  glColorMaterial ( GLenum, GLenum )

  # Texture Mapping
  cdef void glTexParameterf  ( GLenum, GLenum, GLfloat )
  cdef void glTexParameteri  ( GLenum, GLenum, GLint )
  cdef void glTexImage1D     ( GLenum, GLint, GLint, GLsizei, GLint, GLenum, 
                               GLenum, GLvoid* )
  cdef void glTexImage2D     ( GLenum, GLint, GLint, GLsizei, GLsizei, GLint, 
                               GLenum, GLenum, GLvoid* )
  cdef void glTexImage3D     ( GLenum, GLint, GLint, GLsizei, GLsizei, GLsizei,
                               GLint, GLenum, GLenum, GLvoid* )
  cdef void glGenTextures    ( GLsizei, GLuint* )
  cdef void glDeleteTextures ( GLsizei, GLuint* )
  cdef void glBindTexture    ( GLenum, GLuint )
  cdef void glPrioritizeTextures( GLsizei, GLuint*, GLclampf* )
  cdef GLboolean glAreTexturesResident( GLsizei, GLuint*, GLboolean* )
  cdef GLboolean glIsTexture ( GLuint )
  cdef void glTexSubImage1D  ( GLenum, GLint, GLint, GLsizei, GLenum,
                               GLenum, GLvoid* )
  cdef void glTexSubImage2D  ( GLenum, GLint, GLint, GLint, GLsizei, GLsizei,
                               GLenum, GLenum, GLvoid* )
  cdef void glTexSubImage3D  ( GLenum, GLint, GLint, GLint, GLint, GLsizei,
                               GLsizei, GLsizei, GLenum, GLenum, GLvoid* ) 
  cdef void glCopyTexImage1D ( GLenum, GLint, GLenum, GLint, GLint,
                               GLsizei, GLint )
  cdef void glCopyTexImage2D ( GLenum, GLint, GLenum, GLint, GLint,
                               GLsizei, GLsizei, GLint )
  cdef void glCopyTexSubImage1D( GLenum, GLint, GLint, GLint, GLint, GLsizei )
  cdef void glCopyTexSubImage2D( GLenum, GLint, GLint, GLint, GLint, GLint,
                                 GLsizei, GLsizei )
  cdef void glCopyTexSubImage3D( GLenum, GLint, GLint, GLint, GLint, GLint,
                                 GLint, GLsizei, GLsizei )

  # GLU
  cdef void gluPerspective( GLdouble, GLdouble, 
                            GLdouble, GLdouble )
  cdef void gluSphere( GLUquadricObj*, GLdouble, GLint, GLint )
  cdef void gluCylinder( GLUquadricObj*, GLdouble, GLdouble, GLdouble,
                         GLint, GLint )
  cdef GLUquadricObj* gluNewQuadric( )
  cdef void gluDeleteQuadric( GLUquadricObj* )
  cdef void gluQuadricNormals( GLUquadricObj*, GLenum )
  cdef void gluQuadricTexture( GLUquadricObj*, GLenum )

  # GLEW
  cdef GLenum    glewInit        ( )
  cdef GLboolean glewIsSupported ( char* )

cdef extern from "GL/glu.h":

    ctypedef enum:
    
        # TessCallback
        GLU_TESS_BEGIN                     
        GLU_BEGIN                          
        GLU_TESS_VERTEX                    
        GLU_VERTEX                         
        GLU_TESS_END                       
        GLU_END                            
        GLU_TESS_ERROR                     
        GLU_TESS_EDGE_FLAG                 
        GLU_EDGE_FLAG                      
        GLU_TESS_COMBINE                   
        GLU_TESS_BEGIN_DATA                
        GLU_TESS_VERTEX_DATA               
        GLU_TESS_END_DATA                  
        GLU_TESS_ERROR_DATA                
        GLU_TESS_EDGE_FLAG_DATA            
        GLU_TESS_COMBINE_DATA              

        # TessProperty
        GLU_TESS_WINDING_RULE 
        
        # TessWinding
        GLU_TESS_WINDING_NONZERO
    
    ctypedef struct GLUtesselator "GLUtesselator"
    cdef GLUtesselator* gluNewTess ()
    ctypedef void (*_GLUfuncptr)()
    cdef void gluTessCallback (GLUtesselator*, GLenum, _GLUfuncptr)
    cdef void gluTessNormal (GLUtesselator*, GLdouble, GLdouble, GLdouble)
    cdef void gluTessProperty (GLUtesselator*, GLenum, GLdouble)
    cdef void gluTessBeginPolygon (GLUtesselator*, GLvoid*)
    cdef void gluTessBeginContour (GLUtesselator*)
    cdef void gluTessVertex (GLUtesselator*, GLdouble *, GLvoid*)
    cdef void gluTessEndContour (GLUtesselator*)
    cdef void gluTessEndPolygon (GLUtesselator*) 