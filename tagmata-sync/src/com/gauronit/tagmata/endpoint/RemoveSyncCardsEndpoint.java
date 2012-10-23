package com.gauronit.tagmata.endpoint;

import com.gauronit.tagmata.schema.RemoveSyncCardsRequest;

public class RemoveSyncCardsEndpoint extends AbstractTagmataEndpoint {

	@Override
	protected Object invokeInternal(Object request) throws Exception {
		// TODO Auto-generated method stub
		RemoveSyncCardsRequest  req = (RemoveSyncCardsRequest) request;
		return service.removeSyncCards(req.getCard());
	}

}
