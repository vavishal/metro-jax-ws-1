/*
 * Copyright (c) 2004, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package fromwsdl.asyncprovider.simple.server;

import com.sun.xml.ws.api.server.AsyncProvider;
import com.sun.xml.ws.api.server.AsyncProviderCallback;

import jakarta.xml.bind.JAXBContext;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import jakarta.xml.ws.WebServiceContext;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.WebServiceProvider;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@WebServiceProvider(
    wsdlLocation="WEB-INF/wsdl/hello_literal.wsdl",
    targetNamespace="urn:test",
    serviceName="Hello",
    portName="HelloPort")

public class HelloAsyncImpl implements AsyncProvider<Source> {

    private static final JAXBContext jaxbContext = createJAXBContext();
    private int bodyIndex;

    public jakarta.xml.bind.JAXBContext getJAXBContext(){
        return jaxbContext;
    }
    
    private static jakarta.xml.bind.JAXBContext createJAXBContext(){
        try{
            return jakarta.xml.bind.JAXBContext.newInstance(ObjectFactory.class);
        }catch(jakarta.xml.bind.JAXBException e){
            throw new WebServiceException(e.getMessage(), e);
        }
    }

    private Source sendSource() {
        System.out.println("**** sendSource ******");

        String[] body  = {
            "<HelloResponse xmlns=\"urn:test:types\"><argument xmlns=\"\">foo</argument><extra xmlns=\"\">bar</extra></HelloResponse>",
            "<ans1:HelloResponse xmlns:ans1=\"urn:test:types\"><argument>foo</argument><extra>bar</extra></ans1:HelloResponse>",
        };
        int i = (++bodyIndex)%body.length;
        return new StreamSource(
            new ByteArrayInputStream(body[i].getBytes()));
    }

    private Source sendFaultSource() {
        System.out.println("**** sendFaultSource ******");

        String body  = 
            "<soap:Fault xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><faultcode>soap:Server</faultcode><faultstring>Server was unable to process request. ---> Not a valid accountnumber.</faultstring><detail /></soap:Fault>";

        return new StreamSource(
            new ByteArrayInputStream(body.getBytes()));
    }

    private Hello_Type recvBean(Source source) throws Exception {
        System.out.println("**** recvBean ******");
        return (Hello_Type)jaxbContext.createUnmarshaller().unmarshal(source);
    }

    private Source sendBean() throws Exception {
        System.out.println("**** sendBean ******");
        HelloResponse resp = new HelloResponse();
        resp.setArgument("foo");
        resp.setExtra("bar");
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        jaxbContext.createMarshaller().marshal(resp, bout);
        return new StreamSource(new ByteArrayInputStream(bout.toByteArray()));
    }

    public void invoke(Source source, AsyncProviderCallback<Source> cbak, WebServiceContext ctxt) {
        System.out.println("**** Received in AsyncProvider Impl ******");
		try {
			Hello_Type hello = recvBean(source);
			String arg = hello.getArgument();
			if (arg.equals("sync")) {
				String extra = hello.getExtra();
				if (extra.equals("source")) {
					cbak.send(sendSource());
				} else if (extra.equals("bean")) {
					cbak.send(sendBean());
				} else if (extra.equals("fault")) {
					cbak.send(sendFaultSource());
				} else {
					throw new WebServiceException("Expected extra = (source|bean|fault), Got="+extra);
				}
			} else if (arg.equals("async")) {
				new Thread(new RequestHandler(cbak, hello)).start();
			} else {
				throw new WebServiceException("Expected Argument = (sync|async), Got="+arg);
			}
		} catch(Exception e) {
            throw new WebServiceException("Endpoint failed", e);
		}
    }

	private class RequestHandler implements Runnable {
		final AsyncProviderCallback<Source> cbak;
		final Hello_Type hello;

		public RequestHandler(AsyncProviderCallback<Source> cbak, Hello_Type hello) {
			this.cbak = cbak;
			this.hello = hello;
		}

		public void run() {
			try {
				Thread.sleep(15000);
			} catch(InterruptedException ie) {
				cbak.sendError(new WebServiceException("Interrupted..."));
				return;
			}
			try {
				String extra = hello.getExtra();
				if (extra.equals("source")) {
					cbak.send(sendSource());
				} else if (extra.equals("bean")) {
					cbak.send(sendBean());
				} else if (extra.equals("fault")) {
					cbak.send(sendFaultSource());
				} else {
					cbak.sendError(new WebServiceException("Expected extra = (source|bean|fault), Got="+extra));
				}
			} catch(Exception e) {
				cbak.sendError(new WebServiceException(e));
			}
		}
	}

}
