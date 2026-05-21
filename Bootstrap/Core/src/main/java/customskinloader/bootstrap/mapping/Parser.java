package customskinloader.bootstrap.mapping;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import customskinloader.bootstrap.util.RangeMatcher;
import customskinloader.bootstrap.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public final class Parser {
    public Mappings parse(InputStream inputStream, String mappingType) throws IOException {
        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        Document document = XmlUtils.parseDocument(reader, "customskinloader mapping.xml");
        return this.buildMappings(document, mappingType, VersionMetadataReader.PROTOCOL_VERSION, VersionMetadataReader.WORLD_VERSION);
    }

    private Mappings buildMappings(Document document, String mappingType, int protocolVersion, int worldVersion) {
        List<Mappings.ClassMapping> classMappings = new ArrayList<>();
        List<Element> classElements = XmlUtils.childElements(document.getDocumentElement(), "class");
        for (Element classElement : classElements) {
            String canonicalClassName = XmlUtils.requiredAttribute(classElement, "name");
            String mappedClassName = this.selectMappedValue(XmlUtils.childElements(classElement, "name"), mappingType, protocolVersion, worldVersion);
            if (mappedClassName == null) {
                mappedClassName = canonicalClassName;
            }

            Mappings.ClassMapping classMapping = new Mappings.ClassMapping(canonicalClassName, mappedClassName);
            boolean includeClassMapping = !canonicalClassName.equals(mappedClassName);

            for (Element fieldElement : XmlUtils.childElements(classElement, "field")) {
                String canonicalFieldName = XmlUtils.requiredAttribute(fieldElement, "name");
                String mappedFieldName = this.selectMappedValue(XmlUtils.childElements(fieldElement, "name"), mappingType, protocolVersion, worldVersion);
                if (mappedFieldName == null || canonicalFieldName.equals(mappedFieldName)) {
                    continue;
                }

                classMapping.addFieldMapping(new Mappings.FieldMapping(classMapping, canonicalFieldName, mappedFieldName));
                includeClassMapping = true;
            }

            for (Element methodElement : XmlUtils.childElements(classElement, "method")) {
                String canonicalMethodSignature = XmlUtils.requiredAttribute(methodElement, "name");
                int descriptorIndex = canonicalMethodSignature.indexOf('(');
                if (descriptorIndex < 0) {
                    throw new IllegalArgumentException("Method mapping name must include a descriptor: " + canonicalMethodSignature);
                }

                String canonicalMethodName = canonicalMethodSignature.substring(0, descriptorIndex);
                String canonicalMethodDescriptor = canonicalMethodSignature.substring(descriptorIndex);
                String mappedMethodName = this.selectMappedValue(XmlUtils.childElements(methodElement, "name"), mappingType, protocolVersion, worldVersion);
                if (mappedMethodName == null || canonicalMethodName.equals(mappedMethodName)) {
                    continue;
                }

                classMapping.addMethodMapping(new Mappings.MethodMapping(classMapping, canonicalMethodName, canonicalMethodDescriptor, mappedMethodName));
                includeClassMapping = true;
            }

            if (includeClassMapping) {
                classMappings.add(classMapping);
            }
        }

        return new Mappings(classMappings);
    }

    private String selectMappedValue(List<Element> nameElements, String mappingType, int protocolVersion, int worldVersion) {
        String selectedValue = null;
        for (Element nameElement : nameElements) {
            if (!mappingType.equals(nameElement.getAttribute("type"))) {
                continue;
            }
            if (!RangeMatcher.matches(nameElement.getAttribute("version"), protocolVersion, worldVersion)) {
                continue;
            }

            String currentValue = nameElement.getTextContent() == null ? null : nameElement.getTextContent().trim();
            if (currentValue == null || currentValue.isEmpty()) {
                continue;
            }

            if (selectedValue != null && !selectedValue.equals(currentValue)) {
                throw new IllegalArgumentException("Found multiple values for type \"" + mappingType + "\", protocol version " + protocolVersion + " and world version" + worldVersion + ": " + selectedValue + " vs " + currentValue);
            }

            selectedValue = currentValue;
        }

        return selectedValue;
    }
}
