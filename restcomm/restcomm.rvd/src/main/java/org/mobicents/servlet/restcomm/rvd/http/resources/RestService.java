/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2016, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */

package org.mobicents.servlet.restcomm.rvd.http.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.mobicents.servlet.restcomm.rvd.configuration.RvdConfigurator;
import org.mobicents.servlet.restcomm.rvd.configuration.RvdConfiguratorBuilder;
import org.mobicents.servlet.restcomm.rvd.exceptions.RvdException;
import org.mobicents.servlet.restcomm.rvd.http.RvdResponse;
import org.mobicents.servlet.restcomm.rvd.model.callcontrol.CallControlAction;
import org.mobicents.servlet.restcomm.rvd.model.callcontrol.CallControlStatus;
import org.mobicents.servlet.restcomm.rvd.model.callcontrol.CreateCallResponse;
import org.mobicents.servlet.restcomm.rvd.validation.ValidationReport;

import com.google.gson.Gson;

public class RestService {

    @Context
    protected HttpServletRequest request;
    @Context
    protected ServletContext servletContext;
    protected RvdConfigurator configurator;

    void init() {
        this.configurator = RvdConfiguratorBuilder.get();
    }

    protected Response buildErrorResponse(Response.Status httpStatus, RvdResponse.Status rvdStatus, RvdException exception) {
        RvdResponse rvdResponse = new RvdResponse(rvdStatus).setException(exception);
        return Response.status(httpStatus).entity(rvdResponse.asJson()).build();
    }

    protected  Response buildInvalidResponse(Response.Status httpStatus, RvdResponse.Status rvdStatus, ValidationReport report ) {
        RvdResponse rvdResponse = new RvdResponse(rvdStatus).setReport(report);
        return Response.status(httpStatus).entity(rvdResponse.asJson()).build();
    }

    //Response buildInvalidResponse(Response.Status httpStatus, RvdResponse.Status rvdStatus, RvdException exception ) {
    //    RvdResponse rvdResponse = new RvdResponse(rvdStatus).setException(exception);
    //    return Response.status(httpStatus).entity(rvdResponse.asJson()).build();
    //}

    protected  Response buildOkResponse() {
        RvdResponse rvdResponse = new RvdResponse( RvdResponse.Status.OK );
        return Response.status(Response.Status.OK).entity(rvdResponse.asJson()).build();
    }

    protected  Response buildOkResponse(Object payload) {
        RvdResponse rvdResponse = new RvdResponse().setOkPayload(payload);
        return Response.status(Response.Status.OK).entity(rvdResponse.asJson()).build();
    }

    protected int size(InputStream stream) {
        int length = 0;
        try {
            byte[] buffer = new byte[2048];
            int size;
            while ((size = stream.read(buffer)) != -1) {
                length += size;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return length;

    }

    protected String read(InputStream stream) {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
            }
        }
        return sb.toString();

    }

    protected Response buildWebTriggerHtmlResponse(String title, String action, String outcome, String description, Integer status ) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<html><body>");
        if (title != null)
            buffer.append("<h1>").append(title).append("</h1>");
        if (action != null) {
            buffer.append("<h4>").append(action);
            if (outcome != null)
                buffer.append(" - " + outcome);
            buffer.append("</h4>");
        }
        if (description != null)
            buffer.append("<p>").append(description).append("</p>");
        buffer.append("</body></html>");

        return Response.status(status).entity(buffer.toString()).type(MediaType.TEXT_HTML).build();
    }

    protected Response buildWebTriggerJsonResponse(CallControlAction action, CallControlStatus status, Integer httpStatus, Object restcommResponse ) {
        CreateCallResponse response = new CreateCallResponse().setAction(action).setStatus(status).setData(restcommResponse);
        Gson gson = new Gson();
        return Response.status(httpStatus).entity( gson.toJson(response)).type(MediaType.APPLICATION_JSON).build();
    }
}