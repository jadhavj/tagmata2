package com.gauronit.tagmata.endpoint;


public class FetchCardsEndpoint extends AbstractTagmataEndpoint {

	@Override
	protected Object invokeInternal(Object request) throws Exception {
		// TODO Auto-generated method stub
		return service.fetchCards();
	}

}
