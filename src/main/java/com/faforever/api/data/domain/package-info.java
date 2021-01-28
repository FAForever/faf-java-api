/**
 * Contains JPA entity classes, adapted to the needs of JSON-API.
 */
@AnalyzerDef(name = "case_insensitive",
  tokenizer = @TokenizerDef(factory = NGramTokenizerFactory.class, params = {
    @Parameter(name = "minGramSize", value = "3"),
    @Parameter(name = "maxGramSize", value = "10")
  }),
  filters = {
    @TokenFilterDef(factory = LowerCaseFilterFactory.class)
  }
)
package com.faforever.api.data.domain;

import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.ngram.NGramTokenizerFactory;
import org.hibernate.search.annotations.AnalyzerDef;
import org.hibernate.search.annotations.Parameter;
import org.hibernate.search.annotations.TokenFilterDef;
import org.hibernate.search.annotations.TokenizerDef;
