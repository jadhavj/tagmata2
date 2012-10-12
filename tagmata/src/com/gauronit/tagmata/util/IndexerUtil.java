/*******************************************************************************
 * Copyright (c) 2012 Gauronit Technologies.
 * All rights reserved. Tagmata and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Jayesh Jadhav - initial API and implementation
 ******************************************************************************/
package com.gauronit.tagmata.util;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;

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
