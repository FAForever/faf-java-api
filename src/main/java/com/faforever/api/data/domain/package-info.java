/**
 * Contains JPA entity classes, adapted to the needs of JSON-API.
 */

@AnalyzerDef(name = "case_insensitive",
  tokenizer = @TokenizerDef(factory = WhitespaceTokenizerFactory.class),
  filters = {
    @TokenFilterDef(factory = StandardFilterFactory.class),
    @TokenFilterDef(factory = LowerCaseFilterFactory.class),
  })
package com.faforever.api.data.domain;

import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.core.WhitespaceTokenizerFactory;
import org.apache.lucene.analysis.standard.StandardFilterFactory;
import org.hibernate.search.annotations.AnalyzerDef;
import org.hibernate.search.annotations.TokenFilterDef;
import org.hibernate.search.annotations.TokenizerDef;
