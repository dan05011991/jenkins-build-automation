package helpers

import static org.junit.Assert.assertEquals

class CustomAssertHelper {
    static void  assertStringArray(String[] expected, ArrayList<String> actual) {
        String pattern = "@[a-zA-Z0-9]{4,8}"
        String guidPattern = "[a-z0-9]{8}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{12}"
        assertEquals("Call stack size has changed", expected.size(), actual.size())
        actual.indexed().collect { index, item ->
            String actualStr = item.toString().replaceAll(pattern, "@00000000")
                                                .replaceAll(guidPattern, "12345678-1234-1234-1234-123456789012")
            String expectedStr = expected[index].replaceAll(pattern, "@00000000")
                                                .replaceAll(guidPattern, "12345678-1234-1234-1234-123456789012")
            assertEquals(expectedStr, actualStr)
        }
    }
}
