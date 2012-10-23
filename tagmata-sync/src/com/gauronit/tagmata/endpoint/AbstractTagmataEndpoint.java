package com.gauronit.tagmata.endpoint;

import org.springframework.ws.server.endpoint.AbstractMarshallingPayloadEndpoint;

import com.gauronit.tagmata.service.TagmataService;

public abstract class AbstractTagmataEndpoint extends AbstractMarshallingPayloadEndpoint {

	protected TagmataService service;
	
	public void setTagmataService(TagmataService service) {
		this.service = service;
	}
	
	@Override
	protected abstract Object invokeInternal(Object arg0) throws Exception;

}
