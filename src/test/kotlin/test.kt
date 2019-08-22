import io.kotlintest.specs.StringSpec
import oglSamples.framework.AUTOMATED_TESTS
import oglSamples.tests.es200.es_200_draw_elements

class Test : StringSpec() {

    init {
        AUTOMATED_TESTS = true

        "es200" {
            es_200_draw_elements()()
        }
    }
}