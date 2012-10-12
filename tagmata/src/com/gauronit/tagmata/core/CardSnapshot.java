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
package com.gauronit.tagmata.core;

import org.apache.lucene.document.Document;

public class CardSnapshot {

    public String getHighlights() {
        return highlights;
    }
    
    public CardSnapshot(String highlights, Document doc) {
        this.highlights = highlights;
        this.doc = doc;
        title = doc.get("title");
        tags = doc.get("tags");
        text = doc.get("text");
        indexName = doc.get("indexName");
        id = doc.get("id");
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getTags() {
        return tags;
    }
    
    public String getText() {
        return text;
    }
    
    public Document getDoc() {
        return doc;
    }

    public String getId() {
        return id;
    }

    public String getIndexName() {
        return indexName;
    }
    
    private String highlights;
    private Document doc;
    private String title;
    private String tags;
    private String text;
    private String indexName;
    private String id;
}
