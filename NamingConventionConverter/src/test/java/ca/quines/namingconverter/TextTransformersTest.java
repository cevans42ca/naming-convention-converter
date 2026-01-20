package ca.quines.namingconverter;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TextTransformersTest {

    @Test
    void testRemoveDashesAndTrim() {
        assertEquals("HelloWorld", TextTransformers.removeDashesAndTrim("  Hello-World  "));
        assertEquals("test", TextTransformers.removeDashesAndTrim("-test-"));
        assertEquals("", TextTransformers.removeDashesAndTrim(null));
    }

    @Test
    void testReplaceDashesWithSpaces() {
        assertEquals("Hello World", TextTransformers.replaceDashesWithSpaces("Hello-World"));
        assertEquals("", TextTransformers.replaceDashesWithSpaces(null));
    }

    @Test
    void testUnderscoresToSpaces() {
        assertEquals("snake case test", TextTransformers.underscoresToSpaces("snake_case_test"));
        assertEquals("", TextTransformers.underscoresToSpaces(null));
    }

    @Test
    void testCollapseWhitespace() {
        assertEquals("multiple spaces here", TextTransformers.collapseWhitespace("  multiple    spaces   here  "));
        assertEquals("line breaks", TextTransformers.collapseWhitespace("line\nbreaks"));
        assertEquals("", TextTransformers.collapseWhitespace(null));
    }

    @Test
    void testToCamelCase() {
        // Lower Camel Case
        assertEquals("helloWorld", TextTransformers.toCamelCase("hello world", false));
        assertEquals("testCase", TextTransformers.toCamelCase("test_case", false));
        
        // Upper Camel Case (also called PascalCase)
        assertEquals("HelloWorld", TextTransformers.toCamelCase("hello-world", true));
        assertEquals("TestCase", TextTransformers.toCamelCase("test_case", true));

        assertNull(TextTransformers.toCamelCase(null, true));
        assertNull(TextTransformers.toCamelCase(null, false));
    }

    @Test
    void testToUpperSnake() {
        assertEquals("HELLO_WORLD", TextTransformers.toUpperSnake("helloWorld"));
        assertEquals("MY_TEST_STRING", TextTransformers.toUpperSnake("MyTestString"));
        assertEquals("SPACE_TEST", TextTransformers.toUpperSnake("space test"));
        assertEquals("", TextTransformers.toUpperSnake(null));
    }

    @Test
    void testToSqlInClause() {
        String input = "1\n2\n3";
        assertEquals("(1, 2, 3)", TextTransformers.toSqlInClause(input, false));
        assertEquals("('1', '2', '3')", TextTransformers.toSqlInClause(input, true));

        assertEquals("", TextTransformers.toSqlInClause(null, false));
        assertEquals("", TextTransformers.toSqlInClause(null, true));
    }

    @Test
    void testCamelCaseToSpaces() {
        assertEquals("Camel Case", TextTransformers.camelCaseToSpaces("CamelCase"));
        assertEquals("this Is A Test", TextTransformers.camelCaseToSpaces("thisIsATest"));
        assertEquals("", TextTransformers.camelCaseToSpaces(null));
    }

    @Test
    void testSpacesToUpperSnake() {
        assertEquals("HELLO_WORLD", TextTransformers.spacesToUpperSnake("  hello  world  "));
        assertEquals("", TextTransformers.spacesToUpperSnake(null));
    }

    @Test
    void testCamelCaseToUpperSnake() {
        assertEquals("CAMEL_CASE_TEST", TextTransformers.camelCaseToUpperSnake("CamelCaseTest"));
    }

    @Test
    void testSnakeToCamelAndPascal() {
        assertEquals("snakeCase", TextTransformers.snakeToCamel("SNAKE_CASE", false));
        assertEquals("SnakeCase", TextTransformers.snakeToCamel("snake_case", true));
    }

    @Test
    void testToTitleCase() {
        // First and last words always capitalized, minor words in middle stay lower case
        assertEquals("Adventures in Wonderland", TextTransformers.toTitleCase("adventures in wonderland"));
        assertEquals("A Tale of Two Cities", TextTransformers.toTitleCase("a tale of two cities"));
        assertEquals("", TextTransformers.toTitleCase(null));
    }

    @Test
    void testAllInitialCaps() {
        assertEquals("Adventures In Wonderland", TextTransformers.allInitialCaps("adventures in wonderland"));
        assertEquals("", TextTransformers.allInitialCaps(null));
    }

    @Test
    void testFirstCharacterCasing() {
        assertEquals("java", TextTransformers.lowercaseFirst("Java"));
        assertEquals("Java", TextTransformers.uppercaseFirst("java"));
        assertEquals("", TextTransformers.lowercaseFirst(""));
        assertEquals("", TextTransformers.uppercaseFirst(""));
        assertEquals("", TextTransformers.lowercaseFirst(null));
        assertEquals("", TextTransformers.uppercaseFirst(null));
    }

    @Test
    void testDelimitedLists() {
        String input = "Apple\nBanana\n\nCherry"; // Includes a blank line
        assertEquals("Apple, Banana, Cherry", TextTransformers.newlineToComma(input));
        assertEquals("'Apple', 'Banana', 'Cherry'", TextTransformers.newlineToQuotedComma(input));

        assertEquals("", TextTransformers.newlineToComma(null));
        assertEquals("", TextTransformers.newlineToQuotedComma(null));
    }

    @Test
    void testInClauses() {
        assertEquals("IN (101, 102)", TextTransformers.toInClauseInt("101\n102"));
        assertEquals("IN ('A', 'B')", TextTransformers.toInClauseString("A\nB"));
        assertEquals("", TextTransformers.toInClauseInt(null));
        assertEquals("", TextTransformers.toInClauseString(null));
    }

}
