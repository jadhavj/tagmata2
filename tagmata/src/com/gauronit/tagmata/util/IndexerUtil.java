/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gauronit.tagmata.util;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;

/**
 *
 * @author jjayesh
 */
public class IndexerUtil {

    public static Query getTokenizedQuery(BooleanQuery query, String field, String searchText, boolean superFuzzy) {
        Query q = null;
        try {
            //searchText = StringUtil.removeSpecialChars(searchText);
            String[] tokens = StringUtil.getTokens(searchText);

            for (String token : tokens) {
                if (!superFuzzy) {
                    q = new TermQuery(new Term(field, token.toLowerCase()));
                } else {
                    q = new WildcardQuery(new Term(field, "*" + token.toLowerCase() + "*"));
                }
                query.add(q, BooleanClause.Occur.SHOULD);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return q;
    }
}
