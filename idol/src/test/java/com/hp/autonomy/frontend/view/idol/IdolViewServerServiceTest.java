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

import com.autonomy.aci.client.services.AciErrorException;
import com.autonomy.aci.client.services.AciService;
import com.autonomy.aci.client.services.AciServiceException;
import com.autonomy.aci.client.services.Processor;
import com.autonomy.aci.client.util.AciParameters;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.view.idol.configuration.ViewCapable;
import com.hp.autonomy.frontend.view.idol.configuration.ViewConfig;
import com.hp.autonomy.idolutils.processors.AciResponseJaxbProcessorFactory;
import com.hp.autonomy.types.idol.DocContent;
import com.hp.autonomy.types.idol.GetContentResponseData;
import com.hp.autonomy.types.idol.Hit;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class IdolViewServerServiceTest {
    private static final String SAMPLE_REFERENCE_FIELD_NAME = "URL";

    @Mock
    private AciService contentAciService;

    @Mock
    private AciService viewAciService;

    @Mock
    private AciResponseJaxbProcessorFactory processorFactory;

    @Mock
    private ConfigService<? extends ViewCapable> configService;

    @Mock
    private ViewCapable viewCapableConfig;

    private IdolViewServerService idolViewServerService;

    @Before
    public void setUp() {
        final ViewConfig viewConfig = new ViewConfig.Builder().setReferenceField(SAMPLE_REFERENCE_FIELD_NAME).build();
        when(viewCapableConfig.getViewConfig()).thenReturn(viewConfig);
        when(configService.getConfig()).thenReturn(viewCapableConfig);

        idolViewServerService = new IdolViewServerService(contentAciService, viewAciService, processorFactory, configService);
    }

    @Test
    public void viewDocument() throws ViewNoReferenceFieldException, ViewDocumentNotFoundException, ReferenceFieldBlankException {
        final GetContentResponseData responseData = mockResponseData();
        when(contentAciService.executeAction(any(AciParameters.class), any(Processor.class))).thenReturn(responseData);

        idolViewServerService.viewDocument("dede952d-8a4d-4f54-ac1f-5187bf10a744", Collections.singletonList("SomeDatabase"), mock(Processor.class));

        verify(viewAciService, times(1)).executeAction(argThat(AciParameterMatcher.aciParametersWith("Reference", "http://en.wikipedia.org/wiki/Car")), any(Processor.class));
    }

    @Test(expected = ReferenceFieldBlankException.class)
    public void noReference() throws ViewNoReferenceFieldException, ViewDocumentNotFoundException, ReferenceFieldBlankException {
        when(viewCapableConfig.getViewConfig()).thenReturn(new ViewConfig.Builder().build());
        idolViewServerService.viewDocument(null, Collections.<String>emptyList(), mock(Processor.class));
    }

    @Test(expected = ViewDocumentNotFoundException.class)
    public void errorGettingContent() throws ViewNoReferenceFieldException, ViewDocumentNotFoundException, ReferenceFieldBlankException {
        when(contentAciService.executeAction(any(AciParameters.class), any(Processor.class))).thenThrow(new AciErrorException());
        idolViewServerService.viewDocument("dede952d-8a4d-4f54-ac1f-5187bf10a744", Collections.<String>emptyList(), mock(Processor.class));
    }

    @Test(expected = ViewDocumentNotFoundException.class)
    public void noDocumentFound() throws ViewNoReferenceFieldException, ViewDocumentNotFoundException, ReferenceFieldBlankException {
        when(contentAciService.executeAction(any(AciParameters.class), any(Processor.class))).thenReturn(new GetContentResponseData());
        idolViewServerService.viewDocument("dede952d-8a4d-4f54-ac1f-5187bf10a744", Collections.<String>emptyList(), mock(Processor.class));
    }

    @Test(expected = ViewNoReferenceFieldException.class)
    public void noMatchingField() throws ViewNoReferenceFieldException, ViewDocumentNotFoundException, ReferenceFieldBlankException {
        final GetContentResponseData responseData = new GetContentResponseData();

        final Hit hit = new Hit();
        responseData.getHit().add(hit);

        final DocContent content = new DocContent();
        hit.setContent(content);

        final Node node = mock(Node.class);
        content.getContent().add(node);

        final NodeList childNodes = mock(NodeList.class);
        when(childNodes.getLength()).thenReturn(0);
        when(node.getChildNodes()).thenReturn(childNodes);

        when(contentAciService.executeAction(any(AciParameters.class), any(Processor.class))).thenReturn(responseData);
        idolViewServerService.viewDocument("dede952d-8a4d-4f54-ac1f-5187bf10a744", Collections.<String>emptyList(), mock(Processor.class));
    }

    @Test(expected = ViewServerErrorException.class)
    public void viewServer404() {
        final GetContentResponseData responseData = mockResponseData();
        when(contentAciService.executeAction(any(AciParameters.class), any(Processor.class))).thenReturn(responseData);
        when(viewAciService.executeAction(any(AciParameters.class), any(Processor.class))).thenThrow(new AciServiceException());

        idolViewServerService.viewDocument("dede952d-8a4d-4f54-ac1f-5187bf10a744", Collections.singletonList("SomeDatabase"), mock(Processor.class));
    }

    private GetContentResponseData mockResponseData() {
        final GetContentResponseData responseData = new GetContentResponseData();

        final Hit hit = new Hit();
        responseData.getHit().add(hit);

        final DocContent content = new DocContent();
        hit.setContent(content);

        final Node node = mock(Node.class);
        content.getContent().add(node);

        final NodeList childNodes = mock(NodeList.class);
        when(childNodes.getLength()).thenReturn(1);
        when(node.getChildNodes()).thenReturn(childNodes);

        final Node referenceNode = mock(Node.class);
        when(referenceNode.getLocalName()).thenReturn(SAMPLE_REFERENCE_FIELD_NAME);

        final Node textNode = mock(Node.class);
        when(textNode.getNodeValue()).thenReturn("http://en.wikipedia.org/wiki/Car");
        when(referenceNode.getFirstChild()).thenReturn(textNode);
        when(childNodes.item(0)).thenReturn(referenceNode);

        return responseData;
    }

    private static class AciParameterMatcher extends ArgumentMatcher<AciParameters> {

        private final String parameter;
        private final String value;

        private AciParameterMatcher(final String parameter, final String value) {
            this.parameter = parameter;
            this.value = value;
        }

        private static Matcher<AciParameters> aciParametersWith(final String parameter, final String value) {
            return new AciParameterMatcher(parameter, value);
        }

        @Override
        public boolean matches(final Object argument) {
            if (!(argument instanceof AciParameters)) {
                return false;
            }

            final AciParameters parameters = (AciParameters) argument;

            return value.equals(parameters.get(parameter));
        }
    }
}
