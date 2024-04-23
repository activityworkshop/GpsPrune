package tim.prune.function.search;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SearchWikipediaNamesTest {

    @Test
    public void testEncoding_empty()
    {
        Assertions.assertEquals("", SearchWikipediaNames.encodeSearchTerm(null));
        Assertions.assertEquals("", SearchWikipediaNames.encodeSearchTerm(""));
        Assertions.assertEquals("", SearchWikipediaNames.encodeSearchTerm("  "));
    }

    @Test
    public void testEncoding_unchanged()
    {
        Assertions.assertEquals("Abcdef", SearchWikipediaNames.encodeSearchTerm("Abcdef"));
        Assertions.assertEquals("1234567890.", SearchWikipediaNames.encodeSearchTerm("  1234567890. "));
        Assertions.assertEquals("some+++++spaces", SearchWikipediaNames.encodeSearchTerm("some     spaces"));
    }

    @Test
    public void testEncoding_encoded() {
        Assertions.assertEquals("%3B%2B%24+%27%28%29", SearchWikipediaNames.encodeSearchTerm(";+$ '()"));
        Assertions.assertEquals("aeoeue.%C3%84%C3%8F%C3%96%C3%9C", SearchWikipediaNames.encodeSearchTerm("äöü.ÄÏÖÜ"));
        Assertions.assertEquals("e+e+e+e+e", SearchWikipediaNames.encodeSearchTerm("è é ê ë ë"));
        Assertions.assertEquals("a%0Ab", SearchWikipediaNames.encodeSearchTerm("a\nb"));
    }
}
