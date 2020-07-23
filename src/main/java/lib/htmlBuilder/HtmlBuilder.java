package lib.htmlBuilder;

import java.util.*;

public class HtmlBuilder {

    static abstract class Node {
    }

    public static final class StringNode extends Node {
        private String string;

        StringNode(String string) {
            this.string = string;
        }

        @Override
        public String toString() {
            return string;
        }
    }

    public static class ElementNode extends Node {
        private String tagName;
        private List<Node> children;
        private List<AttributeNode> attributes;
        private Boolean closeSelf;

        ElementNode(String name, Boolean closeSelf, List<AttributeNode> attributeNodes, ElementNode... nodes) {
            this.tagName = name;
            this.closeSelf = closeSelf;
            children = Arrays.asList(nodes);
            attributes = attributeNodes;
        }

        ElementNode(String name, Boolean closeSelf, List<AttributeNode> attributeNodes, StringNode stringNode) {
            this.tagName = name;
            this.closeSelf = closeSelf;
            children = Collections.singletonList(stringNode);
            attributes = attributeNodes;
        }

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("<");
            stringBuilder.append(tagName);
            for (AttributeNode attribute : attributes) {
                stringBuilder.append(" ");
                stringBuilder.append(attribute.attribute);
                stringBuilder.append("=\"");
                stringBuilder.append(attribute.value);
                stringBuilder.append("\"");
            }
            stringBuilder.append(">");
            for (Node child : children) {
                stringBuilder.append(child.toString());
            }
            if (!closeSelf) {
                stringBuilder.append("</");
                stringBuilder.append(tagName);
                stringBuilder.append(">");
            }
            return stringBuilder.toString();
        }

    }

    public static class AttributeNode {
        private String attribute;
        private String value;

        AttributeNode(String attribute, String value) {
            // TODO: 動的な文字列が来たときにXSSしないかどうか
            if (attribute.contains("<")
                    || attribute.contains(">")
                    || attribute.contains("\""))
                attribute = "";
            if (value.contains("<")
                    || value.contains(">")
                    || value.contains("\""))
                value = "";
            this.attribute = attribute;
            this.value = value;
        }
    }

    public static AttributeNode attribute(String attribute, String value) {
        return new AttributeNode(attribute, value);
    }

    public static List<AttributeNode> attributes(AttributeNode... attributeNodes) {
        return Arrays.asList(attributeNodes);
    }

    public static String buildHtml(Tags.HtmlElement html) {
        return "<!DOCTYPE html>" + html.toString();
    }

//    final static Set<Character> escapeChars;
//
//    static {
//        escapeChars = new HashSet<>();
//        escapeChars.add('<');
//        escapeChars.add('>');
//        escapeChars.add('&');
//        escapeChars.add('"');
//    }
}
