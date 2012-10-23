package com.gauronit.tagmata.endpoint;

import com.gauronit.tagmata.schema.SyncCardsRequest;

public class SyncCardsEndpoint extends AbstractTagmataEndpoint {

	@Override
	protected Object invokeInternal(Object request) throws Exception {
		// TODO Auto-generated method stub
		SyncCardsRequest req = (SyncCardsRequest) request;
		return service.syncCards(req.getCard());
	}

}
