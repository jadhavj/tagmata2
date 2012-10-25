/*******************************************************************************
 * Copyright (c) 2012, Gauronit, LLC.
 * All rights reserved. Tagmata and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Jayesh J - initial API and implementation
 ******************************************************************************/
package com.gauronit.tagmata.service;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;

import com.gauronit.tagmata.schema.Card;
import com.gauronit.tagmata.schema.FetchCardsResponse;
import com.gauronit.tagmata.schema.RemoveSyncCardsResponse;
import com.gauronit.tagmata.schema.SyncCardsResponse;
import com.gauronit.tagmata.schema.TagmataException;

public class TagmataServiceImpl implements TagmataService {
	
	Logger logger = Logger.getLogger(TagmataServiceImpl.class);

	public void initialize() {
		// start sync engine
		logger.info("Initialized.");
	}
	
	@Override
	public FetchCardsResponse fetchCards() throws TagmataException {
		FetchCardsResponse resp = new FetchCardsResponse();
		Card card = new Card();
		card.setTitle("title");
		card.setTags("tags");
		card.setText("text");
		card.setAction("action");
		card.setId("123abc");
		resp.getCard().add(card);
		return resp;
	}

	@Override
	public SyncCardsResponse syncCards(List<Card> cards) throws TagmataException {
		SyncCardsResponse resp = new SyncCardsResponse();
		resp.setMessage("OK");
		return resp;
	}

	@Override
	public RemoveSyncCardsResponse removeSyncCards(List<Card> cards)
			throws TagmataException {
		RemoveSyncCardsResponse resp = new RemoveSyncCardsResponse();
		resp.setMessage("OK");
		return resp;
	}

	
}
