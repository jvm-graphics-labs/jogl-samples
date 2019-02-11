package oglSamples.framework

/**
 * Created by GBarbieri on 27.03.2017.
 */

object semantic {

    object buffer {
        val STATIC = 0
        val DYNAMIC = 1
    }

    object uniform {
        val MATERIAL = 0
        val TRANSFORM0 = 1
        val TRANSFORM1 = 2
        val INDIRECTION = 3
        val CONSTANT = 0
        val PER_FRAME = 1
        val PER_PASS = 2
        val LIGHT = 3
    }

    object sampler {
        val DIFFUSE = 0
        val POSITION = 4
        val TEXCOORD = 5
        val COLOR = 6
    }

    object image {
        val DIFFUSE = 0
        val PICKING = 1
    }

    object attr {
        val POSITION = 0
        val NORMAL = 1
        val COLOR = 3
        val TEXCOORD = 4
        val DRAW_ID = 5
    }

    object vert {
        val POSITION = 0
        val COLOR = 3
        val TEXCOORD = 4
        val INSTANCE = 7
    }

    object frag {
        val COLOR = 0
        val RED = 0
        val GREEN = 1
        val BLUE = 2
        val ALPHA = 0
    }

    object renderbuffer {
        val DEPTH = 0
        val COLOR0 = 1
    }

    object storage {
        val VERTEX = 0
    }
}
