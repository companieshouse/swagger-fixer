package uk.gov.ch.tools.swagger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

class JsonNodeExtractor {

    public static final String ARRAY = "array";
    private static final String MODELS = "models";
    private static final String DATE = "date";
    private static final String STRING = "string";
    private static final String FORMAT = "format";
    private static final String TYPE = "type";
    private final static Map<String, String> unmodifiableSubstitutions;

    static {
        final Map<String, String> substitutions = new HashMap<>();
        substitutions.put("String", STRING);
        substitutions.put("Array", ARRAY);
        substitutions.put("List", ARRAY);
        substitutions.put("list", ARRAY);
        substitutions.put("<value>", "object");
        unmodifiableSubstitutions = Collections.unmodifiableMap(substitutions);
    }

    private static void fixParentObjectType(ObjectNode node) {
        final String type = node.get(TYPE).asText();
        String subst = unmodifiableSubstitutions.getOrDefault(type, null);
        if (subst != null) {
            node.replace(TYPE, new TextNode(subst));
        } else {
            if (type.equalsIgnoreCase(DATE)) {
                node.replace(TYPE, new TextNode(STRING));
                node.replace(FORMAT, new TextNode(DATE));
            }
        }
    }

    JsonNode convertJson(String jsonContent) {
        JsonNode node = null;
        try {
            final ObjectMapper mapper = new ObjectMapper();
            node = mapper.readTree(jsonContent);
            processExternalModels(node);
            processAtAt(node);
            processTypes(node);
            processHtml(node);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return node;
    }

    /**
     * Search for embedded html markup and remove it, so that it does not
     */
    private void processHtml(final JsonNode node) {
        final Iterator<Entry<String, JsonNode>> nodeFields = node.fields();
        while (nodeFields.hasNext()) {
            final Entry<String, JsonNode> field = nodeFields.next();
            String key = field.getKey();
            final JsonNode value = field.getValue();
            if (value.isTextual()) {
                final String originalValue = value.textValue();
                if (originalValue.contains("<")) {
                    //     System.out.println(originalValue);
                    HtmlCleaner htmlCleaner = new HtmlCleaner();
                    final String fixedValue = htmlCleaner.removeHtml(originalValue);
                    //       System.out.println(fixedValue);
                    replaceTextNodeValue(node, key, value, fixedValue);
                }
            } else {
                processHtml(value);
            }
        }
    }

    private void replaceTextNodeValue(final JsonNode root, final String key, final JsonNode value,
            final String fixedValue) {
        final List<JsonNode> possibleParents = root.findParents(key);
        for (JsonNode parent : possibleParents) {
            if (parent.get(key) == value) {
                ((ObjectNode) parent).replace(key, TextNode.valueOf(fixedValue));
            }
        }
    }

    private void processAtAt(JsonNode node) {
        final ObjectNode baseParent = (ObjectNode) node.findParent("basePath");
        if (baseParent != null) {
            String value = baseParent.get("basePath").asText();
            if (value.startsWith("@@")) {
                value = "http://example.com/" + value.replace("@", "");
            }
            baseParent.replace("basePath", new TextNode(value));
        }
    }

    /**
     * Fix the non-standard external Models, by loading the external files into the correct part of
     * the JSON DOM so that the converter actually parses correctly and include referenced models.
     * This will act recursively as the internals of the included models may need to be fixed, in
     * their own right.
     */
    private void processExternalModels(JsonNode node) {
        final JsonNode external = node.get("externalModels");
        if (external != null && external.isArray()) {
            external.forEach(el -> includeModelFile(node, el));
            ((ObjectNode) node).remove("externalModels");
        }
    }

    private void includeModelFile(JsonNode root, JsonNode el) {
        final String path = el.get("path").asText(null);
        if (path != null) {
            try {
                final JsonNode inclusion = Fix1_2.fixJson(path);
                final ObjectNode includeModels = (ObjectNode) inclusion.get(MODELS);
                final ObjectNode rootModels = (ObjectNode) root.get(MODELS);
                Iterator<Entry<String, JsonNode>> it = includeModels.fields();
                while (it.hasNext()) {
                    final Entry<String, JsonNode> field = it.next();
                    rootModels.replace(field.getKey(), field.getValue());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void processTypes(final JsonNode node) {
        subtituteKnownBadTypes(node);
        processTypeProperties(node);
    }

    private void processTypeProperties(final JsonNode node) {
        final JsonNode models = node.findValue("models");
        if (models != null) {
            for (final JsonNode model : models) {
                final JsonNode properties = model.findValue("properties");
                for (final JsonNode property : properties) {
                    fixMissingParameterType(property);
                    fixEnums(property);
                }
            }
        }
    }

    private void fixEnums(final JsonNode property) {

        final JsonNode enumNode = property.findValue("enum");

        final List<String> enumValues = new LinkedList<>();
        if (enumNode != null && enumNode.isArray()) {
            enumNode.forEach(en -> enumValues.add(en.asText()));
            final List<String> distinctValues = enumValues.stream().distinct()
                    .collect(Collectors.toList());
            //  System.out.println(enumNode.getNodeType()+ ":"+ enumNode);
            // are there duplicate enums values
            if (distinctValues.size() < enumValues.size()) {
                final ObjectNode oProperty = (ObjectNode) property;
                final ArrayNode aEnum = oProperty.putArray("enum");
                distinctValues.forEach(e -> aEnum.add(e));
                System.out.println(aEnum.getNodeType() + ":" + aEnum);
                System.out.println(property.getNodeType() + ":" + property);
            }
        }
    }

    private boolean f(Object a, Object b) {
        return false;
    }

    private void fixMissingParameterType(final JsonNode property) {
        if (!property.has(TYPE)) {
            ((ObjectNode) property).replace(TYPE, ((ObjectNode) property)
                    .textNode(property.has("items") ? ARRAY : "object"));
        }
    }

    private void subtituteKnownBadTypes(JsonNode node) {
        final List<JsonNode> typeParents = node.findParents(TYPE);
        typeParents.stream()
                .filter(n -> n instanceof ObjectNode)
                .map(on -> (ObjectNode) on)
                .forEach(JsonNodeExtractor::fixParentObjectType);
    }
}
