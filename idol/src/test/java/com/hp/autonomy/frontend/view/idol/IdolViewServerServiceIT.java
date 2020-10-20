/*
 * (c) Copyright 2015 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.view.idol;

import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.services.impl.AciServiceImpl;
import com.autonomy.aci.client.transport.AciResponseInputStream;
import com.autonomy.aci.client.transport.AciServerDetails;
import com.autonomy.aci.client.transport.impl.AciHttpClientImpl;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.view.idol.configuration.ViewCapable;
import com.hp.autonomy.frontend.view.idol.configuration.ViewConfig;
import com.hp.autonomy.idolutils.processors.AciResponseJaxbProcessorFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class IdolViewServerServiceIT {
    private static final String SAMPLE_REFERENCE_FIELD_NAME = "DREREFERENCE";

    @Mock
    private ConfigService<? extends ViewCapable> configService;

    @Mock
    private ViewCapable viewCapableConfig;

    private IdolViewServerService idolViewServerService;

    @Before
    public void setUp() {
        final SocketConfig socketConfig = SocketConfig.custom()
                .setSoTimeout(90000)
                .build();

        final HttpClient httpClient = HttpClientBuilder.create()
                .setMaxConnPerRoute(20)
                .setMaxConnTotal(120)
                .setDefaultSocketConfig(socketConfig)
                .build();

        final ViewConfig viewConfig = new ViewConfig.Builder().setReferenceField(SAMPLE_REFERENCE_FIELD_NAME).build();
        when(viewCapableConfig.getViewConfig()).thenReturn(viewConfig);
        when(configService.getConfig()).thenReturn(viewCapableConfig);

        final AciService contentAciService = new AciServiceImpl(new AciHttpClientImpl(httpClient), new AciServerDetails("abc-dev.hpswlabs.hp.com", 9000));
        final AciService viewAciService = new AciServiceImpl(new AciHttpClientImpl(httpClient), new AciServerDetails("abc-dev.hpswlabs.hp.com", 9080));

        final AciResponseJaxbProcessorFactory processorFactory = new AciResponseJaxbProcessorFactory();

        idolViewServerService = new IdolViewServerService(contentAciService, viewAciService, processorFactory, configService);
    }

    @Test
    public void viewDocument() throws ViewNoReferenceFieldException, ViewDocumentNotFoundException, ReferenceFieldBlankException {
        final Processor<?> processor = mock(Processor.class);
        idolViewServerService.viewDocument("http://washingtontimes.feedsportal.com/c/34503/f/629218/s/1fb85532/l/0L0Swashingtontimes0N0Cnews0C20A120Cmay0C250Chonoring0Eour0Efallen0Eby0Esupporting0Etheir0Eloved0Eones0C0Dutm0Isource0FRSS0IFeed0Gutm0Imedium0FRSS/story01.htm", Collections.<String>emptyList(), processor);
        verify(processor).process(any(AciResponseInputStream.class));
    }
}
