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
package com._4dconcept.springframework.data.marklogic.repository.query;

import com._4dconcept.springframework.data.marklogic.core.MarklogicOperations;
import com._4dconcept.springframework.data.marklogic.core.mapping.MarklogicMappingContext;
import com._4dconcept.springframework.data.marklogic.core.query.Query;
import org.springframework.data.repository.query.ParameterAccessor;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.parser.PartTree;

/**
 * {@link RepositoryQuery} implementation for Marklogic.
 *
 * @author Stephane Toussaint
 */
public class PartTreeMarklogicQuery extends AbstractMarklogicQuery {

    private final PartTree tree;
    private final MarklogicMappingContext context;

    public PartTreeMarklogicQuery(MarklogicQueryMethod method, MarklogicOperations marklogicOperations) {
        super(method, marklogicOperations);

        this.tree = new PartTree(method.getName(), method.getResultProcessor().getReturnedType().getDomainType());
        this.context = (MarklogicMappingContext) marklogicOperations.getConverter().getMappingContext();
    }

    @Override
    protected Query createQuery(ParameterAccessor accessor) {
        MarklogicQueryCreator creator = new MarklogicQueryCreator(tree, accessor, context, getQueryMethod().getReturnedObjectType());
        Query query = creator.createQuery();

        if (tree.isLimiting()) {
            query.setLimit(tree.getMaxResults() == null ? 0 : tree.getMaxResults());
        }

        return query;
    }

    @Override
    protected boolean isDeleteQuery() {
        return tree.isDelete();
    }
}
