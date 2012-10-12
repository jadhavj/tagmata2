/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gauronit.tagmata.core;

import org.apache.lucene.document.Document;

/**
 *
 * @author jjayesh
 */
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
