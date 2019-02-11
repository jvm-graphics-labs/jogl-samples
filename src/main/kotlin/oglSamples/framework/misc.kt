package oglSamples.framework


var DEBUG = true

enum class Heuristic(val i: Int) {
    EQUAL_BIT(1 shl 0),
    ABSOLUTE_DIFFERENCE_MAX_ONE_BIT(1 shl 1),
    ABSOLUTE_DIFFERENCE_MAX_ONE_KERNEL_BIT(1 shl 2),
    ABSOLUTE_DIFFERENCE_MAX_ONE_LARGE_KERNEL_BIT(1 shl 3),
    MIPMAPS_ABSOLUTE_DIFFERENCE_MAX_ONE_BIT(1 shl 4),
    MIPMAPS_ABSOLUTE_DIFFERENCE_MAX_FOUR_BIT(1 shl 5),
    MIPMAPS_ABSOLUTE_DIFFERENCE_MAX_CHANNEL_BIT(1 shl 6),
    ALL(EQUAL_BIT.i or ABSOLUTE_DIFFERENCE_MAX_ONE_BIT.i or ABSOLUTE_DIFFERENCE_MAX_ONE_KERNEL_BIT.i or
            ABSOLUTE_DIFFERENCE_MAX_ONE_LARGE_KERNEL_BIT.i or MIPMAPS_ABSOLUTE_DIFFERENCE_MAX_ONE_BIT.i or
            MIPMAPS_ABSOLUTE_DIFFERENCE_MAX_FOUR_BIT.i)
}

enum class Vendor { DEFAULT, AMD, INTEL, NVIDIA }

//    int operator ()();
//    void log(csv & CSV, char const * String);
//    void setupView(bool Translate, bool RotateX, bool RotateY);

enum class ViewSetupFlag {
    TRANSLATE, ROTATE_X, ROTATE_Y;

    val i = 1 shl ordinal
}

enum class Success { RUN_ONLY, GENERATE_ERROR, MATCH_TEMPLATE }