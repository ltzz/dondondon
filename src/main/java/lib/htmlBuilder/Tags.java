package lib.htmlBuilder;

import java.util.ArrayList;
import java.util.List;

import static lib.htmlBuilder.HtmlBuilder.*;

public class Tags {

    static final class HtmlElement extends ElementNode {
        HtmlElement(List<AttributeNode> attributeNodes, HeadElement head, BodyElement body) {
            super("html", false, attributeNodes, head, body);
        }
    }

    public static HtmlElement html(List<AttributeNode> attributeNodes, HeadElement head, BodyElement body) {
        return new HtmlElement(attributeNodes, head, body);
    }

    public static HtmlElement html(HeadElement head, BodyElement body) {
        return new HtmlElement(new ArrayList<>(), head, body);
    }

    static final class HeadElement extends ElementNode {
        HeadElement(List<AttributeNode> attributeNodes, ElementNode... nodes) {
            super("head", false, attributeNodes, nodes);
        }
    }

    public static HeadElement head(ElementNode... nodes) {
        return new HeadElement(new ArrayList<>(), nodes);
    }

    static final class MetaElement extends ElementNode {
        MetaElement(List<AttributeNode> attributeNodes, ElementNode... nodes) {
            super("meta", true, attributeNodes, nodes);
        }
    }

    public static ElementNode meta(List<AttributeNode> attributeNodes) {
        return new MetaElement(attributeNodes);
    }

    static final class ScriptElement extends ElementNode {
        ScriptElement(List<AttributeNode> attributeNodes, StringNode node) {
            super("script", false, attributeNodes, node);
        }
    }

    public static ElementNode script(List<AttributeNode> attributeNodes, StringNode node) {
        return new ScriptElement(attributeNodes, node);
    }

    public static ElementNode script(StringNode node) {
        return new ScriptElement(new ArrayList<>(), node);
    }

    static final class StyleElement extends ElementNode {
        StyleElement(List<AttributeNode> attributeNodes, StringNode node) {
            super("style", false, attributeNodes, node);
        }
    }

    public static ElementNode style(List<AttributeNode> attributeNodes, StringNode node) {
        return new StyleElement(attributeNodes, node);
    }

    public static ElementNode style(StringNode node) {
        return new StyleElement(new ArrayList<>(), node);
    }

    static final class BodyElement extends ElementNode {
        BodyElement(List<AttributeNode> attributeNodes, ElementNode... nodes) {
            super("body", false, attributeNodes, nodes);
        }
    }

    public static BodyElement body(ElementNode... nodes) {
        return new BodyElement(new ArrayList<>(), nodes);
    }

    static final class DivElement extends ElementNode {
        DivElement(List<AttributeNode> attributeNodes, ElementNode... nodes) {
            super("div", false, attributeNodes, nodes);
        }

        DivElement(List<AttributeNode> attributeNodes, StringNode node) {
            super("div", false, attributeNodes, node);
        }
    }

    public static ElementNode div(List<AttributeNode> attributeNodes, ElementNode... nodes) {
        return new DivElement(attributeNodes, nodes);
    }

    public static ElementNode div(List<AttributeNode> attributeNodes, StringNode node) {
        return new DivElement(attributeNodes, node);
    }

    public static ElementNode div(ElementNode... nodes) {
        return new DivElement(new ArrayList<>(), nodes);
    }

    public static ElementNode div(StringNode node) {
        return new DivElement(new ArrayList<>(), node);
    }

    public static StringNode rawString(String string) {
        return new StringNode(string);
    }
}
