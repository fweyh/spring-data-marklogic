package org.springframework.data.marklogic.core.convert;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.springframework.core.convert.ConverterNotFoundException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.data.marklogic.core.mapping.Document;
import org.springframework.data.marklogic.core.mapping.MarklogicMappingContext;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.assertThat;

/**
 * --Description--
 *
 * @author Stéphane Toussaint
 */

public class MarklogicMappingConverterTest {

    @Test(expected = ConverterNotFoundException.class)
    public void throwsExceptionIfUnConvertibleObject() throws Exception {
        MarklogicMappingConverter marklogicMappingConverter = createConverterWithDelegates();
        marklogicMappingConverter.write(new UnConvertibleObject(), new MarklogicContentHolder());
    }

    @Test
    public void marklogicContentHolderSetWithConvertedContentWithExplicitConverter() throws Exception {
        MarklogicMappingConverter marklogicMappingConverter = createConverterWithDelegates(new ConvertibleObjectConverter());

        MarklogicContentHolder contentHolder = new MarklogicContentHolder();
        marklogicMappingConverter.write(new ConvertibleObject(), contentHolder);
        assertThat(contentHolder.getContent(), CoreMatchers.notNullValue());
        assertThat(contentHolder.getContent(), CoreMatchers.is("<empty />"));
    }

    @Test
    public void marklogicContentHolderSetWithConvertedContentWithConditionalConverter() throws Exception {
        MarklogicMappingConverter marklogicMappingConverter = createConverterWithDelegates(new DocumentConverter());

        MarklogicContentHolder contentHolder = new MarklogicContentHolder();
        marklogicMappingConverter.write(new Person("1"), contentHolder);
        assertThat(contentHolder.getContent(), CoreMatchers.notNullValue());
        assertThat(contentHolder.getContent(), CoreMatchers.is("<person><id>1</id></person>"));
    }

    class UnConvertibleObject {}

    class ConvertibleObject {}

    @Document(
            uri = "/person/#{id}.xml",
            defaultCollectionPrefix = "collection",
            defaultCollection = "#{getClass().getSimpleName()}"
    )
    class Person {
        public String id;

        public Person(String id) {
            this.id = id;
        }
    }

    class ConvertibleObjectConverter implements GenericConverter {

        @Override
        public Set<ConvertiblePair> getConvertibleTypes() {
            return Collections.singleton(new ConvertiblePair(ConvertibleObject.class, String.class));
        }

        @Override
        public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
            return "<empty />";
        }

    }

    class DocumentConverter implements ConditionalGenericConverter {
        @Override
        public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
            return sourceType.getObjectType().isAnnotationPresent(Document.class);
        }

        @Override
        public Set<ConvertiblePair> getConvertibleTypes() {
            return Collections.singleton(new ConvertiblePair(Object.class, String.class));
        }

        @Override
        public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
            return "<person><id>1</id></person>";
        }

    }

    private MarklogicMappingConverter createConverterWithDelegates(GenericConverter... converters) throws Exception {
        MarklogicMappingConverter marklogicMappingConverter = new MarklogicMappingConverter(new MarklogicMappingContext());
        marklogicMappingConverter.setConverters(Arrays.asList(converters));
        marklogicMappingConverter.afterPropertiesSet();
        return marklogicMappingConverter;
    }

}