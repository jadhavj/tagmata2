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

import java.util.List;

import com.gauronit.tagmata.schema.Card;
import com.gauronit.tagmata.schema.FetchCardsResponse;
import com.gauronit.tagmata.schema.RemoveSyncCardsResponse;
import com.gauronit.tagmata.schema.SyncCardsResponse;
import com.gauronit.tagmata.schema.TagmataException;

public interface TagmataService {
	public FetchCardsResponse fetchCards() throws TagmataException;
	public SyncCardsResponse syncCards(List<Card> cards) throws TagmataException;
	public RemoveSyncCardsResponse removeSyncCards(List<Card> cards) throws TagmataException;
}
