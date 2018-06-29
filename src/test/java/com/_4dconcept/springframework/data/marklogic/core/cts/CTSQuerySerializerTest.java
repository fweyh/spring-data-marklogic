/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com._4dconcept.springframework.data.marklogic.core.cts;

import com._4dconcept.springframework.data.marklogic.core.query.Criteria;
import com._4dconcept.springframework.data.marklogic.core.query.Query;
import com._4dconcept.springframework.data.marklogic.core.query.SortCriteria;
import org.hamcrest.Matchers;
import org.junit.Test;

import javax.xml.namespace.QName;
import java.util.Arrays;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * --Description--
 *
 * @author stoussaint
 * @since 2017-08-01
 */
public class CTSQuerySerializerTest {

    @Test
    public void parseEmptyQuery() {
        String ctsQuery = new CTSQuerySerializer(new Query()).asCtsQuery();

        assertThat(ctsQuery, is("cts:search(fn:collection(), (), ())"));
    }

    @Test
    public void parsePopulatedQuery() {
        Query query = new Query();
        query.setCriteria(new Criteria(Criteria.Operator.and, Arrays.asList(
            new Criteria(new QName("name"), "Me"),
            new Criteria(new QName("town"), "Paris")
        )));

        String ctsQuery = new CTSQuerySerializer(query).asCtsQuery();

        assertThat(ctsQuery, is("cts:search(fn:collection(), cts:and-query((cts:element-value-query(fn:QName('', 'name'), 'Me'), cts:element-value-query(fn:QName('', 'town'), 'Paris'))), ())"));
    }

    @Test
    public void parseQueryWithNotOperator() {
        Query query = new Query();
        query.setCriteria(new Criteria(Criteria.Operator.not, new Criteria(new QName("town"), "Paris")));

        String ctsQuery = new CTSQuerySerializer(query).asCtsQuery();

        assertThat(ctsQuery, is("cts:search(fn:collection(), cts:not-query(cts:element-value-query(fn:QName('', 'town'), 'Paris')), ())"));
    }

    @Test
    public void parseQueryWithPagination() {
        Query query = new Query();
        query.setCollection("Collection1");
        query.setLimit(10);
        query.setSkip(0);
        String ctsQuery = new CTSQuerySerializer(query).asCtsQuery();

        assertThat(ctsQuery, is("cts:search(fn:collection('Collection1'), (), ())[1 to 10]"));
    }

    @Test
    public void parseQueryWithSortOrders() {
        Query query = new Query();
        query.setCollection("Collection1");
        query.setSortCriteria(Arrays.asList(
                new SortCriteria(new QName("", "age"), true),
                new SortCriteria(new QName("", "lastname"))
        ));
        String ctsQuery = new CTSQuerySerializer(query).asCtsQuery();

        assertThat(ctsQuery, is("cts:search(fn:collection('Collection1'), (), (cts:index-order(cts:element-reference(fn:QName('', 'age')), ('descending')), cts:index-order(cts:element-reference(fn:QName('', 'lastname')), ('ascending'))))"));
    }

    @Test
    public void parseQueryWithNonStringValue() {
        Query query = new Query();
        query.setCriteria(new Criteria(new QName("age"), 38));

        String ctsQuery = new CTSQuerySerializer(query).asCtsQuery();
        System.out.println(ctsQuery);
    }

    // Issue #6
    @Test
    public void parseQueryWithStringValueContainingApostropheIsEscaped() {
        Query query = new Query();
        query.setCriteria(new Criteria(new QName("name"), "l'apostrophe"));

        String ctsQuery = new CTSQuerySerializer(query).asCtsQuery();
        System.out.println(ctsQuery);
        assertThat(ctsQuery, Matchers.containsString("l''apostrophe"));
    }
}