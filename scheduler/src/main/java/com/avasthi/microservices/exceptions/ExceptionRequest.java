package com.avasthi.microservices.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by maheshsapre on 27/06/17.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExceptionRequest {
    private String method;
    private String uri;
    private Object params;
    private Object body;
    private Object headers;

    /**
     * Update.
     *
     * @param headers the headers
     * @param method  the method
     * @param uri     the uri
     * @param params  the params
     * @param body    the body
     */
    public void update(Object headers, String method, String uri, Object params, Object body) {
        this.method = method;
        this.uri = uri;
        this.params = params;
        this.headers = headers;
        this.body = body;
    }

    /**
     * Gets method.
     *
     * @return the method
     */
    public String getMethod() {
        return method;
    }

    /**
     * Sets method.
     *
     * @param method the method
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * Gets uri.
     *
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * Sets uri.
     *
     * @param uri the uri
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * Gets params.
     *
     * @return the params
     */
    public Object getParams() {
        return params;
    }

    /**
     * Sets params.
     *
     * @param params the params
     */
    public void setParams(Object params) {
        this.params = params;
    }

    /**
     * Gets headers.
     *
     * @return the headers
     */
    public Object getHeaders() {
        return headers;
    }

    /**
     * Sets headers.
     *
     * @param headers the headers
     */
    public void setHeaders(Object headers) {
        this.headers = headers;
    }

    /**
     * Gets body.
     *
     * @return the body
     */
    public Object getBody() {
        return body;
    }

    /**
     * Sets body.
     *
     * @param body the body
     */
    public void setBody(Object body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "ExceptionRequest{" +
                "headers=" + headers +
                ", method='" + method + '\'' +
                ", uri='" + uri + '\'' +
                ", params=" + params +
                ", body=" + body +
                '}';
    }
}
